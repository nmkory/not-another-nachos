package nachos.threads;

import nachos.machine.Lib;
import nachos.machine.Machine;

public class PrioSchedulerSelfTester {
	
	/**
	 * A low priority thread, a high priority thread, and a mid priority thread do some stuff.
	 * The threads should run in this order: High, Mid, Low
	 */
	public static void selfTest1() {
		KThread lowPriorityThread = new KThread(immediatelyDoStuff);
		lowPriorityThread.setName("Priority 2 Thread");
		changePriorityOfThread(lowPriorityThread, 2);
		lowPriorityThread.fork();
		
		KThread highPriorityThread = new KThread(immediatelyDoStuff);
		highPriorityThread.setName("Priority 6 Thread");
		changePriorityOfThread(highPriorityThread, 6);
		highPriorityThread.fork();
		
		KThread midPriorityThread = new KThread(immediatelyDoStuff);
		midPriorityThread.setName("Priority 4 Thread");
		changePriorityOfThread(midPriorityThread, 4);
		midPriorityThread.fork();
	}

	
	/**
	 * A low priority thread and two threads of equal priority do some stuff.
	 * The two threads of equal priority should run in the order they were forked.
	 * When the two threads of equal priority finish running, the low priority thread should run.
	 */
	public static void selfTest2() {
		KThread threadA = new KThread(immediatelyDoStuff);
		threadA.setName("Thread A");
		changePriorityOfThread(threadA, 4);
		threadA.fork();
		
		KThread threadB = new KThread(immediatelyDoStuff);
		threadB.setName("Thread B");
		changePriorityOfThread(threadB, 3);
		threadB.fork();
		
		KThread threadC = new KThread(immediatelyDoStuff);
		threadC.setName("Thread C");
		changePriorityOfThread(threadC, 4);
		threadC.fork();
	}
	
	/**
	 * Threads with a mix of priorities run simultaneously.
	 * One thread has high priority, two threads have mid (but equal) priority, and one thread has low priority.
	 * The threads should run from highest to lowest priority with threads of equal priority context switching back and forth.
	 */
	public static void selfTest3() {
		KThread threadA = new KThread(immediatelyDoStuff);
		threadA.setName("Thread A, Priority 3");
		changePriorityOfThread(threadA, 3);
		threadA.fork();
		
		KThread threadB = new KThread(immediatelyDoStuff);
		threadB.setName("Thread B, Priority 2");
		changePriorityOfThread(threadB, 2);
		threadB.fork();
		
		KThread threadC = new KThread(immediatelyDoStuff);
		threadC.setName("Thread C, Priority 3");
		changePriorityOfThread(threadC, 3);
		threadC.fork();
		
		KThread threadD = new KThread(immediatelyDoStuff);
		threadD.setName("Thread D, Priority 4");
		changePriorityOfThread(threadD, 4);
		threadD.fork();
	}
	
	/**
	 * Same as selfTest3() except instead of putting the threads into the readyQueue, we make the threads join theThreadThatDoesStuff
	 * When theThreadThatDoesStuff finishes running, the threads should run in the same order as in selfTest3()
	 */
	public static void selfTest4() {
		theThreadThatDoesStuff = new KThread(immediatelyDoStuff);
		theThreadThatDoesStuff.setName("The Thread That Does Stuff");
		theThreadThatDoesStuff.fork();
		
		KThread threadA = new KThread(joinWithPriority3);
		threadA.setName("Thread A, Priority 3");
		threadA.fork();
		
		KThread threadB = new KThread(joinWithPriority2);
		threadB.setName("Thread B, Priority 2");
		threadB.fork();
		
		KThread threadC = new KThread(joinWithPriority3);
		threadC.setName("Thread C, Priority 3");
		threadC.fork();
		
		KThread threadD = new KThread(joinWithPriority4);
		threadD.setName("Thread D, Priority 4");
		threadD.fork();
	}
	
	/**
	 * A thread with a high priority has their priority decreased while it's waiting for theThreadThatDoesStuff
	 * The priority is decreased below the priority of all other threads, so the other threads should run first.
	 * The result should be that the threads are originally scheduled in the order A, C, B but then are scheduled in the order C, B, A
	 */
	public static void selfTest5() {
		theThreadThatDoesStuff = new KThread(immediatelyDoStuff);
		theThreadThatDoesStuff.setName("The Thread That Does Stuff");
		theThreadThatDoesStuff.fork();
		
		KThread threadA = new KThread(joinWithPriority6);
		threadA.setName("Thread A, Priority 6/2");
		threadA.fork();
		
		KThread threadB = new KThread(joinWithPriority4);
		threadB.setName("Thread B, Priority 4");
		threadB.fork();
		
		KThread threadC = new KThread(joinWithPriority5);
		threadC.setName("Thread C, Priority 5");
		threadC.fork();
		
		KThread.yield();  // Let Threads A-C join before attempting to modify the priority of Thread A
		KThread.yield();  // Yield again so that theThreadThatDoesStuff runs at least once
		changePriorityOfThread(threadA, 2);  // Change the priority of Thread A to 2 making it the thread with the least priority
	}
	
	/**
	 * A trio of threads are running with equal priority. While running, one of the threads lowers its own priority while another increases its own priority.
	 * The threads should finish in this order: 1) The thread that increased its priority 2) The thread that didn't change priority 3) The thread that decreased its priority.
	 */
	public static void selfTest6() {
		KThread threadA = new KThread(decreasePriorityWhileRunning);
		threadA.setName("Decreasing while Running");
		
		KThread threadB = new KThread(immediatelyDoStuff);
		threadB.setName("Staying the Same");
		
		KThread threadC = new KThread(increasePriorityWhileRunning);
		threadC.setName("Increasing while Running");
		
		changePriorityOfThread(threadA, 4);
		changePriorityOfThread(threadB, 4);
		changePriorityOfThread(threadC, 4);
		
		threadA.fork();
		threadB.fork();
		threadC.fork();
	}
	
	
	/**
	 * A function that makes a thread do some stuff
	 * This is literally just a ping test
	 */
	static void doStuff(int numIterations) { 
		for (int i = 0; i < numIterations; i++) {
			Lib.debug(dbgThread, "Doing stuff..." + i * 100 / numIterations + "% done");
			KThread.yield();
		}
		Lib.debug(dbgThread, "I have finished doing stuff!");
	}
	
	/**
	 * A function that makes a thread increase its priority while it's running
	 * This increase in priority occurs early in the ping test
	 */
	static void increasePriorityWhileDoingStuff() {
		doStuff(3);
		Lib.debug(dbgThread, "Oh, I need to increase my priority! (For whatever reason)");
		
		boolean intStatus = Machine.interrupt().disable();
		
		ThreadedKernel.scheduler.increasePriority();
		
		Machine.interrupt().restore(intStatus);
		
		Lib.debug(dbgThread, "Cool! Back to doing stuff.");
		doStuff(7);
	}
	
	/**
	 * A function that makes a thread increase its priority while it's running
	 * This decrease in priority occurs late in the ping test
	 */
	static void decreasePriorityWhileDoingStuff() {
		doStuff(7);
		Lib.debug(dbgThread, "Oh, I need to decrease my priority! (For whatever reason)");
		
		boolean intStatus = Machine.interrupt().disable();
		
		ThreadedKernel.scheduler.decreasePriority();
		
		Machine.interrupt().restore(intStatus);
		
		Lib.debug(dbgThread, "Cool! Back to doing stuff.");
		doStuff(3);
	}
	
	/**
	 * A function that makes a thread join theThreadThatDoesStuff before doing some stuff themselves
	 */
	static void joinThenDoStuff() {
		Lib.debug(dbgThread, "Joining theThreadThatDoesStuff...");
		theThreadThatDoesStuff.join();
		Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I have resumed control. Time to do some stuff!");
		doStuff(10);
	}
	
	/**
	 * A function that makes a thread set its priority first before joining theThreadThatDoesStuff
	 */
	static void setPriorityThenJoin(int priority) {
		Lib.debug(dbgThread, "Updating my priority to " + priority + "...");
		
		boolean intStatus = Machine.interrupt().disable();
		
		ThreadedKernel.scheduler.setPriority(KThread.currentThread(), priority);
		Lib.debug(dbgThread, "Done updating my priority. My effective priority is now " + ThreadedKernel.scheduler.getEffectivePriority(KThread.currentThread()));

		Machine.interrupt().restore(intStatus);
		
		joinThenDoStuff();
	}
	
	/**
	 * A function for manually changing the priority of a specific thread
	 */
	static void changePriorityOfThread(KThread thread, int priority) {
		Lib.debug(dbgThread, "Manually changing the priority of " + thread.getName() + " to " + priority);
		
		boolean intStatus = Machine.interrupt().disable();
		
		ThreadedKernel.scheduler.setPriority(thread, priority);
		Lib.debug(dbgThread, "Done changing the priority of " + thread.getName() + ". Their effective priority is now " + ThreadedKernel.scheduler.getEffectivePriority(thread));

		Machine.interrupt().restore(intStatus);
	}
//	
//	/**
//	 * A function for manually increasing the priority of a specific thread
//	 */
//	static void increasePriorityOfThread(KThread thread) {
//		Lib.debug(dbgThread, "Manually increasing the priority of " + thread.getName());
//		
//		boolean intStatus = Machine.interrupt().disable();
//		
//		if (ThreadedKernel.scheduler.increasePriority())
//			Lib.debug(dbgThread, "Done increasing the priority of " + thread.getName() + ". Their effective priority is now " + ThreadedKernel.scheduler.getEffectivePriority(thread));
//		else
//			Lib.debug(dbgThread, thread.getName() + " is already at maximum priority!");
//			
//		Machine.interrupt().restore(intStatus);
//	}
//	
//	/**
//	 * A function for manually decreasing the priority of a specific thread
//	 */
//	static void decreasePriorityOfThread(KThread thread) {
//		Lib.debug(dbgThread, "Manually decreasing the priority of " + thread.getName());
//		
//		boolean intStatus = Machine.interrupt().disable();
//		
//		if (ThreadedKernel.scheduler.decreasePriority())
//			Lib.debug(dbgThread, "Done decreasing the priority of " + thread.getName() + ". Their effective priority is now " + ThreadedKernel.scheduler.getEffectivePriority(thread));
//		else
//			Lib.debug(dbgThread, thread.getName() + " is already at minimum priority!");
//		
//		Machine.interrupt().restore(intStatus);
//	}
//	
	/**
	 * A Runnable for a thread to do some stuff immediately.
	 * Intended for theThreadThatDoesStuff
	 */
	private static Runnable immediatelyDoStuff = new Runnable() {
		public void run() {
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to do stuff!");
			doStuff(10);
		}
	};
	
	/**
	 * A Runnable for a thread that will first join on theThreadThatDoesStuff before doing stuff itself.
	 * These threads have default priority
	 */
	private static Runnable joinThenDoStuff = new Runnable() {
		public void run() {
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to join before doing stuff!");
			joinThenDoStuff();
		}
	};
	
	/**
	 * A Runnable for a thread that will set its priority before joining theThreadThatDoesStuff
	 * These threads have the minimum priority (0)
	 */
	private static Runnable joinWithMinPriority = new Runnable() {
		public void run() {
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to join with the minimum priority!");
			setPriorityThenJoin(PriorityScheduler.priorityMinimum);
		}
	};
	
	/**
	 * A Runnable for a thread that will set its priority before joining theThreadThatDoesStuff
	 * These threads have a priority of 1
	 * This runnable is technically no different than joinThenDoStuff
	 */
	private static Runnable joinWithPriority1 = new Runnable() {
		public void run() {
			int priority = 1;
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to join with a priority of " + priority);
			setPriorityThenJoin(priority);
		}
	};
	
	/**
	 * A Runnable for a thread that will set its priority before joining theThreadThatDoesStuff
	 * These threads have a priority of 2
	 */
	private static Runnable joinWithPriority2 = new Runnable() {
		public void run() {
			int priority = 2;
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to join with a priority of " + priority);
			setPriorityThenJoin(priority);
		}
	};
	
	/**
	 * A Runnable for a thread that will set its priority before joining theThreadThatDoesStuff
	 * These threads have a priority of 3
	 */
	private static Runnable joinWithPriority3 = new Runnable() {
		public void run() {
			int priority = 3;
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to join with a priority of " + priority);
			setPriorityThenJoin(priority);
		}
	};
	
	/**
	 * A Runnable for a thread that will set its priority before joining theThreadThatDoesStuff
	 * These threads have a priority of 4
	 */
	private static Runnable joinWithPriority4 = new Runnable() {
		public void run() {
			int priority = 4;
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to join with a priority of " + priority);
			setPriorityThenJoin(priority);
		}
	};
	
	/**
	 * A Runnable for a thread that will set its priority before joining theThreadThatDoesStuff
	 * These threads have a priority of 5
	 */
	private static Runnable joinWithPriority5 = new Runnable() {
		public void run() {
			int priority = 5;
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to join with a priority of " + priority);
			setPriorityThenJoin(priority);
		}
	};
	
	/**
	 * A Runnable for a thread that will set its priority before joining theThreadThatDoesStuff
	 * These threads have a priority of 6
	 */
	private static Runnable joinWithPriority6 = new Runnable() {
		public void run() {
			int priority = 6;
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to join with a priority of " + priority);
			setPriorityThenJoin(priority);
		}
	};
	
	/**
	 * A Runnable for a thread that will set its priority before joining theThreadThatDoesStuff
	 * These threads have the maximum priority (7)
	 */
	private static Runnable joinWithMaxPriority = new Runnable() {
		public void run() {
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to join with the maximum priority!");
			setPriorityThenJoin(PriorityScheduler.priorityMaximum);
		}
	};
	
	/**
	 * A Runnable for a thread that will increase its priority while running
	 */
	private static Runnable increasePriorityWhileRunning = new Runnable() {
		public void run() {
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to do stuff!");
			increasePriorityWhileDoingStuff();
		}
	};
	
	/**
	 * A Runnable for a thread that will decrease its priority while running
	 */
	private static Runnable decreasePriorityWhileRunning = new Runnable() {
		public void run() {
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to do stuff!");
			decreasePriorityWhileDoingStuff();
		}
	};
	
	private static final char dbgThread = 't';
	private static KThread theThreadThatDoesStuff = null;
	// private static PriorityScheduler myPrioScheduler = new PriorityScheduler();
	
}