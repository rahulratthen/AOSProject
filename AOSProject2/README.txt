README
SUBMITTED BY
LAKSHMI SHANTI DUDDU
VISWAM NATHAN
RAHUL RATTEN

AIM - To create an application that implements Suzuki Kasami Algorithm to achieve mutual exclusion.

TO COMPILE:

Open the number of windows required

Set current working directory to netxx/AOSProject2/

netxx/AOSProject2/> javac *.java

TO RUN:

netxx/AOSProject2/> java Application <node-id> <config-file.txt>

For example:

net01/AOSProject2/> java Application 0 ConfigFile.txt 
net02/AOSProject2/> java Application 1 ConfigFile.txt
net03/AOSProject2/> java Application 2 ConfigFile.txt

Notes -
 
1.	Node 0 has to be started first since it has the token initially.
 	
2.	According to the algorithm the lowest node id has the token initially.

3.	Each node exits after servicing certain number of requests and prints to the screen if the algorithm works correctly or not by automatically checking the log file called "cstest.txt".

4.	The algorithm is implemented in file "Application.java"  