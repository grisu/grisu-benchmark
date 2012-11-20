Grisu Benchmark
================
 
_grisu-benchmark_  is a Grisu client which allows you to run the same job with several (cpu-)configurations and measure how execution time is affected.


Download / Install
-------------------

### Requirements

 * Java 6

### Downloads
 * executable jar file: [here](https://code.ceres.auckland.ac.nz/jenkins/job/grisu-benchmarks-SNAPSHOT/lastSuccessfulBuild/artifact/target/grisu-benchmark-binary.jar)
 * deb & rpm packages: [here](https://code.ceres.auckland.ac.nz/jenkins/job/grisu-benchmarks-SNAPSHOT/)

### Usage

If you are using the jar file, you can start _grisu-benchmark_ like this:

    # to submit & start a benchmark
    java -cp grisu-benchmark-binary.jar grisu.frontend.Client
    # to get the benchmark results
    java -cp grisu-benchmark-binary.jar grisu.frontend.ClientResults
    
    
If you installed either the deb or rpm package, you can start it via:

    # to submit & start a benchmark
    grisu-benchmark-submit
    # to get the benchmark results
    grisu-benchmark-results
    
#### Preparing a benchmark

First, we need to prepare a shell script that executes the job and writes out the start & end time of the part of the job to measure into a file called _benchmark.log_. Here's a very simple example, copy the following into a file called _testjob.sh_:

    #!/usr/bin/env bash 

    echo "Started: `date +%s`" >> benchmark.log

    echo "Cat'ting input file:"
    cat testfile.txt

    sleeptime=$(($GRISU_CPUS * 30))
    sleep $sleeptime

    echo "Slept well."
    echo "Finished: `date +%s`" >> benchmark.log

Start and endtime need to be prepended by _Started:_ and _Finished:_, just copy the corresponding lines from the above job into your wrapper script.

For this testjob we also need a textfile with some random content. Created that and name it _testfile.txt_.

#### Submitting the benchmark

    grisu-benchmark-subit --script /path/to/testjob.sh --cpu 1,2,3,4,5,6,7,8,16,32,64 --group /nz/nesi --queue pan:pan.nesi.org.nz --files /path/to/testfile.txt --jobname testjob -w 2000

##### Available commandline parameters for benchmark submission

--script (required)
the script that wraps the job that needs to be benchmarked, needs to write out a file called _benchmark.log_ (see above example)

--cpu
specifies the series of CPUs to be used for this benchmark as a comma separated list. You can also specify run-specific hostcount and walltime, format is: cpu-count=hostcount[walltime] (where hostcount and walltime are optional, walltime in seconds or format like 2d10h22m)

-n or --jobname (required)
the basename for all jobs of this benchmark run, the number of cpus and a current timestamp is appended to each job.

-q or --queue (required)
the queue to which the job should be submitted, for example: pan:pan.nesi.org.nz

-w or --walltime (required)
the default wall time for each job (if not specified in the --cpus option), in seconds or format like: 2d10h12m

--group (required)
the group for running the benchmark

--single or --mpi (optional) 
forcing to single or mpi (mind, mpi does not make sense most of the time)

--env (optional)
allows to set additional environment variables for a job, something like: --env LL_VAR=requirements=(Feature==\"sandybridge\") --env otherkey=othervalue

#### Gathering the benchmark results

    grisu-benchmark-results --jobname testjob

The details of all finished jobs can be viewed, by giving the corresponding job name as a command line argument.
The details like job name, host count, status, number of CPUs used, wall time, total execution time, average execution time per CPU and efficiency are logged in a newly created csv file having the job's name.
An HTML file showing a graph of the execution time, per cpu execution time and efficiency against the number of CPUs is also created.


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


