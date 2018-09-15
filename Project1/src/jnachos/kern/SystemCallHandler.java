/**
 * Copyright (c) 1992-1993 The Regents of the University of California.
 * All rights reserved.  See copyright.h for copyright notice and limitation 
 * of liability and disclaimer of warranty provisions.
 *  
 *  Created by Patrick McSweeney on 12/5/08.
 */
package jnachos.kern;

import java.util.Iterator;

import jnachos.filesystem.OpenFile;
import jnachos.machine.*;

/** The class handles System calls made from user programs. */
public class SystemCallHandler {
	/** The System call index for halting. */
	public static final int SC_Halt = 0;

	/** The System call index for exiting a program. */
	public static final int SC_Exit = 1;

	/** The System call index for executing program. */
	public static final int SC_Exec = 2;

	/** The System call index for joining with a process. */
	public static final int SC_Join = 3;

	/** The System call index for creating a file. */
	public static final int SC_Create = 4;

	/** The System call index for opening a file. */
	public static final int SC_Open = 5;

	/** The System call index for reading a file. */
	public static final int SC_Read = 6;

	/** The System call index for writting a file. */
	public static final int SC_Write = 7;

	/** The System call index for closing a file. */
	public static final int SC_Close = 8;

	/** The System call index for forking a forking a new process. */
	public static final int SC_Fork = 9;

	/** The System call index for yielding a program. */
	public static final int SC_Yield = 10;

	/**
	 * Entry point into the Nachos kernel. Called when a user program is
	 * executing, and either does a syscall, or generates an addressing or
	 * arithmetic exception.
	 * 
	 * For system calls, the following is the calling convention:
	 * 
	 * system call code -- r2 arg1 -- r4 arg2 -- r5 arg3 -- r6 arg4 -- r7
	 * 
	 * The result of the system call, if any, must be put back into r2.
	 * 
	 * And don't forget to increment the pc before returning. (Or else you'll
	 * loop making the same system call forever!
	 * 
	 * @pWhich is the kind of exception. The list of possible exceptions are in
	 *         Machine.java
	 **/
	public static void handleSystemCall(int pWhichSysCall) {

		Debug.print('a', "!!!!" + Machine.read1 + "," + Machine.read2 + "," + Machine.read4 + "," + Machine.write1 + ","
				+ Machine.write2 + "," + Machine.write4);

		switch (pWhichSysCall) {
		// If halt is received shut down
		case SC_Halt:
			Debug.print('a', "Shutdown, initiated by user program.");
			Interrupt.halt();
			break;

		case SC_Exit:
			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);
			
			// Read in any arguments from the 4th register
			int arg = Machine.readRegister(4);

			System.out.println("Current Process " + JNachos.getCurrentProcess().getName() + " exiting with code " + arg);

			// check register for whether there is a waiting process
			int waited_epid = JNachos.getCurrentProcess().getPid();
			
			// whether the current process has waiting process
			if (JNachos.mWaitingList.containsKey(waited_epid)) {
				int waiting_epid = (int)JNachos.mWaitingList.get(waited_epid);
				
				// Remove the pair<waited_pid, waiting_pid> form waiting list
				JNachos.mWaitingList.remove(waited_epid);
				
				// save the input of the EXIT system call
				NachosProcess waiting_eprocess = JNachos.mProcesses.get(waiting_epid);
				waiting_eprocess.saveUserRegister(2, arg);
				
				// recover the waiting process
				System.out.println("Recover the waiting Process" + waiting_epid + " from waited Process" + waited_epid);
				Scheduler.readyToRun(waiting_eprocess);
			}
			
			// Remove the current process form running process table
			JNachos.mProcesses.remove(waited_epid);
			
			// Finish the invoking process
			JNachos.getCurrentProcess().finish();
			break;
			
		case SC_Fork:
			System.out.println("Process" + JNachos.getCurrentProcess().getPid() + " Enter System Call SC_Fork");
			
			// Turn off interrupts
			boolean oldLevel_fork = Interrupt.setLevel(false);
			
			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);
			
			// create a new child process and set and store the return value for parent process
			NachosProcess child = new NachosProcess(JNachos.getCurrentProcess().getName() + JNachos.getCurrentProcess().getPid() + "'s Child Process");
			Machine.writeRegister(2, 0);
			
			// save current state of parent process
			AddrSpace space_fork = new AddrSpace(JNachos.getCurrentProcess().getSpace());
			child.setSpace(space_fork);
			child.saveUserState();

			// set and store the return value for child process
			Machine.writeRegister(2, child.getPid());
			
			// put the child process into the ready-list
			child.fork(new NewProcess(), child);
			System.out.println("Process" + JNachos.getCurrentProcess().getPid() + " forked Process" + child.getPid());
			
			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_fork);
			break;
			
		case SC_Join:
			System.out.println("Process" + JNachos.getCurrentProcess().getPid() + " Enter System Call SC_Join");
			
			// Turn off interrupts
			boolean oldLevel_join = Interrupt.setLevel(false);
			
			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);
			
			// get the pid called process
			int waited_jpid = Machine.readRegister(4);
			int waiting_jpid = JNachos.getCurrentProcess().getPid();
			
			// the called process should exist, not be the current process, run
			if (waited_jpid == 0 || waited_jpid == waiting_jpid || !JNachos.mProcesses.containsKey(waited_jpid))
				break;
			
			// put calling process's pid into the Waiting List
			System.out.println("Process" + JNachos.getCurrentProcess().getPid() + " waits for Process" + waited_jpid);
			JNachos.mWaitingList.put(waited_jpid, waiting_jpid);
			
			// sleep the calling process
			JNachos.getCurrentProcess().sleep();
			
			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_join);
			break;
			
		case SC_Exec:
			System.out.println("Process" + JNachos.getCurrentProcess().getPid() + " Enter System Call SC_Exec");
			
			// PCReg + 4
			Machine.writeRegister(Machine.PCReg, Machine.readRegister(Machine.PCReg) + 4);
			
			// Turn off interrupts
			boolean oldLevel_exec = Interrupt.setLevel(false);
			
			// read address of string path of the executable file
			int addr = Machine.readRegister(4);
			// look for the string path from the simulated memory
			String filename = new String();
			int word = Machine.readMem(addr, 1);
			while ((char)word != '\0') {
				filename += (char)word;
				addr++;
				word = Machine.readMem(addr, 1);
			}
			System.out.println("Process" + JNachos.getCurrentProcess().getPid() + " executing file \"" + filename + "\"");
			
			// The executable file to run
			OpenFile executable = JNachos.mFileSystem.open(filename);

			// If the file does not exist
			if (executable == null) {
				Debug.print('t', "Unable to open file " + filename);
				System.out.println("Can not open file " + filename);
				return;
			}

			// Load the file into the memory space
			AddrSpace space = new AddrSpace(executable);
			JNachos.getCurrentProcess().setSpace(space);

			// set the initial register values
			space.initRegisters();

			// load page table register
			space.restoreState();

			// jump to the user progam
			// machine->Run never returns;
			Machine.run();
			
			// Return interrupts to their pre-call level
			Interrupt.setLevel(oldLevel_exec);
			break;

		default:
			Interrupt.halt();
			break;
		}
	}
}
