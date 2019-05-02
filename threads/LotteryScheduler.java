package nachos.threads;

import nachos.machine.*;
import nachos.threads.PriorityScheduler.ThreadState;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
    	return new LotteryQueue(transferPriority);
    }
    
    @Override
    protected LotteryThreadState getThreadState(KThread thread) {
    	if (thread.schedulingState == null)
			thread.schedulingState = new LotteryThreadState(thread);

		return (LotteryThreadState) thread.schedulingState;
    }
    /*
    public static final int priorityMinimum = 1;
    
    public static final int priorityMaximum = Integer.MAX_VALUE;
    */
    /**
     * A <tt>ThreadQueue</tt> that runs using a lottery. Inherits most functionality from <tt>PriorityQueue</tt>
     */
    protected class LotteryQueue extends PriorityQueue {
    	LotteryQueue(boolean transferPriority) {
    		super(transferPriority);
    		this.totalTickets = 0;
    		this.rng = new Random();
    		this.owner = null;
    	}
    	
    	@Override
    	public KThread nextThread() {
    		Lib.assertTrue(Machine.interrupt().disabled());
    		
    		// Check if the wait queue is empty. If so, return NULL
    		if (treeSetQueue.isEmpty()) {
    			owner = null;
    			return null;
    		}
    		
    		Lib.debug(dbgThread, "Before popping:");
    		this.print();  // Print the contents of the queue before popping
    		
    		// Poll a random number from 1 to totalTickets inclusive
    		long winningTicket = rng.nextInt((int)(totalTickets)) + 1;  // Note: For the purposes of Project 2, it is safe to cast our totalTickets to an int
    		long runningTotal = 0;
    		ThreadState winning_thread = null;
    		
    		// For each thread in our queue...
    		for (Iterator<ThreadState> i = treeSetQueue.iterator(); i.hasNext();) {
    			ThreadState nextThread = i.next();  // Grab the next thread
    			runningTotal += nextThread.getEffectivePriority();  // Add its effectivePriority to our running total
    			
    			if (runningTotal >= winningTicket) {  // If this thread holds the winning ticket...
    				winning_thread = nextThread;  // Set this thread as the winner
    				i.remove();  // Remove the thread from the queue
    				totalTickets -= winning_thread.getEffectivePriority();  // Remove the tickets from the lottery
    				break;  // Break from the for loop
    			}
    		}
    		
    		Lib.debug(dbgThread, "After Popping: ");
			this.print(); // Print the result of popping the queue
    		
    		winning_thread.acquire(this);
    		return winning_thread.thread;
    	}
    	
    	@Override
    	public void waitForAccess(KThread thread) {
    		Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);  // Invokes the waitForAcess() from LotteryThreadState rather than from ThreadState
    	}
    	
    	@Override
    	public void acquire(KThread thread) {
    		Lib.assertTrue(Machine.interrupt().disabled());
    		getThreadState(thread).acquire(this);  // Invokes acquire() from LotteryThreadState rather than from ThreadState
    	}
    	
    	long totalTickets;
    	Random rng;
    	LotteryThreadState owner;
    }
    
    protected class LotteryThreadState extends ThreadState {
    	public LotteryThreadState(KThread thread) {
    		super(thread);
    		this.lotteryQueuesWaitingIn = new LinkedList<LotteryQueue>();
    	}
    	
    	@Override
    	public void updateEffectivePriority(int donatedTickets) {
    		for (int i = 0; i < lotteryQueuesWaitingIn.size(); i++) {
				// remove ourselves from their queue
				lotteryQueuesWaitingIn.get(i).treeSetQueue.remove(this);
			} // after for loop we've removed ourselves from all waitQueues
    		
    		// increment our effective priority by the number of donatedTickets
    		this.effectivePriority += donatedTickets;
    		
    		// for each resource we are waiting on
			for (int i = 0; i < lotteryQueuesWaitingIn.size(); i++) {
				// readd ourselves to their queue with new priority
				lotteryQueuesWaitingIn.get(i).treeSetQueue.add(this);
				
				// increment the totalTickets of the queue
				lotteryQueuesWaitingIn.get(i).totalTickets += donatedTickets;

				// if transferPriority on and the resource has a holder
				if (lotteryQueuesWaitingIn.get(i).transferPriority && lotteryQueuesWaitingIn.get(i).owner != null) {

					// see if owner needs to update its priority
					lotteryQueuesWaitingIn.get(i).owner.updateEffectivePriority(donatedTickets);
				} // after if, owner has done its own recursive calls

			} // after for loop, we've updated in all queues
    	}
        
        public void waitForAccess(LotteryQueue waitQueue) {
        	// Mark the thread with the current machine time so
        	// we know how long they have been waiting
        	this.markTimestamp();

        	// if this is a queue with transfer priority and if there is an owner
        	if (waitQueue.transferPriority && waitQueue.owner != null) {
        		// donate priority
        		waitQueue.owner.updateEffectivePriority(this.effectivePriority);
        	} // after if statement, all recursive calls complete

        	// add to this to the list of queues we are waiting in
        	lotteryQueuesWaitingIn.add(waitQueue);
        	
        	// Increment the total number of tickets in the lottery
        	waitQueue.totalTickets += this.effectivePriority;

        	// Add the thread to the TreeSet wait queue
        	Lib.debug(dbgThread, "Before Adding: ");
        	waitQueue.print();

        	waitQueue.treeSetQueue.add(this);

        	Lib.debug(dbgThread, "After Adding: ");
        	waitQueue.print(); // Print the result of pushing into the queue
        }
        
        public void acquire(LotteryQueue waitQueue) {
        	waitQueue.owner = this;
        	lotteryQueuesWaitingIn.remove(waitQueue);  // Remove the lottery queue from the list of queues the thread is waiting in
        }
    	
    	protected LinkedList<LotteryQueue> lotteryQueuesWaitingIn;
    }
    
    private static final char dbgThread = 't';
}
