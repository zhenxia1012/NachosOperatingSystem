# NachosOperatingSystem


## Description
Nachos Operating System simulates the functionality of a normal Linux operating system. It has memory, register, swap space and so on. 
* It could run multiple C programs on a single processor based on FCFS process scheduling algorithm.
* It implemented the system calls like fork, join, exec, exit.
* It main virtual memory and swap space, handled page faults by LRU cache.
* It implemented communication between processes through message queue.

## Sub-Project

### Sub-project1
------------
* Implemented execution of user-level program wrapped in C source code on Java platform.
* Implemented execution of multiple programs on a single processor based on FCFS process scheduling algorithm, by designing the class "Userpro" which extracted files from user input and create a new process for each file, then appending them to the ready list.
* Implemented system calls "fork", "join", "exec", "exit".

### Sub-project2
------------
* Designed virtual memory, handled page fault and designed page replacement based on LRU. 
* Built a Swap Space to supply pages to main memory

### Sub-project3
------------
* Designed Nucleus Message Passing in Nachos System for communication between processes 
* int SendMsg(char* receiver, char* msg): An asynchronized system call. Sender process specifies the name of the receiver process and deliveries it a message via a system buffer. This system call returns the buffer ID;
*	int WaitMsg(char* sender, char* msg): A synchronized system call. Receiver process specifies the name of the sender process and receive the message sent by the in-out argument, "char* msg".
*	int SendAnswer(char* answer, int BufferId): An asynchronized system call. This system call is called by receiver program. The receiver replies the sender with an "answer" via the system buffer, which is assigned by "BufferId". If answer is successfully delivered, return 1 to indicate success, else return 0.
* int WaitAnswer(char* answer, int BufferId): A synchronized system call. Sender waits the answer sent by the receiver or the system arrives at the buffer identified by "BufferId". If answer sent by system, return 0, else return 1;

