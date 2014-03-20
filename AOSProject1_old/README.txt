CS6378.003 AOS PROJECT 1
FIDGE-MATTERN VECTOR CLOCK IMPLEMENTATION
SUBMITTED BY
LAKSHMI SHANTI DUDDU
lxd121230@utdallas.edu

STEPS TO EXECUTE

STEP-1: OPEN required number of windows
	for example: ssh net01.utdallas.edu

STEP-2: cd AOSProject1. (AOSProject1 needs to be the cwd)

STEP-3: TO COMPLIE:
	net01/AOSProject1>javac *.java

STEP-4: COPY any configfile to the directory AOSProject1
sample config looks like this:
5
0 net01.utdallas.edu 54490
1 net02.utdallas.edu 54445
2 net03.utdallas.edu 54445
3 net04.utdallas.edu 54445
4 net05.utdallas.edu 54445

STEP-5: TO RUN:java SctpVectorClock <Nodeid> <Filename.txt>
For example:
	net01/AOSProject1>java SctpVectorClock 0 ConfigFile.txt
	net02/AOSProject1>java SctpVectorClock 1 ConfigFile.txt
	net03/AOSProject1>java SctpVectorClock 2 ConfigFile.txt
	net04/AOSProject1>java SctpVectorClock 3 ConfigFile.txt
	net05/AOSProject1>java SctpVectorClock 4 ConfigFile.txt