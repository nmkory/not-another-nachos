package nachos.threads;

import nachos.machine.Lib;
import nachos.machine.Machine;
import nachos.threads.LotteryScheduler.LotteryQueue;

public class LottSchedulerSelfTester {

	/* Basic Lottery Testing */

	/**
	 * A 15 Ticket Thread and a 5 Ticket Thread are put into a LotteryQueue.
	 * The 15 Ticket Thread should be picked 75% of the time.
	 * The 5 Ticket Thread should be picked 25% of the time.
	 */
	public static void selfTest1() {
		System.out.println("Basic Lottery Testing 1: 15 Tix Thread vs. 5 Tix Thread");
		
		// A thread that will "acquire" the LotteryQueue
		theThreadThatDoesStuff = new KThread(null);
		
		KThread Tix5Thread = new KThread(null);
		Tix5Thread.setName("5 Ticket Thread");
		changePriorityOfThread(Tix5Thread, 5);
		
		KThread Tix15Thread = new KThread(null);
		Tix15Thread.setName("15 Ticket Thread");
		changePriorityOfThread(Tix15Thread, 15);
		
		// Keep track of the times each thread was picked
		int Tix5Picked = 0, Tix15Picked = 0;
		
		// Disable interrupts
		boolean intStatus = Machine.interrupt().disable();
		
		for (int i = 0; i < numIterations; i++) {
			// Have theThreadThatDoesStuff acquire the LotteryQueue
			testQueue.acquire(theThreadThatDoesStuff);
			
			// Shove the other threads into the LotteryQueue
			testQueue.waitForAccess(Tix15Thread);
			testQueue.waitForAccess(Tix5Thread);
			
			// Assert that there are only 20 tickets in the Lottery
			Lib.assertTrue(testQueue.totalTickets == 20);
			
			// Pick a thread using the Lottery
			KThread winningThread = testQueue.nextThread();
			
			// Increment the counter for the winning thread
			if (winningThread.compareTo(Tix15Thread) == 0) {
				Tix15Picked++;
			}
			else if (winningThread.compareTo(Tix5Thread) == 0) {
				Tix5Picked++;
			}
			
			// Flush out the remaining threads in the Lottery
			while (testQueue.nextThread() != null) {
			}
			
			Lib.assertTrue(testQueue.totalTickets == 0);  // There should now be 0 tickets left in the lottery
		}
		
		// Restore interrupts
		Machine.interrupt().restore(intStatus);
		
		System.out.println("Final Results");
		System.out.println("5 Ticket Thread was picked " + Tix5Picked + "/" + numIterations + " times! (" + (float)(Tix5Picked) / numIterations * 100 + "%)");
		System.out.println("15 Ticket Thread was picked " + Tix15Picked + "/" + numIterations + " times! (" + (float)(Tix15Picked) / numIterations * 100 + "%)");
		System.out.println("");
	}
	
	/**
	 * A 15 Ticket Thread, a 5 Ticket Thread, and a 20 Ticket Thread are put into a LotteryQueue.
	 * On the first pull...
	 *   The 15 Ticket Thread should be picked 37.5% of the time.
	 *   The 5 Ticket Thread should be picked 12.5% of the time.
	 *   The 20 Ticket Thread should be picked 50% of the time.
	 * On the second pull...
	 *   5-Tix Won: 15-Tix should be picked ~42.86% (15/35) of the time while 20-Tix should be picked ~57.14% (20/35) of the time.
	 *   15-Tix Won: 5-Tix should be picked 20% of the time while 20-Tix should be picked 80% of the time.
	 *   20-Tix Won: 5-Tix should be picked 25% of the time while 15-Tix should be picked 75% of the time.
	 */
	public static void selfTest2() {
		System.out.println("Basic Lottery Testing 2: 15 Tix Thread vs. 5 Tix Thread vs. 20 Tix Thread");
		
		// A thread that will "acquire" the LotteryQueue
		theThreadThatDoesStuff = new KThread(null);
		
		KThread Tix5Thread = new KThread(null);
		Tix5Thread.setName("5 Ticket Thread");
		changePriorityOfThread(Tix5Thread, 5);
		
		KThread Tix15Thread = new KThread(null);
		Tix15Thread.setName("15 Ticket Thread");
		changePriorityOfThread(Tix15Thread, 15);
		
		KThread Tix20Thread = new KThread(null);
		Tix20Thread.setName("20 Ticket Thread");
		changePriorityOfThread(Tix20Thread, 20);
		
		// Keep track of the times each thread was picked on the first pull
		int first_pull_tix5 = 0, first_pull_tix15 = 0, first_pull_tix20 = 0;
		
		// Second pull statistics
		int fp5_tix15 = 0, fp5_tix20 = 0, fp15_tix5 = 0, fp15_tix20 = 0, fp20_tix5 = 0, fp20_tix15 = 0;
		
		// Useful IDs for coordinating the second pull
		final int TIX15_WINNER = 0, TIX5_WINNER = 1, TIX20_WINNER = 2;
		
		// Disable interrupts
		boolean intStatus = Machine.interrupt().disable();
		
		for (int i = 0; i < numIterations; i++) {
			// Have theThreadThatDoesStuff acquire the LotteryQueue
			testQueue.acquire(theThreadThatDoesStuff);
			
			// Shove the other threads into the LotteryQueue
			testQueue.waitForAccess(Tix15Thread);
			testQueue.waitForAccess(Tix5Thread);
			testQueue.waitForAccess(Tix20Thread);
			
			// Assert that there are currently 40 tickets in the Lottery
			Lib.assertTrue(testQueue.totalTickets == 40);
			
			// Pick a thread using the Lottery
			KThread winningThread = testQueue.nextThread();
			int winningID = -1;
			
			// Increment the counter for the winning thread
			if (winningThread.compareTo(Tix15Thread) == 0) {
				first_pull_tix15++;
				winningID = TIX15_WINNER;
			}
			else if (winningThread.compareTo(Tix5Thread) == 0) {
				first_pull_tix5++;
				winningID = TIX5_WINNER;
			}
			else if (winningThread.compareTo(Tix20Thread) == 0) {
				first_pull_tix20++;
				winningID = TIX20_WINNER;
			}
			
			// Do some assertions on the remaining totalTickets depending on the winner of the first pull
			if (winningID == TIX15_WINNER) {
				Lib.assertTrue(testQueue.totalTickets == 25);
			}
			else if (winningID == TIX5_WINNER) {
				Lib.assertTrue(testQueue.totalTickets == 35);
			}
			else if (winningID == TIX20_WINNER) {
				Lib.assertTrue(testQueue.totalTickets == 20);
			}
			
			// Now do a second pull
			KThread runnerUp = testQueue.nextThread();
			
			// Increment the corresponding 2nd pull variable depending on the winner of the first pull
			switch(winningID) {
			case TIX15_WINNER:  // 15-Tix Thread won the first pull...
				if (runnerUp.compareTo(Tix5Thread) == 0) {  // And 5-Tix Thread won the second pull...
					fp15_tix5++;
				}
				else if (runnerUp.compareTo(Tix20Thread) == 0) {  // And 20-Tix Thread won the second pull...
					fp15_tix20++;
				}
				break;
			case TIX5_WINNER:  // 5-Tix Thread won the first pull...
				if (runnerUp.compareTo(Tix15Thread) == 0) {  // And 15-Tix Thread won the second pull...
					fp5_tix15++;
				}
				else if (runnerUp.compareTo(Tix20Thread) == 0) {  // And 20-Tix Thread won the second pull...
					fp5_tix20++;
				}
				break;
			case TIX20_WINNER:  // 20-Tix Thread won the first pull...
				if (runnerUp.compareTo(Tix5Thread) == 0) {  // And 5-Tix Thread won the second pull...
					fp20_tix5++;
				}
				else if (runnerUp.compareTo(Tix15Thread) == 0) {  // And 15-Tix Thread won the second pull...
					fp20_tix15++;
				}
				break;
			}
			
			// Flush out the remaining threads in the Lottery
			while (testQueue.nextThread() != null) {
			}
			
			Lib.assertTrue(testQueue.totalTickets == 0);  // There should now be 0 tickets left in the lottery
		}
		
		// Restore interrupts
		Machine.interrupt().restore(intStatus);
		
		System.out.println("Final Results");
		System.out.println("5 Ticket Thread was picked first " + first_pull_tix5 + "/" + numIterations + " times! (" + (float)(first_pull_tix5) / numIterations * 100 + "%)");
		if (first_pull_tix5 > 0) {  // Print 2nd pull statistics for fp5, but only if tix5 was picked first at least once
			System.out.println("--> 2nd Pull Statistics (fp5)");
			System.out.println("--> 15 Ticket Thread was picked second " + fp5_tix15 + "/" + first_pull_tix5 + " times! (" + (float)(fp5_tix15) / first_pull_tix5 * 100 + "%)");
			System.out.println("--> 20 Ticket Thread was picked second " + fp5_tix20 + "/" + first_pull_tix5 + " times! (" + (float)(fp5_tix20) / first_pull_tix5 * 100 + "%)");
		}
		System.out.println("15 Ticket Thread was picked first " + first_pull_tix15 + "/" + numIterations + " times! (" + (float)(first_pull_tix15) / numIterations * 100 + "%)");
		if (first_pull_tix15 > 0) {  // Print 2nd pull statistics for fp15, but only if tix15 was picked first at least once
			System.out.println("--> 2nd Pull Statistics (fp15)");
			System.out.println("--> 5 Ticket Thread was picked second " + fp15_tix5 + "/" + first_pull_tix15 + " times! (" + (float)(fp15_tix5) / first_pull_tix15 * 100 + "%)");
			System.out.println("--> 20 Ticket Thread was picked second " + fp5_tix20 + "/" + first_pull_tix15 + " times! (" + (float)(fp15_tix20) / first_pull_tix15 * 100 + "%)");
		}
		System.out.println("20 Ticket Thread was picked first " + first_pull_tix20 + "/" + numIterations + " times! (" + (float)(first_pull_tix20) / numIterations * 100 + "%)");
		if (first_pull_tix20 > 0) {  // Print 2nd pull statistics for fp20, but only if tix20 was picked first at least once
			System.out.println("--> 2nd Pull Statistics (fp20)");
			System.out.println("--> 5 Ticket Thread was picked second " + fp20_tix5 + "/" + first_pull_tix20 + " times! (" + (float)(fp20_tix5) / first_pull_tix20 * 100 + "%)");
			System.out.println("--> 15 Ticket Thread was picked second " + fp20_tix15 + "/" + first_pull_tix20 + " times! (" + (float)(fp20_tix15) / first_pull_tix20 * 100 + "%)");
		}
		System.out.println("");
	}

	/* Ticket Donation Testing */
	
	/**
	 * A 5 Ticket Thread acquires a resource while a 10 Ticket and a 15 Ticket Thread wait for access in that order.
	 * When the 10-Tix Thread waits for access, the 5-Tix thread should return an effective priority of 15
	 * When the 15-Tix Thread waits for access, the 5-Tix thread should return an effective priority of 30
	 */
	public static void selfTest3() {
		System.out.println("Ticket Donation Testing 1: 1 Resource, Many Threads");
		
		KThread Tix5Thread = new KThread(null);
		Tix5Thread.setName("5 Ticket Thread");
		changePriorityOfThread(Tix5Thread, 5);
		
		KThread Tix10Thread = new KThread(null);
		Tix10Thread.setName("10 Ticket Thread");
		changePriorityOfThread(Tix10Thread, 10);
		
		KThread Tix15Thread = new KThread(null);
		Tix15Thread.setName("15 Ticket Thread");
		changePriorityOfThread(Tix15Thread, 15);
		
		// Disable interrupts
		boolean intStatus = Machine.interrupt().disable();
		
		donationQueue.acquire(Tix5Thread);
		System.out.println(Tix5Thread.getName() + " starts with an effective priority of " + ThreadedKernel.scheduler.getEffectivePriority(Tix5Thread));
		
		donationQueue.waitForAccess(Tix10Thread);
		System.out.println(Tix10Thread.getName() + " is now waiting for access. " + Tix5Thread.getName() + " now has an effective priority of " + ThreadedKernel.scheduler.getEffectivePriority(Tix5Thread));
		
		Lib.assertTrue(donationQueue.totalTickets == 10);  // There should now be 10 tickets in the lottery
		
		donationQueue.waitForAccess(Tix15Thread);
		System.out.println(Tix15Thread.getName() + " is now waiting for access. " + Tix5Thread.getName() + " now has an effective priority of " + ThreadedKernel.scheduler.getEffectivePriority(Tix5Thread));
		
		Lib.assertTrue(donationQueue.totalTickets == 25);  // There should now be 25 tickets in the lottery
		
		System.out.println("");
		
		while (donationQueue.nextThread() != null) {
		}  // Flush out the queues from the lottery
		
		// Restore interrupts
		Machine.interrupt().restore(intStatus);
		
	}
	
	/**
	 * A 5 Ticket Thread acquires Resource A. A 15 Ticket Thread acquires Resource B.
	 * The 15 Ticket Thread waits for access on Resource A thus giving the 5-Tix thread an effective priority of 20
	 * A 10 Ticket Thread waits for access on Resource B. This should give the 15-Tix thread an effective priority of 25
	 * This should also recursively give the 5-Tix Thread an effective priority of 30
	 */
	public static void selfTest3_Part2() {
		System.out.println("Ticket Donation Testing 2: Chain of resources");
		
		KThread Tix5Thread = new KThread(null);
		Tix5Thread.setName("5 Ticket Thread");
		changePriorityOfThread(Tix5Thread, 5);
		
		KThread Tix10Thread = new KThread(null);
		Tix10Thread.setName("10 Ticket Thread");
		changePriorityOfThread(Tix10Thread, 10);
		
		KThread Tix15Thread = new KThread(null);
		Tix15Thread.setName("15 Ticket Thread");
		changePriorityOfThread(Tix15Thread, 15);
		
		// Disable interrupts
		boolean intStatus = Machine.interrupt().disable();
		
		donationQueue.acquire(Tix5Thread);
		System.out.println("Resource A has been acquired by " + Tix5Thread.getName() + ". They start with an effective priority of " + ThreadedKernel.scheduler.getEffectivePriority(Tix5Thread));
		donationQueue2.acquire(Tix15Thread);
		System.out.println("Resource B has been acquired by " + Tix15Thread.getName() + ". They start with an effective priority of " + ThreadedKernel.scheduler.getEffectivePriority(Tix15Thread));
		
		donationQueue.waitForAccess(Tix15Thread);
		System.out.println(Tix15Thread.getName() + " is now waiting for access on Resource A. " + Tix5Thread.getName() + " now has an effective priority of " + ThreadedKernel.scheduler.getEffectivePriority(Tix5Thread));
		
		Lib.assertTrue(donationQueue.totalTickets == 15);  // There should now be 15 tickets in Resource A's lottery
		
		donationQueue2.waitForAccess(Tix10Thread);
		System.out.println(Tix10Thread.getName() + " is now waiting for access on Resource B. " + Tix15Thread.getName() + " now has an effective priority of " + ThreadedKernel.scheduler.getEffectivePriority(Tix15Thread));
		
		Lib.assertTrue(donationQueue2.totalTickets == 10);  // There should now be 10 tickets in Resource B's lottery
		
		System.out.println("By extension, " + Tix5Thread.getName() + " now has an effective priority of " + ThreadedKernel.scheduler.getEffectivePriority(Tix5Thread));
		System.out.println("");
		
		Lib.assertTrue(donationQueue.totalTickets == 25);  // The donation should have reached through to Resource A and increased the ticket count to 25
		
		// Restore interrupts
		Machine.interrupt().restore(intStatus);
		
	}
	
	/*
	 * A 5 Ticket Thread acquires a resource, then a 10 Ticket Thread waits for access
	 * The 5 Ticket Thread is then put into a LotteryQueue with a 15 Ticket Thread
	 * Both the 5 Ticket Thread and the 15 Ticket Thread should be picked 50% of the time
	 */
	public static void selfTest4() {
		System.out.println("Ticket Donation Testing 3: 5+10 Tix Thread vs 15 Tix Thread");
		
		theThreadThatDoesStuff = new KThread(null);
		
		KThread Tix5Thread = new KThread(null);
		Tix5Thread.setName("5 Ticket Thread");
		changePriorityOfThread(Tix5Thread, 5);
		
		KThread Tix10Thread = new KThread(null);
		Tix10Thread.setName("10 Ticket Thread");
		changePriorityOfThread(Tix10Thread, 10);
		
		KThread Tix15Thread = new KThread(null);
		Tix15Thread.setName("15 Ticket Thread");
		changePriorityOfThread(Tix15Thread, 15);

		// Disable interrupts
		boolean intStatus = Machine.interrupt().disable();
		
		donationQueue.acquire(Tix5Thread);
		donationQueue.waitForAccess(Tix10Thread);
		
		// Keep track of the times each thread was picked
		int Tix5Picked = 0, Tix15Picked = 0;

		for (int i = 0; i < numIterations; i++) {
			// Have theThreadThatDoesStuff acquire the LotteryQueue
			testQueue.acquire(theThreadThatDoesStuff);

			// Shove the other threads into the LotteryQueue
			testQueue.waitForAccess(Tix15Thread);
			testQueue.waitForAccess(Tix5Thread);

			// Due to the ticket donation, there should be 30 tickets in the lottery
			Lib.assertTrue(testQueue.totalTickets == 30);

			// Pick a thread using the Lottery
			KThread winningThread = testQueue.nextThread();

			// Increment the counter for the winning thread
			if (winningThread.compareTo(Tix15Thread) == 0) {
				Tix15Picked++;
			}
			else if (winningThread.compareTo(Tix5Thread) == 0) {
				Tix5Picked++;
			}

			// Flush out the remaining threads in the Lottery
			while (testQueue.nextThread() != null) {
			}
		}

		// Restore interrupts
		Machine.interrupt().restore(intStatus);

		System.out.println("Final Results");
		System.out.println("5 Ticket Thread was picked " + Tix5Picked + "/" + numIterations + " times! (" + (float)(Tix5Picked) / numIterations * 100 + "%)");
		System.out.println("15 Ticket Thread was picked " + Tix15Picked + "/" + numIterations + " times! (" + (float)(Tix15Picked) / numIterations * 100 + "%)");
		System.out.println("");
	}
	
	/**
	 * A function that makes a thread do some stuff This is literally just a ping
	 * test
	 */
	static void doStuff(int numIterations) {
		for (int i = 0; i < numIterations; i++) {
			Lib.debug(dbgThread, "Doing stuff..." + i * 100 / numIterations + "% done");
			KThread.yield();
		}
		Lib.debug(dbgThread, "I have finished doing stuff!");
	}

	/**
	 * A function for getting the priority of a specific thread
	 */
	static void getPriorityOfThread(KThread thread) {
		boolean intStatus = Machine.interrupt().disable();

		Lib.debug(dbgThread, "Priority of " + thread.getName() + " is " + ThreadedKernel.scheduler.getPriority(thread));

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * A function for getting the effective priority of a specific thread
	 */
	static void getEffectivePriorityOfThread(KThread thread) {
		boolean intStatus = Machine.interrupt().disable();

		Lib.debug(dbgThread, "Effective Priority of " + thread.getName() + " is "
				+ ThreadedKernel.scheduler.getEffectivePriority(thread));

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * A function for manually changing the priority of a specific thread
	 */
	static void changePriorityOfThread(KThread thread, int priority) {
		Lib.debug(dbgThread, "Manually changing the priority of " + thread.getName() + " to " + priority);

		boolean intStatus = Machine.interrupt().disable();

		ThreadedKernel.scheduler.setPriority(thread, priority);
		Lib.debug(dbgThread, "Done changing the priority of " + thread.getName() + ". Their effective priority is now "
				+ ThreadedKernel.scheduler.getEffectivePriority(thread));

		Machine.interrupt().restore(intStatus);
	}

	/**
	 * A Runnable for a thread to do some stuff immediately. Intended for
	 * theThreadThatDoesStuff
	 */
	private static Runnable immediatelyDoStuff = new Runnable() {
		public void run() {
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to do stuff!");
			doStuff(10);
		}
	};

	private static final char dbgThread = 't';
	private static final int numIterations = 1000000;
	private static KThread theThreadThatDoesStuff = null;
	private static LotteryQueue testQueue = (LotteryQueue) ThreadedKernel.scheduler.newThreadQueue(false);
	private static LotteryQueue donationQueue = (LotteryQueue) ThreadedKernel.scheduler.newThreadQueue(true);
	private static LotteryQueue donationQueue2 = (LotteryQueue) ThreadedKernel.scheduler.newThreadQueue(true);
}