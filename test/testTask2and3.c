#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"


int
main()
{
  char *addressOfAddress[20];
  int returnVal;

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

}
