package nachos.threads;

import nachos.machine.*;

import java.util.Comparator;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
	/**
	 * Allocate a new priority scheduler.
	 */
	public PriorityScheduler() {
	}

	/**
	 * Allocate a new priority thread queue.
	 *
	 * @param	transferPriority	<tt>true</tt> if this queue should
	 *					transfer priority from waiting threads
	 *					to the owning thread.
	 * @return	a new priority thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new PriorityQueue(transferPriority);
	}

	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getPriority();
	}

	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getEffectivePriority();
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= priorityMinimum &&
				priority <= priorityMaximum);

		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMaximum)
			return false;

		setPriority(thread, priority+1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMinimum)
			return false;

		setPriority(thread, priority-1);

		Machine.interrupt().restore(intStatus);
		return true;
	}

	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 0;
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = 7;    

	/**
	 * Return the scheduling state of the specified thread.
	 *
	 * @param	thread	the thread whose scheduling state to return.
	 * @return	the scheduling state of the specified thread.
	 */
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	protected class PriorityQueue extends ThreadQueue {
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
			this.treeSetQueue = new TreeSet<ThreadState>(new ThreadStateComparator());
			this.owner = null;  //owner only set upon acquire
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			
			// Check if the wait queue is empty. If so, return NULL
			if (treeSetQueue.isEmpty()) {
				owner = null;
				return null;
			}  
			
			Lib.debug(dbgThread, "Before Popping: ");
			this.print();
			
			ThreadState next_thread_state = treeSetQueue.pollLast();
			
			Lib.debug(dbgThread, "After Popping: ");
			this.print();  // Print the result of popping the queue
			
			next_thread_state.acquire(this);
			// Pops the thread with the highest effective priority that has been 
			// waiting the longest
			return next_thread_state.thread;  
		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return	the next thread that <tt>nextThread()</tt> would
		 *		return.
		 */
		protected ThreadState pickNextThread() {
			// Check if the wait queue is empty. If so, return NULL
			if (treeSetQueue.isEmpty()) return null;
			
			// Return the thread that is next to run without popping it
			return treeSetQueue.last();  
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me (if you want)
			Lib.debug(dbgThread, "--- Current queue line up (lowest to highest): ");
			for (Iterator<ThreadState> i=treeSetQueue.iterator(); i.hasNext(); )
				Lib.debug(dbgThread, ((ThreadState) i.next()).thread + " ");
			Lib.debug(dbgThread, "\n");
		}

		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;
		public TreeSet<ThreadState> treeSetQueue;
		public ThreadState owner;  //last thread that acquired the queue
	}

	/**
	 * The scheduling state of a thread. This should include the thread's
	 * priority, its effective priority, any objects it owns, and the queue
	 * it's waiting for, if any.
	 *
	 * @see	nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState {
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param	thread	the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			this.thread = thread;
			this.queuesWaitingIn = new LinkedList <PriorityQueue> ();

			setPriority(priorityDefault);
		}

		/**
		 * Return the priority of the associated thread.
		 *
		 * @return	the priority of the associated thread.
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Return the effective priority of the associated thread.
		 *
		 * @return	the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {
			return effectivePriority;
		}
		
		/**
		 * Recursively updates the effective priority of a ThreadState for its
		 * own priority and for any of the resource owners for resources it is
		 * currently waiting on. Only updates if it should be updated.
		 *
		 * @param	newEffectivePriority	the new potential priority.
		 */
		public void updateEffectivePriority(int newEffectivePriority) {
			//if the newEffectivePriority is greater than our current ePriority
			if (this.effectivePriority < newEffectivePriority) {
				
				for (int i = 0; i < queuesWaitingIn.size(); i++) {
					//remove ourselves from their queue
					queuesWaitingIn.get(i).treeSetQueue.remove(this);
				}  //after for loop we've removed ourselves from all waitQueues
				
				//set our effectivePriority to the newEffectivePriority
				this.effectivePriority = newEffectivePriority;
				
				//for each resource we are waiting on
				for (int i = 0; i < queuesWaitingIn.size(); i++) {
					//readd ourselves to their queue with new priority
					queuesWaitingIn.get(i).treeSetQueue.add(this);
					
					//if transferPriority on and the resource has a holder
					if (queuesWaitingIn.get(i).transferPriority 
					    && queuesWaitingIn.get(i).owner != null) {
						
						//see if owner needs to update its priority
						queuesWaitingIn.get(i).owner.updateEffectivePriority(newEffectivePriority);
					}  //after if, owner has done its own recursive calls
					
				} //after for loop, we've updated in all queues
				
			} //bypass everything if the newEffectivePriority is lower
		}  //updateEffectivePriority()
		
		/**
		 * Return the timestamp of the associated thread.
		 *
		 * @return	the timestamp of the associated thread.
		 */
		public long getTimestamp() {
			return timestamp;
		}

		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param	priority	the new priority.
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;

			this.priority = priority;

			// implement me
			this.effectivePriority = priority;
		}
		
		/**
		 * Mark the thread with the current machine time
		 */
		public void markTimestamp() {
			this.timestamp = Machine.timer().getTime();
		}

		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the
		 * resource guarded by <tt>waitQueue</tt>. This method is only called
		 * if the associated thread cannot immediately obtain access.
		 *
		 * @param	waitQueue	the queue that the associated thread is
		 *				now waiting on.
		 *
		 * @see	nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
			// Mark the thread with the current machine time so 
			// we know how long they have been waiting
			this.markTimestamp();  
			
			//if this is a queue with transfer priority and if there is an owner
			if (waitQueue.transferPriority && waitQueue.owner != null) {
				//donate priority
				waitQueue.owner.updateEffectivePriority(this.effectivePriority);	
			} // after if statement, all recursive calls complete
			
			//add to this to the list of queues we are waiting in
			queuesWaitingIn.add(waitQueue);
			
			// Add the thread to the TreeSet wait queue
			Lib.debug(dbgThread, "Before Adding: ");
			waitQueue.print();
			
			waitQueue.treeSetQueue.add(this);
			
			Lib.debug(dbgThread, "After Adding: ");
			waitQueue.print();  // Print the result of pushing into the queue
		}

		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see	nachos.threads.ThreadQueue#acquire
		 * @see	nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			queuesWaitingIn.remove(waitQueue);
			waitQueue.owner = this;  //this ThreadState is now the owner
		}	

		/** The thread with which this object is associated. */	   
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority;
		/** The effective priority of the associated thread. */
		protected int effectivePriority;
		/** The timestamp of the associated thread. */
		protected long timestamp;
		/** List of queues we have acquired. */
		protected LinkedList <PriorityQueue> queuesWaitingIn;
	}
	
	protected class ThreadStateComparator implements Comparator<ThreadState> {
		
		public int compare(ThreadState threadA, ThreadState threadB) {
			int threadA_EffPrio = threadA.effectivePriority;
			int threadB_EffPrio = threadB.effectivePriority;
			
			if (threadA_EffPrio < threadB_EffPrio) {  
				return -1;  // Thread A has lower effective priority
			}
			else if (threadA_EffPrio > threadB_EffPrio) {  
				return 1;// Thread A has higher effective priority
			}
			else if (threadA.getTimestamp() < threadB.getTimestamp()) {  
				return 1; // Thread A has been waiting longer.
			}
			else if (threadA.getTimestamp() > threadB.getTimestamp()) {
				return -1;
			}
			else if (threadA.thread.getID() < threadB.thread.getID()) {  
				return 1; // Thread A has been waiting longer.
			}
			else if (threadA.thread.getID() > threadB.thread.getID()) {
				return -1;
			}
			
			else {
				return 0;
			}
		}  //compare()
		
	}  //class ThreadStateComparator
	
	private static final char dbgThread = 't';
}  //class PriorityScheduler


/* 
 *
 */