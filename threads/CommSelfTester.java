package nachos.threads;

import nachos.machine.Lib;

public class CommSelfTester {

	/**
	 * Test with 1 listener then 1 speaker.
	 */
	public static void selfTest1() {

		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();

		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();


	}  //selfTest1()

	/**
	 * Test with 1 speaker then 1 listener.
	 */
	public static void selfTest2() {

		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();

		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();


	}  //selfTest2()

	/**
	 * Test with 2 speakers and 2 listeners intermixed.
	 */
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

	/**
	 * Second test with 2 speakers and 2 listeners intermixed.
	 */
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


	/**
	 * Stress test with 100 speakers and 100 listeners intermixed.
	 */
	public static void selfTest5() {

		for (int i = 0; i < 100; i++) {
			new KThread(speakerRun).setName("Speaker "
											+ Integer.toString(i)).fork();
			
			new KThread(listenRun).setName("Listen " 
										   + Integer.toString(i)).fork();
		}


	}  //selfTest5()


	static void listenFunction()
	{
		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
				  + " is about to listen");
		
		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName()
				  + " got value "+ myComm.listen());  
		
	}  //listenFunction()

	static void speakFunction() {
		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
				  + " is about to speak");
		
		myComm.speak(myWordCount++);
		
		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() 
				  + " has spoken");  
	}  //speakFunction()


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
	private static Communicator myComm = new Communicator();
	private static int myWordCount = 0;

}  //CommSelfTester class
