#!/usr/bin/env bash 

echo "Started: `date`" >> benchmark.log

echo "Cat'ting input file:"

cat testfile.txt

echo "Sleeping 30 seconds."

sleep 30

echo "Slept well."

echo "Finished: `date`" >> benchmark.log
