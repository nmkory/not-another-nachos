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
    	KThread nextListener;
    	boolean intStatus = Machine.interrupt().disable();
    	
    	lock.acquire();
    	
    	while (wordToBeHeard) {
    	    lock.release();
    	    speakerQueue.waitForAccess(KThread.currentThread());
    	    KThread.sleep();
    	    lock.acquire();
    	    }
        
        wordToBeHeard = true;
        this.word = word;
        
        if ((nextListener = listenerQueue.nextThread()) != null)
        	nextListener.ready();
        
    	lock.release();
    	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    KThread nextSpeaker;
    boolean intStatus = Machine.interrupt().disable();
    
    lock.acquire();
    
    while (!wordToBeHeard) {
    	if ((nextSpeaker = speakerQueue.nextThread()) != null)
    		nextSpeaker.ready();
	    lock.release();
	    listenerQueue.waitForAccess(KThread.currentThread());
	    KThread.sleep();
	    lock.acquire();
	    }
    
    wordToBeHeard = false;
    
	lock.release();
	Machine.interrupt().restore(intStatus);
	return word;
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
*/
    private static final char dbgThread = 't';
        
    private boolean wordToBeHeard = false;  //remove static?, variable should belong to object not to class
    private int word;  //remove static?, variable should belong to object not to class
    private Lock lock = new Lock();  //remove static?, variable should belong to object not to class
    private ThreadQueue listenerQueue =
    		ThreadedKernel.scheduler.newThreadQueue(false);
    private ThreadQueue speakerQueue =
    		ThreadedKernel.scheduler.newThreadQueue(false);
}
