package nachos.threads;

import nachos.machine.Lib;

public class Condition2SelfTester {

	/**
	 * Test with 1 sleeper then 1 waker.
	 */
	public static void selfTest1() {

		KThread sleeper1 = new KThread(sleepRun);
		sleeper1.setName("Sleeper");
		sleeper1.fork();

		KThread waker1 = new KThread(wakeRun);
		waker1.setName("Waker");
		waker1.fork();
	} // selfTest1()

	/**
	 * Test with 5 sleepers then 1 waker.
	 */
	public static void selfTest2() {

		for (int i = 1; i <= 5; i++) {
			new KThread(sleepRun).setName("Sleeper " + Integer.toString(i)).fork();
		} // end of for loop

		KThread waker1 = new KThread(wakeRun);
		waker1.setName("Waker");
		waker1.fork();

	} // selfTest1()

	/**
	 * Function for a sleeping thread runnable object.
	 */
	static void sleepFunction() {
		Lib.debug(dbgThread,
				"\n Thread " + KThread.currentThread().getName() + " is about to wake all and sleep on Condition2 \n");

		lock.acquire();
		sleepers_not_done++;
		cond_2.wakeAll();
		cond_2.sleep();

		Lib.debug(dbgThread, "\n Thread " + KThread.currentThread().getName() + " was woken by another thread");

		sleepers_not_done--;

		Lib.debug(dbgThread, "\n Thread " + KThread.currentThread().getName()
				+ " will decrement and wake any other sleeper or wake threads and finish \n");

		cond_2.wakeAll();
		lock.release();

		KThread.finish();

	} // sleepFunction()

	/**
	 * Function for a waking thread runnable object.
	 */
	static void wakeFunction() {
		Lib.debug(dbgThread,
				"\n Thread " + KThread.currentThread().getName() + " is about to wake one Condition2 thread \n");

		lock.acquire();
		cond_2.wake();

		while (sleepers_not_done > 0) {
			Lib.debug(dbgThread, "\n There are " + sleepers_not_done + " sleepers still not done so thread "
					+ KThread.currentThread().getName() + " is going to wake all and sleep on Condition2 \n");
			cond_2.wakeAll();
			cond_2.sleep();

			Lib.debug(dbgThread, "\n Thread " + KThread.currentThread().getName() + " was woken by another thread \n");
		}

		Lib.debug(dbgThread,
				" All sleepers are done so thread " + KThread.currentThread().getName() + " is about to finish \n");

		lock.release();
		KThread.finish();

	} // wakeFunction()

	/**
	 * Wraps sleepFunction inside a Runnable object so threads can be generated for
	 * testing.
	 */
	private static Runnable sleepRun = new Runnable() {
		public void run() {
			sleepFunction();
		}
	}; // runnable sleepRun

	/**
	 * Wraps wakeFunction inside a Runnable object so threads can be generated for
	 * testing.
	 */
	private static Runnable wakeRun = new Runnable() {
		public void run() {
			wakeFunction();
		}
	}; // runnable wakeRunRun

	// dbgThread = 't' variable needed for debug output
	private static final char dbgThread = 't';

	// lock for condition variable and to maintain atomicity
	private static Lock lock = new Lock();

	// condition variable for listeners
	private static Condition2 cond_2 = new Condition2(lock);

	// tracking sleeping threads that are not yet done waking
	private static int sleepers_not_done = 0;
} // class Cond2SelfTester
