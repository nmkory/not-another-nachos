package nachos.threads;

import nachos.machine.*;

import java.util.*;

/**
 * Comparator to organize the TreeSet.
 */
class Alarm_Comparator implements Comparator<KThread> {
    public int compare(KThread t1, KThread t2) {
    
    if (t1.getWakeTime() > t2.getWakeTime()) 
        return 1;   
    
    else 
        return -1;   
    }
}  //Alarm_Comparator()


/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
    
    //constructor initiates sleepQueue with the Alarm_Comparator
    sleepQueue = new TreeSet <KThread> (new Alarm_Comparator());
    
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    } //Alarm()

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
    boolean awake = false;
    
    //disable and store interrupts
    boolean intStatus = Machine.interrupt().disable();
    Lib.debug(dbgThread,"\n ***Inside timer interrupt***");
    
    //short circuit if sleepQueue empty, check top wakeTime against current time
    while (sleepQueue.isEmpty() != true 
    	   && sleepQueue.first().getWakeTime() < Machine.timer().getTime()) {
    	
    	Lib.debug(dbgThread,"\n sleeping thread " + sleepQueue.first().getName()
    			  + " is waiting for: " + sleepQueue.first().getWakeTime());
        Lib.debug(dbgThread,"\n current time is: " + Machine.timer().getTime());
        
        sleepQueue.pollFirst().ready();
        awake = true;
    }  //after while loop all sleeping threads that should be awake are on ready
	
    //restore interrupt status
    Machine.interrupt().restore(intStatus);
    
    //force a context switch if there is another thread that should be run
    if (awake)
    	KThread.currentThread().yield();
    } //timerInterrupt()

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
    
    //disable and store interrupts
    boolean intStatus = Machine.interrupt().disable();

    //set the current thread's wait time using parameter
	KThread.currentThread().setWakeTime(Machine.timer().getTime() + x);
	
	//add current thread to the sleep queue
	sleepQueue.add(KThread.currentThread());
	
	//go to sleep - to be woken later during timerInterrupt()
	KThread.sleep();
	
	//we have been woken, so restore interrupt status and resume
	Lib.debug(dbgThread,"\n " + KThread.currentThread().getName() + " woke");
	Machine.interrupt().restore(intStatus);
    }  //waitUntil()
    
	//dbgThread = 't' variable needed for debug output
	private static final char dbgThread = 't';
    
	//sleepQueue holds and organizes threads by the time they should wake
    private TreeSet <KThread> sleepQueue; 
}

