#!/bin/sh

KAA_KEY_PATH="./cfg/key.txt"

openssl genrsa -out key.pem 2048
openssl rsa -in key.pem -outform der -pubout > $KAA_KEY_PATH
rm key.pem
