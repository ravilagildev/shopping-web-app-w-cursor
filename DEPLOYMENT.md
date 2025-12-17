# Deployment (EC2 + S3, CLI only)

These steps deploy the app without pushing to a remote repo. Replace ALL_CAPS placeholders:
- `REGION`: your AWS region, e.g., `us-east-1`.
- `BUCKET`: any globally-unique S3 bucket name.
- `PUBLIC_IP_OR_DOMAIN` / `PUBLIC_IP`: the EC2 public IP (or your domain if you front it).
- `KEY_NAME`: name of your EC2 SSH key pair (file will be `KEY_NAME.pem`).
- `SEC_GROUP_NAME`: a name for the security group.
- Secrets: `APP_USERNAME`, `APP_PASSWORD`, `JWT_SECRET` (use a long random secret).

## Prereqs (local)
- AWS CLI configured (`aws configure`) with permissions for EC2 + S3.
- Docker installed (for local build, optional).
- Node 18+ for frontend build.
- Choose region (e.g., `us-east-1`).

## 1) Frontend → S3
```bash
REGION=us-east-1
BUCKET=my-coffee-tracker-frontend-$(date +%s)

# Create bucket
aws s3api create-bucket --bucket "$BUCKET" --region "$REGION" \
  --create-bucket-configuration LocationConstraint="$REGION"

# Build frontend
cd frontend
npm install
VITE_API_BASE_URL=http://PUBLIC_IP_OR_DOMAIN:8080/api npm run build  # set to backend URL

# Upload
aws s3 sync dist/ s3://"$BUCKET"/ --delete

# (Optional) Static site hosting
aws s3 website s3://"$BUCKET"/ --index-document index.html --error-document index.html
# URL: http://$BUCKET.s3-website-$REGION.amazonaws.com
```

## 2) Backend on EC2 (small instance)
```bash
REGION=us-east-1
KEY_NAME=my-keypair
SEC_GROUP_NAME=coffee-tracker-sg

# Latest Amazon Linux 2023 x86_64 AMI
AMI_ID=$(aws ec2 describe-images --owners amazon \
  --filters "Name=name,Values=al2023-ami-*-x86_64" "Name=state,Values=available" \
  --query 'Images | sort_by(@,&CreationDate)[-1].ImageId' --output text)

# Key pair (if needed)
aws ec2 create-key-pair --key-name "$KEY_NAME" --query 'KeyMaterial' --output text > "$KEY_NAME".pem
chmod 400 "$KEY_NAME".pem

# Security group (80 + 8080 open; tighten CIDR if desired)
VPC_ID=$(aws ec2 describe-vpcs --query 'Vpcs[0].VpcId' --output text)
SG_ID=$(aws ec2 create-security-group --group-name "$SEC_GROUP_NAME" --description "gifts sg" --vpc-id "$VPC_ID" --query 'GroupId' --output text)
aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 80 --cidr 0.0.0.0/0
aws ec2 authorize-security-group-ingress --group-id "$SG_ID" --protocol tcp --port 8080 --cidr 0.0.0.0/0

# Launch instance (t3.nano)
INSTANCE_ID=$(aws ec2 run-instances --image-id "$AMI_ID" --instance-type t3.nano \
  --key-name "$KEY_NAME" --security-group-ids "$SG_ID" \
  --query 'Instances[0].InstanceId' --output text)
aws ec2 wait instance-status-ok --instance-ids "$INSTANCE_ID"
PUBLIC_IP=$(aws ec2 describe-instances --instance-ids "$INSTANCE_ID" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
echo "PUBLIC_IP=$PUBLIC_IP"
```

## 3) Install Docker on EC2
```bash
ssh -i "$KEY_NAME".pem ec2-user@"$PUBLIC_IP"
sudo yum update -y
sudo amazon-linux-extras install docker -y || sudo dnf install docker -y
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ec2-user
exit
# Reconnect
ssh -i "$KEY_NAME".pem ec2-user@"$PUBLIC_IP"
```

## 4) Copy backend and build image on EC2
```bash
# Local -> EC2
scp -i "$KEY_NAME".pem -r backend ec2-user@"$PUBLIC_IP":~/backend

# On EC2
cd ~/backend
docker build -t gifts-backend .
```

## 5) Run backend container
Choose secrets and CORS origin:
```bash
APP_USERNAME=admin
APP_PASSWORD=changeMe
JWT_SECRET=please-change-to-long-random-256-bit
CORS_ALLOWED_ORIGINS=http://$BUCKET.s3-website-$REGION.amazonaws.com  # Your S3 bucket URL

docker run -d --name gifts-backend \
  -p 8080:8080 \
  -e APP_USERNAME="$APP_USERNAME" \
  -e APP_PASSWORD="$APP_PASSWORD" \
  -e JWT_SECRET="$JWT_SECRET" \
  -e CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS" \
  --restart unless-stopped \
  gifts-backend
```

### Optional: persist H2 locally (avoids data loss on restart)
```bash
docker run -d --name gifts-backend \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:h2:/data/christmasgifts \
  -e SPRING_DATASOURCE_DRIVERCLASSNAME=org.h2.Driver \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
  -e APP_USERNAME="$APP_USERNAME" \
  -e APP_PASSWORD="$APP_PASSWORD" \
  -e JWT_SECRET="$JWT_SECRET" \
  -e CORS_ALLOWED_ORIGINS="$CORS_ALLOWED_ORIGINS" \
  -v /var/lib/gifts-data:/data \
  --restart unless-stopped \
  gifts-backend
```

### Test API
```bash
curl http://$PUBLIC_IP:8080/api/budget/summary
```

## 6) Point frontend to backend
- Rebuild frontend with `VITE_API_BASE_URL=http://$PUBLIC_IP:8080/api npm run build`
- `aws s3 sync dist/ s3://"$BUCKET"/ --delete`
- Open `http://$BUCKET.s3-website-$REGION.amazonaws.com` (or CloudFront/custom domain if added).

## Optional enhancements
- CloudFront for HTTPS + caching on frontend.
- Nginx reverse proxy on EC2 for HTTPS to backend (or use an ALB/ACM).
- Use ECR if you prefer not to build on-instance.
- Move to Postgres/RDS if you need durable data beyond H2 file mode.

