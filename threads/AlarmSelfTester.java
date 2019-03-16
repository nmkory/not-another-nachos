package nachos.threads;

import nachos.machine.Lib;
import nachos.machine.Machine;


public class AlarmSelfTester {

	/**
	 * selfTest1(): runs a PingTest() that sets an alarm and sleeps until woken 
	 * then runs a for loop
	 */
	private static class PingTest implements Runnable {

		public void run() {
			Lib.debug(dbgThread, "\n ***selfTest1 Alarm***");
			
			Lib.debug(dbgThread, "\n Time is: " + Machine.timer().getTime());
			
			testAlarm.waitUntil(400);
			
			Lib.debug(dbgThread, "\n Time is: " + Machine.timer().getTime());

			for (int i = 0; i < 10; i++) {
				Lib.debug(dbgThread, "\n " + KThread.currentThread().getName() 
						  + " is awake");
			}  //run()
		} // PingTest run()

	} // class PingTest

	/**
	 * Test if alarm works with just one object, should run long enough to not 
	 * try an empty sleepQueue
	 */
	public static void selfTest1() {

		new KThread(new PingTest()).setName("ping").fork();

	}

	/**
	 * selfTest2(): one of multiple PingTest() that sets an alarm and sleeps 
	 * until woken at the appx same time, then runs a for loop
	 */
	private static class PingTest2 implements Runnable {

		public void run() {
			Lib.debug(dbgThread, "\n ***selfTest2 Alarm***");
			
			Lib.debug(dbgThread, "\n Time is: " + Machine.timer().getTime());
			
			testAlarm.waitUntil(400);
			
			Lib.debug(dbgThread, "\n Time is: " + Machine.timer().getTime());

			for (int i = 0; i < 10; i++) {
				Lib.debug(dbgThread, "\n " + KThread.currentThread().getName() 
						  + " is awake");
			}  //end of for loop
		}  //run()

	}  //class PingTest2

	/**
	 * Test if alarm works with multiple objects and check if 
	 * threads are sorted in order objects do go to sleep in order
	 */
	public static void selfTest2() {

		for (int i = 0; i < 5; i++) {
			new KThread(new PingTest2()).setName("ping" + i).fork();

		} //end of for loop

	}  //selfTest2()

	/**
	 * selfTest3(): Checking if threads with different sleep values are sorted 
	 * and woken in the right order
	 */
	private static class PingTest3 implements Runnable {

		public void run() {

			Lib.debug(dbgThread, "\n ***selfTest3 Alarm***");

			Lib.debug(dbgThread, "\n Time is: " + Machine.timer().getTime());

			self_test_3_x = self_test_3_x - 200;
			testAlarm.waitUntil(self_test_3_x);

			Lib.debug(dbgThread, "\n Time is: " + Machine.timer().getTime());

			for (int i = 0; i < 10; i++) {
				Lib.debug(dbgThread, "\n " + KThread.currentThread().getName()
						  + " is awake");

			}  // for loop end

		}  //run()

	}

	/**
	 * Test if alarm works with multiple objects and check if 
	 * threads that have different sleep times are sorted in order 
	 * and objects wake up in appropriate order
	 */
	public static void selfTest3() {

		for (int i = 0; i < 5; i++) {
			new KThread(new PingTest3()).setName("ping" + i).fork();

		}

	}

	// dbgThread = 't' variable needed for debug output
	private static final char dbgThread = 't';

	// static variable for selfTest3()
	private static long self_test_3_x = 1400;

	// one shared alarm for selfTest
	private static Alarm testAlarm = new Alarm();
}