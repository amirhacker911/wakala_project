#!/bin/bash
# Usage: run as root on your Vultr Ubuntu 22.04 server
apt update && apt upgrade -y
apt install -y docker.io docker-compose git
systemctl enable --now docker
echo 'Docker installed. Clone your repo and run: docker compose up -d --build'
