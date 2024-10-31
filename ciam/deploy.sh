#!/bin/bash

# Step 1: Installing Zero Downtime Plugin
echo "Installing Zero Downtime Plugin..."
cf install-plugin autopilot -f -r CF-Community

if [ $? -ne 0 ]; then
  echo "Failed to install autopilot plugin"
  exit 1
fi

# Step 2: Building A source using Maven
echo "Building A source using Maven..."
cd /home/user/projects/SAMSUNG_CIAM/ciam  # Change directory to where pom.xml is located
mvn clean package -DskipTests=true

if [ $? -ne 0 ]; then
  echo "Failed to build A source"
  exit 1
fi

# Step 3: Deploying to Cloud Foundry using cf cli
echo "Deploying to Cloud Foundry..."
cf zero-downtime-push ciam -f /home/user/projects/SAMSUNG_CIAM/ciam/manifest.yml | tee deployment.log

if [ $? -ne 0 ]; then
  echo "Zero-downtime deployment failed"
  exit 1
fi

echo "Deployment completed successfully"