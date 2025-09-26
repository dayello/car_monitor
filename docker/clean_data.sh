#!/usr/bin/env bash

find ./services -type d \( -name 'data' -o -name 'datas' \) | xargs -I {} find {} -type f | grep -vE '.gitignore|\/code\/|\/mysql\/' | xargs rm -rf