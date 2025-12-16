#!/bin/bash

echo "Starting Spring Boot Backend..."
cd backend

# Try mvnw first, then fall back to mvn
if [ -f "./mvnw" ]; then
    ./mvnw spring-boot:run
elif command -v mvn &> /dev/null; then
    mvn spring-boot:run
else
    echo "Error: Maven not found. Please install Maven or use the Maven wrapper."
    exit 1
fi

