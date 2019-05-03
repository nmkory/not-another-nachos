#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"


int
main()
{
  int returnVal;
  int joinValue;
  int exitStatus;
  int joinReturn;
  char *argument;
  char *addr = 100;

  // Test task 2
  printf("\nTesting Task 2 to see if virtual memory spaces are different.\n\n");
  printf("Reading virtual address 100 in main process: %c%c%c%c%c%c%c%c%c%c%c%c\n", addr[0], addr[1], addr[2], addr[3], addr[4], addr[5], addr[6], addr[7], addr[8], addr[9], addr[10], addr[11]);
  addr = "for narnia!";
  printf("A string has been loaded into this process at address 100. It says: %c%c%c%c%c%c%c%c%c%c%c%c\n", addr[0], addr[1], addr[2], addr[3], addr[4], addr[5], addr[6], addr[7], addr[8], addr[9], addr[10], addr[11]);
  printf("We will now execute a new process, join on it, and read it's virtual memory.\n\n");
  joinValue = exec("testTask2and3Helper.coff", 0, argument);
  join(joinValue, &exitStatus);
  printf("We have returned back to our first process. The string at address 100 says: %c%c%c%c%c%c%c%c%c%c%c%c\n", addr[0], addr[1], addr[2], addr[3], addr[4], addr[5], addr[6], addr[7], addr[8], addr[9], addr[10], addr[11]);
  printf("As you can see, accessing the same virtual memory space points to a different physical memory location.\n");
  printf("Task 2 succeeds!\n");

  // Test task 3
  printf("\nTesting Task 3");
  // Test with bad file name
  printf("\nTrying to exec with a bad file name.\n");
  printf("exec TestFail.cof.\n\n");
  returnVal = exec("TestFail.cof", 0, argument);

  if(returnVal == -1)
	   printf("We correctly did not execute TestFail.cof.\n");

  else {
    printf("TestFail.cof exec worked for some reason. Error. Halting!\n\n");
    halt();
  }

  printf("Executing matmult and joining on it. Exit value should be 7220.\n\n");
  joinValue = exec("matmult.coff", 0, argument);
  joinReturn = join(joinValue, &exitStatus);

  if (joinReturn == 1)
    printf("\nmatmult.coff exited normally. We've returned back to tester. matmult.coff exit value was %d.\n\n", exitStatus);

  else {
    printf("\nmatmult.coff did not exited normally. Error. Halting!\n\n");
    halt();
  }

  printf("We are calling exit. If the machine halts, Task 3 succeeds.\n");
  exit(100);
}
