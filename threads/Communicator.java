package nachos.threads;

import nachos.machine.*;

/**
 * [TODO] !!!!CHANGE Condition VARIABLES TO Condition2 VARIABLES!!!!
 * 
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
	//disable and stores interrupts to acquire lock
	boolean intStatus = Machine.interrupt().disable();
	lock.acquire();
	
	//while there is a word in the buffer
	while (wordToBeHeard) {
		//wake a potential listener on the listener cond variable
		listener.wake();
		
		//sleep on the spker cond variable, will release lock & require @ wake
		speaker.sleep();
	}  //leaves while loop when a word may be moved into the buffer
	
	//moves the parameter into the static variable for this obj
	this.word = word;
	
	//notes that the buffer is full
	wordToBeHeard = true;
	
	//wakes a potential listen partner
	listener.wake();
	
	//sleeps, can't return until thread is paired up with a listening thread
	speaker.sleep();
	
	//releases the lock and restores interrupts
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
	//disable and stores interrupts to acquire lock
	boolean intStatus = Machine.interrupt().disable();
	lock.acquire();
	
	//while there is no word in the buffer
	while (!wordToBeHeard) {
		//wake any potential speakers
		speaker.wake();
		
		//sleep and release the lock
		listener.sleep();	
	}  //leaves while loop when a word is in the buffer
	
	//store the buffer word in local context
	int wordToHear = word;
	
	//reset buffer to receive a new word
	wordToBeHeard = false;
	
	//wake a partner sleeper that is waiting on a listener
	speaker.wake();
	
	//releases the lock and restores interrupts
	lock.release();
	Machine.interrupt().restore(intStatus);
	return wordToHear; 	
	}  //listen()   	

	//variable to indicate if buffer is full
	private boolean wordToBeHeard = false;
	
	//buffer to pass word
	private int word;
	
	//lock for condition variables and to maintain atomicity
	private Lock lock = new Lock();
	
	//condition variable for listeners
	private Condition listener = new Condition (lock);
	
	////condition variable for speakers
	private Condition speaker = new Condition (lock);
}
