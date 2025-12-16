#!/usr/bin/env bash
set -euo pipefail

# Simple redeploy script: builds frontend, syncs to S3, rsyncs backend to EC2, rebuilds container, restarts it.
# Optional: can provision EC2 + security group if PROVISION_EC2=true.
# Requirements:
# - aws cli configured locally
# - ssh access to EC2 with KEY_PATH (if reusing an instance)
# - docker on EC2 (auto-installed if missing)
#
# Usage:
#   1) cp deploy.env.example deploy.env
#   2) edit deploy.env with your values
#   3) ./deploy.sh

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Load env
if [[ -f "$ROOT_DIR/deploy.env" ]]; then
  # shellcheck disable=SC1091
  source "$ROOT_DIR/deploy.env"
else
  echo "deploy.env not found. Copy deploy.env.example to deploy.env and fill in values." >&2
  exit 1
fi

: "${REGION:?Set REGION in deploy.env}"
: "${BUCKET:?Set BUCKET in deploy.env}"
: "${KEY_NAME:?Set KEY_NAME in deploy.env}"
: "${SSH_HOST:?Set SSH_HOST (e.g., ec2-user@1.2.3.4) in deploy.env}"
: "${KEY_PATH:?Set KEY_PATH to your SSH key in deploy.env}"
: "${APP_USERNAME:?Set APP_USERNAME in deploy.env}"
: "${APP_PASSWORD:?Set APP_PASSWORD in deploy.env}"
: "${JWT_SECRET:?Set JWT_SECRET in deploy.env}"

PROVISION_EC2="${PROVISION_EC2:-false}"
INSTANCE_TYPE="${INSTANCE_TYPE:-t3.nano}"
AMI_ARCH="${AMI_ARCH:-x86_64}"          # x86_64 or arm64
SEC_GROUP_NAME="${SEC_GROUP_NAME:-gifts-sg}"
SSH_USER="${SSH_USER:-ec2-user}"

BACKEND_REMOTE_DIR="${BACKEND_REMOTE_DIR:-~/backend}"
CONTAINER_NAME="${CONTAINER_NAME:-gifts-backend}"
IMAGE_NAME="${IMAGE_NAME:-gifts-backend}"
H2_DATA_PATH="${H2_DATA_PATH:-}" # if set, H2 will persist to this host path

echo "==> Using API_BASE_URL=$API_BASE_URL"

### Ensure bucket exists ###
echo "==> Ensuring S3 bucket exists: $BUCKET"
if ! aws s3api head-bucket --bucket "$BUCKET" --region "$REGION" >/dev/null 2>&1; then
  if [[ "$REGION" == "us-east-1" ]]; then
    aws s3api create-bucket --bucket "$BUCKET"
  else
    aws s3api create-bucket --bucket "$BUCKET" --region "$REGION" \
      --create-bucket-configuration LocationConstraint="$REGION"
  fi
fi

### Ensure key pair exists locally and in AWS ###
if [[ ! -f "$KEY_PATH" ]]; then
  echo "==> Creating key pair $KEY_NAME and saving to $KEY_PATH"
  mkdir -p "$(dirname "$KEY_PATH")"
  aws ec2 create-key-pair --key-name "$KEY_NAME" \
    --query 'KeyMaterial' --output text > "$KEY_PATH"
  chmod 400 "$KEY_PATH"
else
  echo "==> Found key file at $KEY_PATH"
fi

if ! aws ec2 describe-key-pairs --key-names "$KEY_NAME" >/dev/null 2>&1; then
  echo "==> Registering key pair $KEY_NAME in AWS (will not overwrite existing file)"
  aws ec2 create-key-pair --key-name "$KEY_NAME" \
    --query 'KeyMaterial' --output text > /dev/null
fi

### Optionally provision EC2 + security group ###
if [[ "$PROVISION_EC2" == "true" ]]; then
  echo "==> Provisioning EC2 instance..."

  if [[ "$AMI_ARCH" == "arm64" ]]; then
    AMI_FILTER="al2023-ami-*-arm64"
  else
    AMI_FILTER="al2023-ami-*-x86_64"
  fi

  AMI_ID=$(aws ec2 describe-images --owners amazon \
    --filters "Name=name,Values=$AMI_FILTER" "Name=state,Values=available" \
    --query 'Images | sort_by(@,&CreationDate)[-1].ImageId' --output text)

  VPC_ID=$(aws ec2 describe-vpcs --query 'Vpcs[0].VpcId' --output text)

  # Get or create security group
  SG_ID=""
  if aws ec2 describe-security-groups --group-names "$SEC_GROUP_NAME" >/dev/null 2>&1; then
    SG_ID=$(aws ec2 describe-security-groups --group-names "$SEC_GROUP_NAME" --query 'SecurityGroups[0].GroupId' --output text)
    echo "==> Using existing security group $SEC_GROUP_NAME ($SG_ID)"
  else
    SG_ID=$(aws ec2 create-security-group --group-name "$SEC_GROUP_NAME" --description "gifts sg" --vpc-id "$VPC_ID" --query 'GroupId' --output text)
    echo "==> Created security group $SEC_GROUP_NAME ($SG_ID)"
    aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 22 --cidr 0.0.0.0/0
    aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 80 --cidr 0.0.0.0/0
    aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 8080 --cidr 0.0.0.0/0
  fi

  INSTANCE_ID=$(aws ec2 run-instances --image-id "$AMI_ID" --instance-type "$INSTANCE_TYPE" \
    --key-name "$KEY_NAME" --security-group-ids "$SG_ID" \
    --query 'Instances[0].InstanceId' --output text)
  echo "==> Waiting for instance $INSTANCE_ID..."
  aws ec2 wait instance-status-ok --instance-ids "$INSTANCE_ID"
  PUBLIC_IP=$(aws ec2 describe-instances --instance-ids "$INSTANCE_ID" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
  SSH_HOST="${SSH_USER}@${PUBLIC_IP}"
  echo "==> Provisioned EC2 at $SSH_HOST"
fi

API_BASE_URL="${API_BASE_URL:-http://${SSH_HOST#*@}:8080/api}"
echo "==> Using API_BASE_URL=$API_BASE_URL"

### Frontend build + upload ###
echo "==> Building frontend..."
pushd "$ROOT_DIR/frontend" >/dev/null
npm install
VITE_API_BASE_URL="$API_BASE_URL" npm run build
echo "==> Syncing frontend to S3 bucket: $BUCKET"
aws s3 sync dist/ "s3://$BUCKET/" --delete --region "$REGION"
popd >/dev/null

### Rsync backend to EC2 ###
echo "==> Syncing backend to EC2: $SSH_HOST"
rsync -az --delete \
  --exclude target \
  --exclude .git \
  --exclude .idea \
  --exclude .vscode \
  -e "ssh -i $KEY_PATH" \
  "$ROOT_DIR/backend/" "$SSH_HOST:$BACKEND_REMOTE_DIR/"

### Remote build & restart ###
REMOTE_SCRIPT=$(cat <<'EOS'
set -euo pipefail

# Install docker if missing
if ! command -v docker >/dev/null 2>&1; then
  echo "==> Installing Docker..."
  if command -v amazon-linux-extras >/dev/null 2>&1; then
    sudo amazon-linux-extras install docker -y
  else
    sudo dnf install -y docker || sudo yum install -y docker
  fi
  sudo systemctl enable docker
  sudo systemctl start docker
fi

cd "$BACKEND_REMOTE_DIR"
echo "==> Building backend image..."
docker build -t "$IMAGE_NAME" .

echo "==> Restarting container..."
docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true

RUN_CMD=(docker run -d --name "$CONTAINER_NAME"
  -p 8080:8080
  -e APP_USERNAME="$APP_USERNAME"
  -e APP_PASSWORD="$APP_PASSWORD"
  -e JWT_SECRET="$JWT_SECRET"
  --restart unless-stopped
)

if [[ -n "${H2_DATA_PATH:-}" ]]; then
  RUN_CMD+=(
    -e SPRING_DATASOURCE_URL=jdbc:h2:/data/christmasgifts
    -e SPRING_DATASOURCE_DRIVERCLASSNAME=org.h2.Driver
    -e SPRING_JPA_HIBERNATE_DDL_AUTO=update
    -v "${H2_DATA_PATH}:/data"
  )
fi

RUN_CMD+=("$IMAGE_NAME")

echo "==> Running: ${RUN_CMD[*]}"
"${RUN_CMD[@]}"
EOS
)

echo "==> Deploying on EC2..."
ssh -i "$KEY_PATH" "$SSH_HOST" \
  BACKEND_REMOTE_DIR="$BACKEND_REMOTE_DIR" \
  IMAGE_NAME="$IMAGE_NAME" \
  CONTAINER_NAME="$CONTAINER_NAME" \
  APP_USERNAME="$APP_USERNAME" \
  APP_PASSWORD="$APP_PASSWORD" \
  JWT_SECRET="$JWT_SECRET" \
  H2_DATA_PATH="$H2_DATA_PATH" \
  'bash -s' <<< "$REMOTE_SCRIPT"

echo "==> Done. Frontend synced to s3://$BUCKET, backend running on $SSH_HOST:8080"

