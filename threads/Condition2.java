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
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	q = new LinkedList<KThread>();
	
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically re-acquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
        // if lock is true because its held. 
	    Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	    boolean state = Machine.interrupt().disable();
//linkedlist queue
	    q.add(KThread.currentThread());
// lock release
	   conditionLock.release();

// do I need to sleep this?
	    conditionLock.acquire();

	    Machine.interrupt().restore(state);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
      
	    Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
        boolean state = Machine.interrupt().disable();
	
        Machine.interrupt().restore(state);
    
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    
    
    public void wakeAll() {
	    Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	        boolean state = Machine.interrupt().disable();

        Machine.interrupt().restore(state);     
    }

    private Lock conditionLock;
        private LinkedList<KThread> q;
}