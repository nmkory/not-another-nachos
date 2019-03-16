package nachos.threads;

import nachos.machine.*;

/**
 * A multi-threaded OS kernel.
 */
public class ThreadedKernel extends Kernel {
    /**
     * Allocate a new multi-threaded kernel.
     */
    public ThreadedKernel() {
	super();
    }

    /**
     * Initialize this kernel. Creates a scheduler, the first thread, and an
     * alarm, and enables interrupts. Creates a file system if necessary.   
     */
    public void initialize(String[] args) {
	// set scheduler
	String schedulerName = Config.getString("ThreadedKernel.scheduler");
	scheduler = (Scheduler) Lib.constructObject(schedulerName);

	// set fileSystem
	String fileSystemName = Config.getString("ThreadedKernel.fileSystem");
	if (fileSystemName != null)
	    fileSystem = (FileSystem) Lib.constructObject(fileSystemName);
	else if (Machine.stubFileSystem() != null)
	    fileSystem = Machine.stubFileSystem();
	else
	    fileSystem = null;

	// start threading
	new KThread(null);

	alarm  = new Alarm();

	Machine.interrupt().enable();
    }

    /**
     * Test this kernel. Test the <tt>KThread</tt>, <tt>Semaphore</tt>,
     * <tt>SynchList</tt>, and <tt>ElevatorBank</tt> classes. Note that the
     * autograder never calls this method, so it is safe to put additional
     * tests here.
     */	
    public void selfTest() {
    //variable main stores main os KThread in local context for atom comparison
    KThread main = KThread.currentThread();
    
    
    /** Uncomment tests to see if things work. */
    //Boat.selfTest();  //go to Boat.java and uncomment its selfTests there
	//Semaphore.selfTest();
	//SynchList.selfTest();
    
    //KThreadJoinSelfTester.selfTest1();
    //KThreadJoinSelfTester.selfTest2();
    //KThreadJoinSelfTester.selfTest3();
    //KThreadJoinSelfTester.selfTest4();
    //KThreadJoinSelfTester.selfTest5();
    
    //CommSelfTester.selfTest1();
    //CommSelfTester.selfTest2();
    //CommSelfTester.selfTest3();
    //CommSelfTester.selfTest4();
    //CommSelfTester.selfTest5();
    
    //AlarmSelfTester.selfTest1();
    //AlarmSelfTester.selfTest2();
    //AlarmSelfTester.selfTest3();
    
    Condition2SelfTester.selfTest1();
    
    //for loop to keep main thread alive when self testing
    for (int i = 0; i < 150; i++) {
    	
    	//check that we're yielding main
    	if (KThread.currentThread().compareTo(main) == 0) {
    		
    		//debug statement to see when main yields
    		Lib.debug(dbgThread, "main thread yielding"); 
    		
    		KThread.yield();
    	}  //else this is not main thread so do not yield
    }  //for loop has yielded 30 times and os will move on to finish
    
	if (Machine.bank() != null) {
	    ElevatorBank.selfTest();
	}
    }
    
    /**
     * A threaded kernel does not run user programs, so this method does
     * nothing.
     */
    public void run() {
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	Machine.halt();
    }
    
    //debug thread for this class for testing
    private static final char dbgThread = 't';

    /** Globally accessible reference to the scheduler. */
    public static Scheduler scheduler = null;
    /** Globally accessible reference to the alarm. */
    public static Alarm alarm = null;
    /** Globally accessible reference to the file system. */
    public static FileSystem fileSystem = null;

    // dummy variables to make javac smarter
    private static RoundRobinScheduler dummy1 = null;
    private static PriorityScheduler dummy2 = null;
    private static LotteryScheduler dummy3 = null;
    private static Condition2 dummy4 = null;
    private static Communicator dummy5 = null;
    private static Rider dummy6 = null;
    private static ElevatorController dummy7 = null;
}
