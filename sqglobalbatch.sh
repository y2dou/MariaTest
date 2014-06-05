#!/bin/bash
sqsub --global -o sqmaria.log -r $1 ./batch_home.sh $2

