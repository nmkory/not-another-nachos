package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 *
	 * @param conditionLock the lock associated with this condition variable. The
	 *                      current thread must hold this lock whenever it uses
	 *                      <tt>sleep()</tt>, <tt>wake()</tt>, or
	 *                      <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;

		// Declare new waitQueue LinkedList for KThreads
		waitQueue = new LinkedList<KThread>();
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically reacquire
	 * the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		// Atomically release the associated lock and go to sleep
		conditionLock.release();
		boolean intStatus = Machine.interrupt().disable();

		waitQueue.add(KThread.currentThread());
		KThread.sleep();

		Machine.interrupt().restore(intStatus);
		conditionLock.acquire();
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		if (!waitQueue.isEmpty()) {
			boolean intStatus = Machine.interrupt().disable();

			KThread thread = waitQueue.removeFirst();
			if (thread != null) {
				thread.ready();
			}

			Machine.interrupt().restore(intStatus);
		}
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current thread
	 * must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		// while there are still KThreads in the waitQueue
		while (!waitQueue.isEmpty())
			wake();
	} // all KThreads have been woken

	private Lock conditionLock;
	// linkedlist to track the sleeping KThreads
	private LinkedList<KThread> waitQueue;
}
