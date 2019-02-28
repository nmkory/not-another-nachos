package nachos.threads;

import nachos.machine.Lib;

public class ComSelfTester {

	/**
	 * Test if this module is working.
	 */
	public static void selfTest1() {

		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();

		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();


	}  //selfTest1()
	
	public static void selfTest2() {

		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();
		
		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();


	}  //selfTest2()
	
	public static void selfTest3() {

		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();
		
		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();
		
		KThread speaker2 = new KThread(speakerRun);
		speaker2.setName("speaker2");
		speaker2.fork();
		
		KThread listener2 = new KThread(listenRun);
		listener2.setName("listener2");
		listener2.fork();


	}  //selfTest3()
	
	public static void selfTest4() {

		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();
		
		KThread speaker2 = new KThread(speakerRun);
		speaker2.setName("speaker2");
		speaker2.fork();
		
		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();
		
		KThread listener2 = new KThread(listenRun);
		listener2.setName("listener2");
		listener2.fork();


	}  //selfTest4()
	
	
	public static void selfTest5() {
		
		for (int i = 0; i < 10; i++) {
			new KThread(speakerRun).setName("Speaker " + Integer.toString(i)).fork();
			new KThread(listenRun).setName("Listen " + Integer.toString(i)).fork();
		}


	}  //selfTest5()
	

	static void listenFunction()
	{
		Lib.debug(dbgThread,
				"Thread " + KThread.currentThread().getName() + " is about to listen");
		Lib.debug(dbgThread,
				"Thread " + KThread.currentThread().getName() + " got value " + myComms.listen());  
	}

	static void speakFunction() {
		Lib.debug(dbgThread,
				"Thread " + KThread.currentThread().getName() + " is about to speak");
		myComms.speak(myWordCount);
		myWordCount++;
		Lib.debug(dbgThread,
				"Thread " + KThread.currentThread().getName() + " has spoken");  
	}


	private static Runnable listenRun = new Runnable() {
		public void run() {
			listenFunction();
		}
	};
	private static Runnable speakerRun = new Runnable() {
		public void run() {
			speakFunction();
		}
	};
	
	private static final char dbgThread = 't';
	private static Communicator myComms = new Communicator();
	private static int myWordCount = 0;

}
