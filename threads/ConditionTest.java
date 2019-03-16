package nachos.threads;

import nachos.machine.Lib;

public class ConditionTest {
// create new threads
	public static void smallThread() {
		KThread smallThread = new KThread(smallRunner);
		smallThread.setName("small threads");
		smallThread.fork();
	}
	public static void bigThreads() {
		KThread bigThreads = new KThread(bigRunner);
		bigThreads.setName("many threads");
		bigThreads.fork();
	}
// test with 5 threads
	public static void smallThreader() {
		
		for (int i = 0; i < 5; i++) {
			new KThread(smallRunner).setName("small threads " + Integer.toString(i + 1)).fork();
		}
	
	}
// test with 100 threads
	public static void bigThread() {
		
		for (int i = 0; i < 100; i++) {
			new KThread(bigRunner).setName("lotta threads " + Integer.toString(i + 1)).fork();
		}
	
	}
	
	static void printSmall(){

	Lib.debug(dbg, "Thread shows " + KThread.currentThread().getName());
			
	}

	static void printLarge(){

	Lib.debug(dbg, "Thread shows " + KThread.currentThread().getName());
			
	}

	private static Runnable smallRunner = new Runnable() {
	public void run() {
		printSmall();
	}
}; 

	private static Runnable bigRunner = new Runnable() {
	public void run() {
		printLarge();
	}
}; 
// debug outputs may need to be fixed.

	private static final char dbg = 'test';

}

