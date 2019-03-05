package nachos.threads;

import nachos.machine.Lib;

public class KThreadJoinSelfTester {

	/**
	 * Test where thread tries to join on itself.
	 */
	public static void selfTest1() {

	KThread self_joining_thread = new KThread(joinSelfRun);
	self_joining_thread.setName("self_joining_thread");
	self_joining_thread.fork();

	}  //selfTest1()	
	
	/**
	 * Test where thread spawns a ping test and joins on it after it finishes.
	 */
	public static void selfTest2() {

	KThread join_on_finished_thread = new KThread(finishRun);
	join_on_finished_thread.setName("join_on_finished_thread");
	join_on_finished_thread.fork();

	}  //selfTest2()
	
	
	/**
	 * Test where thread spawns a ping test and joins on it.
	 */
	public static void selfTest3() {

	KThread joining_thread = new KThread(joinRun);
	joining_thread.setName("joining_thread");
	joining_thread.fork();

	}  //selfTest3()
	
	/**
	 * selfTest1: Creates a runnable obj for joinSelf().
	 */
	private static Runnable joinSelfRun = new Runnable() {
	public void run() {
		joinSelf();
	}
	};  //Runnable joinSelfRun
	
	/**
	 * selfTest2: Creates a runnable obj for finishAndJoin().
	 */
	private static Runnable finishRun = new Runnable() {
	public void run() {
		finishAndJoin();
	}
	};  //Runnable finishRun
	
	/**
	 * selfTest3: Creates a runnable obj for spawnAndJoin().
	 */
	private static Runnable joinRun = new Runnable() {
	public void run() {
		spawnAndJoin();
	}
	};  //Runnable joinRun
	
	/**
	 * selfTest3: Creates a runnable obj for pingTestRunner().
	 */
	private static Runnable pingTestRun = new Runnable() {
	public void run() {
		pingTestRunner();
	}
	};  //Runnable pingTestRun
	
	/**
	 * selfTest1: Function that runs in runnable obj to try to join on itself.
	 */
	static void joinSelf() {
	Lib.debug(dbgThread, "The " + KThread.currentThread().getName() 
			  + " is going to try to join on itself");
	try {
	KThread.currentThread().join();
	}
	catch (Throwable e) {
		Lib.debug(dbgThread, KThread.currentThread().getName() 
				  + " correctly failed to join itself");
	}
	
	}  //joinSelf()
	
	/**
	 * selfTest2: Function that runs in runnable obj to wait on ping test.
	 * Thread will create a ping test thread using pingTestRun
	 * and then join on it. Then try to join on it again.
	 */
	static void finishAndJoin() {
	Lib.debug(dbgThread, "The " + KThread.currentThread().getName() 
			  + " is about to spawn a ping_test thread");
	
	KThread ping_test = new KThread(pingTestRun);
	ping_test.setName("ping_test");
	ping_test.fork();
	
	Lib.debug(dbgThread, "The " + KThread.currentThread().getName() 
			  + " has spawned " + ping_test.getName() + " and will join on it"); 
	
	ping_test.join();
	
	Lib.debug(dbgThread, "The " + KThread.currentThread().getName() 
			  + " has resumed control and will try to join on ping_test again");
	
	ping_test.join();
	}  //finishAndJoin()
	
	/**
	 * selfTest3: Function that runs in runnable obj to wait on ping test.
	 * Thread will create a ping test thread using pingTestRun
	 * and then join on it.
	 */
	static void spawnAndJoin() {
	Lib.debug(dbgThread, "The " + KThread.currentThread().getName() 
			  + " is about to spawn a ping_test thread");
	
	KThread ping_test = new KThread(pingTestRun);
	ping_test.setName("ping_test");
	ping_test.fork();
	
	Lib.debug(dbgThread, "The " + KThread.currentThread().getName() 
			  + " has spawned " + ping_test.getName() + " and will join on it"); 
	
	ping_test.join();
	
	Lib.debug(dbgThread, "The " + KThread.currentThread().getName() 
			  + " has resumed control ");
	}  //spawnAndJoin()
	
	/**
	 * selfTest2 & 3: Function that runs in runnable obj.
	 * Runs a loop that announces index and yields. Other threads will wait for
	 * this to finish when they join on it.
	 */
	static void pingTestRunner() {
	Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
			  + " is about to run pingTest");
	
	for (int i=0; i<5; i++) { // You may adjust the number of iterations
		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
				  + " looped " + (i + 1) + " times");
        KThread.yield();
    }
	
	Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
			  + " has ended pingTest");  
	}  //pingTestRunner()
	

	
	//dbgThread = 't' variable needed for debug output
	private static final char dbgThread = 't';
}  //KThreadJoinSelfTester class
