package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	boolean intStatus = Machine.interrupt().disable();
	    lock.acquire();
	    
	    while (wordToBeHeard) {
	    	listener.wake();
	    	speaker.sleep();
	    }
	    
	   this.word = word;
	   wordToBeHeard = true;
	   listener.wake();
	   speaker.sleep();
	   lock.release();
	   Machine.interrupt().restore(intStatus);
    }  //speak()
    
    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
	    boolean intStatus = Machine.interrupt().disable();
	    lock.acquire();
	    
	    while (!wordToBeHeard) {
	    	speaker.wake();
	    	listener.sleep();	
	    }
	    
	   int wordToHear = word;
	   wordToBeHeard = false;
	   speaker.wake();
	   lock.release();
	   Machine.interrupt().restore(intStatus);
	   return wordToHear; 	
   }  //listen()   	
    
    
    
/*    private static class speakerRun implements Runnable {
    	speakerRun(Communicator myCom) {
    	    this.myCom = myCom;
    	}
    	
    	public void run() {
    	   myCom.speak(2);    	    
    	}

    	private Communicator myCom;
        }  //speakerRun()
    
    private static class listenRun implements Runnable {
    	listenRun(Communicator myCom) {
    	    this.myCom = myCom;
    	    Lib.debug(dbgThread,
      			  	"Creating listen thread");
    	}
    	
    	public void run() {
    		Lib.debug(dbgThread,
      			  	"Thread " + KThread.currentThread().getName() + "is about to listen");
    		Lib.debug(dbgThread,
      			  	"Thread " + KThread.currentThread().getName() + "got value " + myCom.listen());    	    
    	}

    	private Communicator myCom;
        }  //listenerRun()

        
        public static void selfTest1() {
    	Communicator testCom = new Communicator();

    	KThread listener1 = new KThread(new listenRun(testCom));
    	listener1.setName("listener1");
    	listener1.fork();
    	
    	KThread speaker1 = new KThread(new speakerRun(testCom));
    	speaker1.setName("speaker1");
    	speaker1.fork();
    	
    	
        }  //selfTest1()

    private static final char dbgThread = 't';
*/
    
    private boolean wordToBeHeard = false;
    private int word;
    private Lock lock = new Lock();
    private Condition listener =
    		new Condition (lock);
    private Condition speaker =
    		new Condition (lock);
}
