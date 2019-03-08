package nachos.threads;
import nachos.ag.BoatGrader;
import nachos.machine.Lib;

public class Boat
{
	static BoatGrader bg;
	static boolean not_done;
	static boolean boat_is_on_oahu;
	static Lock lock;
	static Condition child_on_oahu;
	static Condition child_on_molokai;
	static Condition adult_on_oahu;
	static Condition adult_on_molokai;
	static int total_children;
	static int total_adults;
	static int num_child_on_molokai;
	static int num_adult_on_molokai;
	static int children_on_boat;

	public static void selfTest()
	{
	BoatGrader b = new BoatGrader();

//	System.out.println("\n ***Testing Boats with only 2 children***");
//	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with only 100 children***");
//	begin(0, 100, b);

//	System.out.println("\n ***Testing Boats with only 2 children, 1 adult***");
//	begin(1, 2, b);

//	System.out.println("\n ***Testing Boats with 3 children, 1 adult***");
//	begin(1, 3, b);

//  System.out.println("\n ***Testing Boats with 2 children, 2 adults***");
//  begin(2, 2, b);

//  System.out.println("\n ***Testing Boats with 3 children, 2 adults***");
//  begin(2, 3, b);

// 	System.out.println("\n ***Testing Boats with 2 children, 100 adults***");
//	begin(100, 2, b);

//	System.out.println("\n ***Testing Boats with 17 children, 23 adults***");
//	begin(23, 17, b);
	}  // selfTest()

	public static void begin( int adults, int children, BoatGrader b )
	{
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	not_done = true;
	boat_is_on_oahu = true;
	lock = new Lock();
	child_on_oahu = new Condition(lock);
	child_on_molokai = new Condition(lock);
	adult_on_oahu = new Condition(lock);
	adult_on_molokai = new Condition(lock);
	total_children = children;
	total_adults = adults;
	num_child_on_molokai = 0;
	num_adult_on_molokai = 0;
	children_on_boat = 0;
		
	// Define runnable object for child thread.
	Runnable r_child = new Runnable() {
		public void run() {
			ChildItinerary();
		}
	};  // r_child Runnable()

	// Define runnable object for adult thread.
	Runnable r_adult = new Runnable() {
		public void run() {
			AdultItinerary();
		}
	};  // r_adult Runnable()



	// Spawn all adult threads.
	for (int i = 0; i < adults; i++) {
		new KThread(r_adult).setName("Adult " + Integer.toString(i+1)).fork();
	}  // after this for loop, all adult threads are spawned and sleeping

	// Spawn all child threads.
	for (int i = 0; i < children; i++) {
		new KThread(r_child).setName("Child " + Integer.toString(i+1)).fork();
	}  // after this for loop, all child threads are spawned and start running
	
	// hold main thread while solutions calls are made to the BoatGrader
	while (not_done) 
    	KThread.yield();
	//  while loop ends when last children and all adults are on Molokai
	    
	}  // begin()
	
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/

	static void AdultItinerary()
	{
	// adult threads can only operate with the lock atomically
	lock.acquire();
	
	// while there are still adults not asleep on Molokai
	while(true) {
		
		/*
		 * [NOTE] Using num_child_on_molokai to determine children on Oahu. Per
		 * specifications, this adult can know how many children are on Molokai 
		 * because it has seen them leave Oahu. It can also see children on
		 * Oahu.  If the boat is not on Oaku or if there is more than one child
		 * on Oahu...
		 */
		if (!boat_is_on_oahu || (total_children - num_child_on_molokai) > 1) {
			
			//  go to sleep and let children go first
			child_on_oahu.wakeAll();
			adult_on_oahu.sleep();
		}  // after if, boat is on Oahu and children do not need it.

		// check the boat if there are children on it
		else {
			
			// if there are no child passengers in the boat
			if (children_on_boat == 0) {
				
				// row adult self to Molokai and wake one child up so it can 
				// bring the boat back
				bg.AdultRowToMolokai();
				num_adult_on_molokai++;
				boat_is_on_oahu = false;
				child_on_molokai.wake();
				adult_on_molokai.sleep();
			}  // after if, adult sleeps on Molokai, gives boat to Molokai child
		}  // else check that boat can be used, otherwise restart outer while 
	}  //while adult still need to get to Molokai
	
	}  // AdultItinerary()

	static void ChildItinerary()
	{
		lock.acquire();

		while(true) {
			if (!boat_is_on_oahu) {
				child_on_oahu.sleep();
			}
			else {
				if (children_on_boat == 0) {
					children_on_boat++;
					child_on_oahu.wakeAll();
					child_on_molokai.sleep();

					//this child is woken up on molokai by an adult
					bg.ChildRowToOahu();
					boat_is_on_oahu = true;
					num_child_on_molokai--;
					children_on_boat = 0;
					adult_on_oahu.wakeAll();
					child_on_oahu.wakeAll();
					child_on_oahu.sleep();

				}

				else if (children_on_boat == 1) {
					//two children always bring the boat back and check if they are done
					children_on_boat++;
					bg.ChildRowToMolokai();
					bg.ChildRideToMolokai();
					boat_is_on_oahu = false;
					num_child_on_molokai++;
					num_child_on_molokai++;
					if (num_child_on_molokai == total_children && num_adult_on_molokai == total_adults) {
						//System.out.println("We're done!");
						not_done = false;
						return;
					}
					else {
						bg.ChildRowToOahu();
						num_child_on_molokai--;
						children_on_boat = 0;
						boat_is_on_oahu = true;
						child_on_oahu.wakeAll();
						adult_on_oahu.wakeAll();
						child_on_oahu.sleep();
					}
				}

				else {
					child_on_oahu.sleep();
				}

			}
		}



		//last release
		//lock.release();
	}

	static void SampleItinerary()
	{
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

}
