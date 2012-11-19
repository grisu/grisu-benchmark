Grisu
=====
 
Grisu is an open source framework to help grid admins and developers to support end users in a grid environment. Grisu publishes an easy-to-use service interface which by default sits behind a web service. This service interface contains a set of methods that are usually needed to submit jobs to the grid, including providing information about the grid and the staging of input/output files from/to the users desktop.

Installation and configuration
https://github.com/grisu/grisu/wiki/Grisu-Backend-install-and-configuration

Documentation
------------------------
The Documentation can bee found at
- [Wiki](https://github.com/grisu/grisu/wiki)
- [Javadoc](http://grisu.github.com/grisu/javadoc/)
- [SOAP/REST API documentaion](https://compute.services.bestgrid.org/)

--script example\testjob.sh --cpu 3[20],1=2[2m],2=3,5,3=5,4=4,6=6,5=5,6=6,7=7,8=8,9=9,10=1 --group /nz/nesi --queue pan:pan.nesi.org.nz --files example\testfile.txt --jobname cat19nov2 -w 60

Usage:
--------------------

1. --script (required)
It requires users to specify the script file for running a job, at the command line using the

2. --cpu (optional) 
It allows to specify the number of CPUs to be used for running a job as a comma separated list, and also the corresponding host count and the wall time.

3. -n or --jobname (required)
It requires the user to specify a name for the job to be run. (the current timestamp is appended to the specified name)

4. -q or --queue (required)
It also requires specification of the queue to which the job should be submitted

5. -w or --walltime (required)
It requires the user to specify a wall time for the job and allows specification of wall time in seconds/minutes/hours/days.

6. --group (required)
It also requires the user to specify a group for running the job

7. -single or -mpi (optional) 
It enables the job type to be set to single or mpi

8. --env (optional)
It enables the user to set additional environment variables for a job


The details of all finished jobs can be viewed, by giving the corresponding job name as a command line argument.
The details like job name, host count, status, number of CPUs used, wall time, total execution time, average execution time per CPU and efficiency are logged in a newly created csv file having the job's name.
An HTML file showing a graph of the execution time, per cpu execution time and efficiency against the number of CPUs is also created.


