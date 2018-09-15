package jnachos.kern;

import java.util.Arrays;

import jnachos.machine.MMU;
import jnachos.machine.Machine;
import jnachos.machine.TranslationEntry;

public class PageFaultHandler {

	public static void handlePageFault() {
		//JNachos.numOfPageFault++;
		int virtAddr = Machine.readRegister(Machine.BadVAddrReg);
		int vpn = (int) virtAddr / Machine.PageSize;
		int curProcess = JNachos.getCurrentProcess().getPid();
		System.out.println("\nProcess" + curProcess + " Bad virtual address: " + virtAddr + ", virtual page: " + vpn);
		//System.out.println("Number of clear pages in memory: " + AddrSpace.mFreeMap.numClear());
		
		int freePPN = AddrSpace.mFreeMap.find();
		if (freePPN < 0) {
			// there is no free page frame in memory, then using FIFO to kick out a victim
			// page
			// and fetch the target page
			// find a victim page
			int[] victim = JNachos.pageRamList.removeFirst();
			System.out.println("Memory is full, kicking out Process" + victim[0] + " virtual page: " + victim[1]);
			NachosProcess victimProcess = JNachos.mProcesses.get(victim[0]);
			TranslationEntry[] victimPageTable = victimProcess.getSpace().getPageTable();
			
			// whether the victim page is revised
			if (victimPageTable[victim[1]].dirty == true) {
				System.out.println("Update Process" + victim[0] + " Swap Space");
				byte[] bytes = new byte[Machine.PageSize];
				System.arraycopy(Machine.mMainMemory, victimPageTable[victim[1]].physicalPage * Machine.PageSize, bytes, 0, Machine.PageSize);
				JNachos.mSwapSpace.writeAt(bytes, Machine.PageSize, victimPageTable[victim[1]].SWSPPage * Machine.PageSize);
			}
			
			freePPN = victimPageTable[victim[1]].physicalPage;
			victimPageTable[victim[1]].physicalPage = -1;
			victimPageTable[victim[1]].valid = false;
			victimPageTable[victim[1]].use = false;
			victimPageTable[victim[1]].dirty = false;
		}
		
		// there is free page frame in memory, then writing the target page into memory
		MMU.mPageTable[vpn].physicalPage = freePPN;
		MMU.mPageTable[vpn].valid = true;
		MMU.mPageTable[vpn].use = true;
		MMU.mPageTable[vpn].dirty = false;
		MMU.mPageTable[vpn].readOnly = false;
		System.out.println("Writing into physical page: " + MMU.mPageTable[vpn].physicalPage);

		// Zero out all of main memory
		Arrays.fill(Machine.mMainMemory, MMU.mPageTable[vpn].physicalPage * Machine.PageSize, 
				(MMU.mPageTable[vpn].physicalPage + 1) * Machine.PageSize, (byte) 0);

		// Create a temporary buffer to fetch page content
		byte[] bytes1 = new byte[Machine.PageSize];

		// fetch the target page from Swap Space
		//System.out.println("Read from Process" + curProcess + " SWSPPage: " + MMU.mPageTable[vpn].SWSPPage);
		JNachos.mSwapSpace.readAt(bytes1, Machine.PageSize, MMU.mPageTable[vpn].SWSPPage * Machine.PageSize);

		// Copy the buffer into the main memory
		System.arraycopy(bytes1, 0, Machine.mMainMemory, MMU.mPageTable[vpn].physicalPage * Machine.PageSize, Machine.PageSize);

		// add target page into FIFO List
		//System.out.println("Add Process" + curProcess + " virtual page into Page List: " + vpn);
		int[] temPair = {curProcess, vpn};
		JNachos.pageRamList.addLast(temPair);
	}
}
