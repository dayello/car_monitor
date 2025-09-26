#!/usr/bin/env bash

service_name=$1
if [ -z "$service_name" ]; then
  echo "Usage: $0 <service_name>"
  exit 1
fi

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
service_dir="$script_dir/services/$service_name"
service_log_dir="$service_dir/logs"
service_data_dir="$service_dir/data"
service_code_dir="$service_dir/code"
service_script_dir="$service_dir/script"
service_config_dir="$service_dir/conf"

if [ -d "$service_dir" ]; then
  echo "Service $service_name already exists"
  exit 1
fi

mkdir -p "$service_dir"
mkdir -p "$service_log_dir"
mkdir -p "$service_data_dir"
mkdir -p "$service_code_dir"
mkdir -p "$service_script_dir"
mkdir -p "$service_config_dir"

cp $script_dir/docker-compose.yml "$service_dir"
touch "$service_dir/docker-compose.env"
touch "$service_dir/README.md"