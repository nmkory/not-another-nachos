package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
	static BoatGrader bg;
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

//		System.out.println("\n ***Testing Boats with 4 children, 2 adult***");
//		begin(2, 2, b);

		//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		//  	begin(3, 3, b);
	}

	public static void begin( int adults, int children, BoatGrader b )
	{
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;

		// Instantiate global variables here
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

		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.

		//		Runnable r = new Runnable() {
		//			public void run() {
		//				SampleItinerary();
		//			}
		//		};
		//		KThread t = new KThread(r);
		//		t.setName("Sample Boat Thread");
		//		t.fork();

		Runnable r_child = new Runnable() {
			public void run() {
				ChildItinerary();
			}
		};

		Runnable r_adult = new Runnable() {
			public void run() {
				AdultItinerary();
			}
		};
		
		for (int i = 0; i < adults; i++) {
			new KThread(r_adult).setName("Adult " + Integer.toString(i+1)).fork();
		}
		
		for (int i = 0; i < children; i++) {
			new KThread(r_child).setName("Child " + Integer.toString(i+1)).fork();
		}




	}

	static void AdultItinerary()
	{
		lock.acquire();
		//System.out.println(num_child_on_molokai);
		while(true) {
			//[TODO] fix variable so it knows how many children on Oahu
			//This adult can know how many children are on molokai because it has
			//seen them leave Oahu
			if (!boat_is_on_oahu || (total_children - num_child_on_molokai) > 1) {
				//go to sleep if boat is not there or let children go first
				child_on_oahu.wakeAll();
				adult_on_oahu.sleep();
			}
			
			else {
				if (children_on_boat == 0) {
					//row yourself to the other island and wake a child up so they can bring the boat back
					bg.AdultRowToMolokai();
					num_adult_on_molokai++;
					boat_is_on_oahu = false;
					child_on_molokai.wake();
					adult_on_molokai.sleep();
				}
				

			}
		}



		//last release
		//lock.release();

		/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
		 */

	}

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
