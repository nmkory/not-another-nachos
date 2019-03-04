package nachos.threads;

import nachos.machine.Lib;

public class KThreadJoinSelfTester {

	
	public static void selfTest1() {

	KThread thread1 = new KThread(joinRun);
	thread1.setName("thread1");
	thread1.fork();

	}  //selfTest1()
	
	static void spawnAndJoin() {
	Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
			  + " is about to spawn a pingTest thread");
	
	KThread pingTest = new KThread(pingTestRun);
	pingTest.setName("pingTest");
	pingTest.fork();
	
	Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
			  + " has spawned " + pingTest.getName() + " and will join on it"); 
	
	pingTest.join();
	
	Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
			  + " has resumed control ");
	}  //spawnAndJoin()
	
	static void pingTestRunner() {
	Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
			  + " is about to run pingTest");
	
	for (int i=0; i<5; i++) { // You may adjust the number of iterations
		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() + " looped " + i + " times");
        KThread.yield();
    }
	
	Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
			  + " has ended pingTest");  
	}  //pingTestRunner()
	
	
	private static Runnable pingTestRun = new Runnable() {
	public void run() {
		pingTestRunner();
	}
	};  //Runnable speakerRun
	
	private static Runnable joinRun = new Runnable() {
	public void run() {
		spawnAndJoin();
	}
	};  //Runnable speakerRun
	
	//dbgThread = 't' variable needed for debug output
	private static final char dbgThread = 't';
}  //KThreadJoinSelfTester class
