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
    boolean intStatus = Machine.interrupt().disable();
    Lib.debug(dbgThread,"\n ***inside timer interrupt***");
    
    while (sleepQueue.isEmpty() != true && sleepQueue.first().getWakeTime() < Machine.timer().getTime()) {
    	Lib.debug(dbgThread,"\n sleeper is waiting for: " + sleepQueue.first().getWakeTime());
        Lib.debug(dbgThread,"\n current time is: " + Machine.timer().getTime());
        sleepQueue.pollFirst().ready();
    }
		
    Machine.interrupt().restore(intStatus);
    //KThread.currentThread().yield();
    }

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
	// for now, cheat just to get something working (busy waiting is bad)
    boolean intStatus = Machine.interrupt().disable();
	long wakeTime = Machine.timer().getTime() + x;
	KThread.currentThread().setWakeTime(wakeTime);
	sleepQueue.add(KThread.currentThread());
	
	KThread.sleep();
	Lib.debug(dbgThread,"\n ***I'm asleep***");
	Machine.interrupt().restore(intStatus);
    }
    
    private static class PingTest implements Runnable {

    	
    	public void run() {
    		Alarm alarm = new Alarm();
    		Lib.debug(dbgThread,"\n ***Testing Alarm***");
    		Lib.debug(dbgThread,"\n Time is: " + Machine.timer().getTime());
    		alarm.waitUntil(1);
    		Lib.debug(dbgThread,"\n Time is: " + Machine.timer().getTime());
    		
    		for (int i=0; i<10; i++) {
    			Lib.debug(dbgThread,"\n I'm awake");
    	    }
    	}


        }
    
    public static void selfTest() {

    	new KThread(new PingTest()).setName("ping").fork();

    	
        }
    
	//dbgThread = 't' variable needed for debug output
	private static final char dbgThread = 't';
    
    private TreeSet <KThread> sleepQueue;
    
    
}

