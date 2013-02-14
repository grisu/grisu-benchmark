#!/usr/bin/env bash 

module load gromacs/4.5.4

echo "Started: `date +%s`" >> benchmark.log

/home/mbin029/src/cluster-scripts/gromacs/gromacs_workflow.sh -d . -i 4a55new_loopall_p110a_after_equilmd_pme.gro -t 4a55new_loopall_p110a.top -mpi -tuned

echo "Finished: `date +%s`" >> benchmark.log
