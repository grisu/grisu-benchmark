Grisu Benchmark
================
 
_grisu-benchmark_  is a Grisu client which allows you to run the same job with several (cpu-)configurations and measure how execution time is affected. 

It can also compare several of those runs against each other and produce html files like [this](https://raw.github.com/grisu/grisu-benchmark/develop/example/benchmark.html) (you'll need to download this file if you want your browser to render it correctly).


Download / Install
-------------------

### Requirements

 * Java 6

### Downloads
 * executable jar file: [here](http://code.ceres.auckland.ac.nz/stable-downloads/grisu-benchmark/grisu-benchmark-binary.jar)
 * linux packages: [deb](http://code.ceres.auckland.ac.nz/stable-downloads/grisu-benchmark/grisu-benchmark.deb) and [rpm](http://code.ceres.auckland.ac.nz/stable-downloads/grisu-benchmark/grisu-benchmark.rpm)

### Usage

If you are using the jar file, you can start _grisu-benchmark_ like this:

    # to submit & start a benchmark
    java -cp grisu-benchmark-binary.jar grisu.frontend.benchmark.Submit
    # to get the benchmark results
    java -cp grisu-benchmark-binary.jar grisu.frontend.benchmark.Results
    
    
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

For this testjob we also need a textfile with some random content. Create that and name it _testfile.txt_.

#### Submitting the benchmark

    grisu-benchmark-subit --script /path/to/testjob.sh --cpu 1,2,3,4,5,6,7,8,16,32,64 --group /nz/nesi --queue pan:pan.nesi.org.nz --files /path/to/testfile.txt --jobname testjob -w 6h

##### Available commandline parameters for benchmark submission

_--script_ (required):
the script that wraps the job that needs to be benchmarked, needs to write out a file called _benchmark.log_ (see above example)

_--cpu_:
specifies the series of CPUs to be used for this benchmark as a comma separated list. You can also specify run-specific hostcount and walltime, format is: 

    cpucount=hostcount[walltime] # where hostcount and walltime are optional, walltime in seconds or format like 2d10h22m

_-n_ or _--jobname_ (required):
the basename for all jobs of this benchmark run, the number of cpus and a current timestamp is appended to each job.

_-q_ or _--queue_ (required):
the queue to which the job should be submitted, for example: pan:pan.nesi.org.nz

_-w_ or _--walltime_ (required):
the default wall time for each job (if not specified in the --cpus option), in seconds or format like: 2d10h12m

_--group_ (required):
the group for running the benchmark

_--single_ or _--mpi_ (optional):
forcing to single or mpi (mind, mpi does not make sense most of the time)

_--env_ (optional):
allows to set additional environment variables for a job, something like: 

    --env "LL_VAR=requirements=(Feature==\"sandybridge\")" --env otherkey=othervalue

#### Gathering the benchmark results

If you only want details for one job:

    grisu-benchmark-results --jobnames testjob
    
If you want to compare several benchmark runs against each other, specify a list of comma-seperated benchmark names:

    grisu-benchmark-results --jobnames testjob,testjob2,testjob3
    
The details like job name, host count, status, number of CPUs used, wall time, total execution time, average execution time per CPU and efficiency are logged in a newly created csv file having the job's name.
    
You can also specify an already created .csv file instead of a benchmark name for both of the above scenarios.

An HTML file (called 'benchmark.html) is created showing a graph of the execution time, per cpu execution time and efficiency against the number of CPUs is also created.

##### Available commandline parameters for gathering benchmark results

_--list_ (optional, default if no other parameter specified): 
the list option if specified, lists down all the current benchmark jobs along with the total number of jobs, number of finished jobs and the number of jobs which are still in progress for each benchmark job

_-n_ or _--jobnames_ (required if --list option not used): 
specifies a series of benchmark-job names, as a comma separated list, whose results need to be obtained

_--no-wait_ (optional): 
if specified, it does not wait for all the jobs to finish before populating the final results for the specified benchmark jobs

