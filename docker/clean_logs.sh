#!/usr/bin/env bash

find . -type d \( -name 'log' -o -name 'logs' \) | xargs -I {} find {} -type f | xargs rm -rf

rm -f ./services/pulsar/code/pulsar-manager/apache-pulsar-manager-0.4.0-bin/pulsar-manager/pulsar-manager.log