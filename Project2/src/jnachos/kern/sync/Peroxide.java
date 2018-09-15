/**
 *  <MakePeroxide Problem>
 *  Author: Jae C. Oh
 *  
 *  Created by Patrick McSweeney on 12/17/08.
 */
package jnachos.kern.sync;

import java.io.*;
import jnachos.kern.*;

/**
 *
 */
public class Peroxide {

	/** Semaphore H */
	static Semaphore H = new Semaphore("SemH", 0);

	/**	*/
	static Semaphore O = new Semaphore("SemO", 0);

	/**	*/
	static Semaphore HO = new Semaphore("SemHO", 0);
	
	/**	*/
	static Semaphore waitH = new Semaphore("waitH", 0);
	
	/**	*/
	static Semaphore waitO = new Semaphore("waitO", 0);

	/**	*/
	static Semaphore mutex = new Semaphore("MUTEX", 1);

	/**	*/
	static Semaphore mutex1 = new Semaphore("MUTEX1", 1);

	/**	*/
	static long countH = 0, countO = 0;

	/**	*/
	static int Hcount, Ocount, nH, nO;

	/**	*/
	class HAtom implements VoidFunctionPtr {
		int mID;

		/**
		 *
		 */
		public HAtom(int id) {
			mID = id;
		}

		/**
		 * oAtom will call oReady. When this atom is used, do continuous
		 * "Yielding" - preserving resource
		 */
		public void call(Object pDummy) {
			mutex.P();
			if (countH % 2 == 0) // first H atom
			{
				countH++; // increment counter for the first H
				mutex.V(); // Critical section ended
				//System.out.println("H atom #" + mID + " wait for second H atom");
				H.P(); // Waiting for the second H atom
			} else // second H atom
			{
				countH++; // increment count for next first H
				mutex.V(); // Critical section ended
				//System.out.println("H atom #" + mID + " wake up first H atom");
				H.V(); // wake up the first H atom
				//System.out.println("H atom #" + mID + " wake up second O atom");
				HO.V(); // wake up the second O atom
			}

			//System.out.println("H atom #" + mID + " wait for peroxide message done");
			waitH.P(); // wait for peroxide message done

			System.out.println("H atom #" + mID + " used in making peroxide.");
		}
	}

	/**	*/
	class OAtom implements VoidFunctionPtr {
		int mID;

		/**
		 * oAtom will call oReady. When this atom is used, do continuous
		 * "Yielding" - preserving resource
		 */
		public OAtom(int id) {
			mID = id;
		}

		/**
		 * oAtom will call oReady. When this atom is used, do continuous
		 * "Yielding" - preserving resource
		 */
		public void call(Object pDummy) {
			mutex1.P();
			if (countO % 2 == 0) {
				countO++; // increment counter for the first O
				mutex1.V(); // Critical section ended
				//System.out.println("O atom #" + mID + " wait for second O atom");
				O.P(); // Waiting for the second O atom
				//System.out.println("O atom #" + mID + " wait for peroxide message done");
				waitO.P(); // wait for peroxide message done
			}
			else {
				countO++;
				mutex1.V();
				//System.out.println("O atom #" + mID + " wake up first O atom");
				O.V(); // wake up the first O atom
				//System.out.println("O atom #" + mID + " wait for second H atom");
				HO.P(); // waiting for the second H atom.
				makePeroxide();
				waitH.V(); // wake up the H atom
				waitH.V(); // wake up the H atom
				waitO.V(); // wake up the O atom
				mutex1.P();
				Hcount = Hcount - 2;
				Ocount = Ocount - 2;
				System.out.println("Numbers Left: H Atoms: " + Hcount + ", O Atoms: " + Ocount);
				System.out.println("Numbers Used: H Atoms: " + (nH - Hcount) + ", O Atoms: " + (nO - Ocount));
				mutex1.V();
			}
			
			System.out.println("O atom #" + mID + " used in making peroxide.");
		}
	}

	/**
	 * oAtom will call oReady. When this atom is used, do continuous "Yielding"
	 * - preserving resource
	 */
	public static void makePeroxide() {
		System.out.println("** Peroxide made! Splash!! **");
	}

	/**
	 * oAtom will call oReady. When this atom is used, do continuous "Yielding"
	 * - preserving resource
	 */
	public Peroxide() {
		runPeroxide();
	}

	/**
	 *
	 */
	public void runPeroxide() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Number of H atoms ? ");
			nH = (new Integer(reader.readLine())).intValue();
			System.out.println("Number of O atoms ? ");
			nO = (new Integer(reader.readLine())).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Hcount = nH;
		Ocount = nO;

		for (int i = 0; i < nH; i++) {
			HAtom atom = new HAtom(i);
			(new NachosProcess(new String("hAtom" + i))).fork(atom, null);
		}

		for (int j = 0; j < nO; j++) {
			OAtom atom = new OAtom(j);
			(new NachosProcess(new String("oAtom" + j))).fork(atom, null);
		}
	}
}
