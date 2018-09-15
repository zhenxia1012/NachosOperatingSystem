package jnachos.kern;

import jnachos.filesystem.OpenFile;
import jnachos.machine.Machine;

public class UserPro implements VoidFunctionPtr {
	
	public void call(Object pArg) {
		System.out.println("*** " + JNachos.getCurrentProcess().getName() + " ***");
		
		String filename = (String)pArg;
		JNachos.startProcess(filename);
	}
	
	public UserPro(String strFiles) {
		Debug.print('t', "Entering User Program");
		
		String[] files = strFiles.split(",");
		for (int i = 0; i < files.length; i++) {
			NachosProcess p = new NachosProcess(JNachos.getCurrentProcess().getName() + JNachos.getCurrentProcess().getPid() + "'s Child Process");
			//System.out.println("Executable file: " + files[i]);
			p.fork(this, new String(files[i]));
		}
	}
}
