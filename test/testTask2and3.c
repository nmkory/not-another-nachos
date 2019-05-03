#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"


int
main()
{
  char *addressOfAddress[20];
  int returnVal;
  int joinValue;
  int exitStatus;
  int joinReturn;

  // Test with bad file name
  printf("\nTrying to exec with a bad file name.\n");
  printf("exec TestFail.cof.\n\n");
  returnVal = exec("TestFail.cof", 0, addressOfAddress);

  if(returnVal == -1)
	   printf("We correctly did not execute TestFail.cof.\n");

  else {
    printf("TestFail.cof exec worked for some reason. Error. Halting!\n\n");
    halt();
  }

  printf("Executing matmult and joining on it. Exit value should be 7220.\n\n");
  joinValue = exec("matmult.coff", 0, addressOfAddress);
  joinReturn = join(joinValue, &exitStatus);

  if (joinReturn == 1)
    printf("\nmatmult.coff exited normally. We've returned back to tester. matmult.coff exit value was %d.\n\n", exitStatus);

  else {
    printf("\nmatmult.coff did not exited normally. Error. Halting!\n\n");
    halt();
  }

  printf("We are calling exit. As we are the last process, machine should halt.\n");
  exit(100);
}
