#!/bin/bash
sqsub -q threaded -n $1 -o sqmaria.log -r 1d ./batchset_work.sh $1 $2

