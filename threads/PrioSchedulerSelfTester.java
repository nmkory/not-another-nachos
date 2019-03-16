package nachos.threads;

import nachos.machine.Lib;
import nachos.machine.Machine;

public class PrioSchedulerSelfTester {
	
	/**
	 * A low priority thread and a high priority thread join a thread that is doing stuff.
	 * When theThreadThatDoesStuff finishes running, the higher priority thread should run first
	 */
	public static void selfTest1() {
		theThreadThatDoesStuff = new KThread(immediatelyDoStuff);
		theThreadThatDoesStuff.setName("The Thread That Does Stuff");
		theThreadThatDoesStuff.fork();
		
		KThread lowPriorityThread = new KThread(joinWithPriority2);
		lowPriorityThread.setName("Priority 2 Thread");
		lowPriorityThread.fork();
		
		KThread highPriorityThread = new KThread(joinWithPriority6);
		highPriorityThread.setName("Priority 6 Thread");
		highPriorityThread.fork();
	}
	
	/**
	 * A low priority thread, a high priority thread, and a mid priority thread join a thread that is doing stuff.
	 * When theThreadThatDoesStuff finishes running, the threads should run in this order: High, Mid, Low
	 */
	public static void selfTest2() {
		theThreadThatDoesStuff = new KThread(immediatelyDoStuff);
		theThreadThatDoesStuff.setName("The Thread That Does Stuff");
		theThreadThatDoesStuff.fork();
		
		KThread lowPriorityThread = new KThread(joinWithPriority2);
		lowPriorityThread.setName("Priority 2 Thread");
		lowPriorityThread.fork();
		
		KThread highPriorityThread = new KThread(joinWithPriority6);
		highPriorityThread.setName("Priority 6 Thread");
		highPriorityThread.fork();
		
		KThread midPriorityThread = new KThread(joinWithPriority4);
		midPriorityThread.setName("Priority 4 Thread");
		midPriorityThread.fork();
	}
	
	/**
	 * A low priority thread and two threads of equal priority join a thread that is doing stuff.
	 * When theThreadThatDoesStuff finishes running, the two threads of equal priority should run in the order they joined.
	 * When the two threads of equal priority finish running, the low priority thread should run.
	 * Assuming the threads join in the same order they are initialized below, the threads should run in this order: A, C, B
	 */
	public static void selfTest3() {
		theThreadThatDoesStuff = new KThread(immediatelyDoStuff);
		theThreadThatDoesStuff.setName("The Thread That Does Stuff");
		theThreadThatDoesStuff.fork();
		
		/*
		 *  There is *probably* no guarantee that Thread A will indeed join first.
		 *  However, we should still see the thread that joined first indeed runs first.
		 */
		KThread threadA = new KThread(joinWithPriority4);
		threadA.setName("Thread A");
		threadA.fork();
		
		KThread threadB = new KThread(joinWithPriority2);
		threadB.setName("Thread B");
		threadB.fork();
		
		KThread threadC = new KThread(joinWithPriority4);
		threadC.setName("Thread C");
		threadC.fork();
	}
	
	/**
	 * Threads with a mix of priorities join a thread that is doing some stuff.
	 * One thread has high priority, two threads have mid (but equal) priority, and one thread has low priority.
	 * The threads should run from highest to lowest priority with threads of equal priority running based on who joined first.
	 * Assuming the threads join in the same order they're initialized below, the threads should run in this order: D, A, C, B
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
	 * A function that makes a thread do some stuff
	 * This is literally just a ping test
	 */
	static void doStuff() {
		int numIterations = 5;  // 
		for (int i = 0; i < numIterations; i++) {
			Lib.debug(dbgThread, "Doing stuff..." + i * 100 / numIterations + "% done");
			KThread.yield();
		}
		Lib.debug(dbgThread, "I have finished doing stuff!");
	}
	
	/**
	 * A function that makes a thread join theThreadThatDoesStuff before doing some stuff themselves
	 */
	static void joinThenDoStuff() {
		Lib.debug(dbgThread, "Joining theThreadThatDoesStuff...");
		theThreadThatDoesStuff.join();
		Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I have resumed control. Time to do some stuff!");
		doStuff();
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
	 * A function for manually setting the priority of a specific thread
	 */
	static void setPriorityOfThread(KThread thread, int priority) {
		Lib.debug(dbgThread, "Manually changing the priority of " + thread.getName() + " to " + priority);
		
		boolean intStatus = Machine.interrupt().disable();
		
		ThreadedKernel.scheduler.setPriority(thread, priority);
		Lib.debug(dbgThread, "Done changing the priority of " + thread.getName() + ". Their effective priority is now " + ThreadedKernel.scheduler.getEffectivePriority(thread));

		Machine.interrupt().restore(intStatus);
	}
	
	/**
	 * A Runnable for a thread to do some stuff immediately.
	 * Intended for theThreadThatDoesStuff
	 */
	private static Runnable immediatelyDoStuff = new Runnable() {
		public void run() {
			Lib.debug(dbgThread, "I am " + KThread.currentThread().getName() + " and I'm going to do stuff!");
			doStuff();
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
	
	private static final char dbgThread = 't';
	private static KThread theThreadThatDoesStuff = null;
	// private static PriorityScheduler myPrioScheduler = new PriorityScheduler();
	
}