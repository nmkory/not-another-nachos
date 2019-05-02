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
    	
    	long totalTickets;
    	Random rng;
    }
    
    protected class LotteryThreadState extends ThreadState {
    	public LotteryThreadState(KThread thread) {
    		super(thread);
    		this.queuesWaitingIn = new LinkedList<LotteryQueue>();
    	}
    	
    	@Override
    	public void updateEffectivePriority(int donatedTickets) {
    		for (int i = 0; i < queuesWaitingIn.size(); i++) {
				// remove ourselves from their queue
				queuesWaitingIn.get(i).treeSetQueue.remove(this);
			} // after for loop we've removed ourselves from all waitQueues
    		
    		// increment our effective priority by the number of donatedTickets
    		this.effectivePriority += donatedTickets;
    		
    		// for each resource we are waiting on
			for (int i = 0; i < queuesWaitingIn.size(); i++) {
				// readd ourselves to their queue with new priority
				queuesWaitingIn.get(i).treeSetQueue.add(this);
				
				// increment the totalTickets of the queue
				queuesWaitingIn.get(i).totalTickets += donatedTickets;

				// if transferPriority on and the resource has a holder
				if (queuesWaitingIn.get(i).transferPriority && queuesWaitingIn.get(i).owner != null) {

					// see if owner needs to update its priority
					queuesWaitingIn.get(i).owner.updateEffectivePriority(donatedTickets);
				} // after if, owner has done its own recursive calls

			} // after for loop, we've updated in all queues
    	}
        
        public void waitForAccess(LotteryQueue waitQueue) {
        	waitForAccess((PriorityQueue)(waitQueue));
        	waitQueue.totalTickets += this.getEffectivePriority();
        }
    	
    	protected LinkedList<LotteryQueue> queuesWaitingIn;
    }
    
    private static final char dbgThread = 't';
}
