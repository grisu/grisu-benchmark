#!/usr/bin/env bash 

echo "Started: `date +%s`" >> benchmark.log

echo "Cat'ting input file:"

cat testfile.txt

seconds=$(($GRISU_CPUS * 30))

sleeptime=$((600 - $seconds))

echo "Sleeping $sleeptime seconds."

for i in $(eval echo {1..$GRISU_CPUS})
do
	hostname >> hostfile
done

sleep $sleeptime

echo "Slept well."

echo "Finished: `date +%s`" >> benchmark.log
