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
    	lock.acquire();
    	
    	KThread listener = listenerQueue.nextThread();
    	
    	while (listener == null) {
    		KThread.yield();
    		listener = listenerQueue.nextThread();
    	}
    	
    	this.word = word;
    	
    	listener.ready();
    	
    	while (!wordToBeHeard)
    		KThread.yield();
    	
    	wordToBeHeard = false;
    	
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    int heardWord;
    
    lock.acquire();
    
    if (wordToBeHeard) {
    	heardWord = word;
    	wordToBeHeard = false;
    	lock.release();
    	return heardWord;
    }
    
    else {  
	    while (!wordToBeHeard) {
	    listenerQueue.waitForAccess(KThread.currentThread());
	    KThread.sleep();
	    }
	
	    heardWord = word;
		wordToBeHeard = false;
		lock.release();
		return heardWord;
    }
    }  //listen()
    
    
    
    private boolean wordToBeHeard = false;  //remove static?, variable should belong to object not to class
    private int word;  //remove static?, variable should belong to object not to class
    private Lock lock;  //remove static?, variable should belong to object not to class
    private ThreadQueue listenerQueue =
    		ThreadedKernel.scheduler.newThreadQueue(false);
    private ThreadQueue speakerQueue =
    		ThreadedKernel.scheduler.newThreadQueue(false);
}
