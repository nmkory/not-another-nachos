package nachos.threads;

import nachos.machine.Lib;

public class Condition2SelfTester {
		
		/**
		 * Test with 1 sleeper then 1 waker.
		 */
		public static void selfTest1() {
			


		KThread sleeper1 = new KThread(sleepRun);
		sleeper1.setName("sleeper1");
		sleeper1.fork();

		KThread waker1 = new KThread(wakeRun);
		waker1.setName("waker1");
		waker1.fork();


		}  //selfTest1()
		
		static void sleepFunction() {
			Lib.debug(dbgThread, "\n Thread " + KThread.currentThread().getName() 
					  + " is about to sleep on Condition2 \n");
			
			lock.acquire();
			cond_2.sleep();
			
			Lib.debug(dbgThread, "\n Thread " + KThread.currentThread().getName()
					  + " was woken by another thread \n");  
				
			}  //sleepFunction()
		
		static void wakeFunction() {
			Lib.debug(dbgThread, "\n Thread " + KThread.currentThread().getName() 
					  + " is about to wake one Condition2 thread \n");
			
			lock.acquire();
			cond_2.wake();
			
			Lib.debug(dbgThread, "\n Thread " + KThread.currentThread().getName() 
					  + " is about to sleep on Condition2 \n");
			
			cond_2.sleep();
			
			Lib.debug(dbgThread, "\n Thread " + KThread.currentThread().getName()
					  + " was woken by another thread \n");  
				
			}  //wakeFunction()
		
		/**
		 * Wraps sleepRun inside a Runnable object so threads can be
		 * generated for testing.
		 */
		private static Runnable sleepRun = new Runnable() {
		public void run() {
			sleepFunction();
		}
		};  //runnable sleepRun
		
		/**
		 * Wraps wakeRun inside a Runnable object so threads can be
		 * generated for testing.
		 */
		private static Runnable wakeRun = new Runnable() {
		public void run() {
			wakeFunction();
		}
		};  //runnable wakeRunRun

		//dbgThread = 't' variable needed for debug output
		private static final char dbgThread = 't';
		
		//lock for condition variable and to maintain atomicity
		static Lock lock = new Lock();
		
			
		//condition variable for listeners
		static Condition2 cond_2 = new Condition2 (lock);
}  //class Cond2SelfTester

