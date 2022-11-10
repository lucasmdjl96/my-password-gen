#!/bin/bash
export $(cat aws.env | xargs)
java -jar my-password-gen-all.jar
