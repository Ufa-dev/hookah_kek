#!/usr/bin/env bash
set -euo pipefail

REGISTRY="sullssul"
TAG="${1:-latest}"

echo "Building backend..."
docker build -t "$REGISTRY/hookah-backend:$TAG" .

echo "Building frontend..."
docker build -t "$REGISTRY/hookah-frontend:$TAG" ./frontend

echo "Pushing images..."
docker push "$REGISTRY/hookah-backend:$TAG"
docker push "$REGISTRY/hookah-frontend:$TAG"

echo "Done. Images published:"
echo "  docker.io/$REGISTRY/hookah-backend:$TAG"
echo "  docker.io/$REGISTRY/hookah-frontend:$TAG"
