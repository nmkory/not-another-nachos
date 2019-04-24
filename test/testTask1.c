/* testTask1.c
 *	Simple program to test syscalls from Task 1.
 */

#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"


int createTest()
{
  printf("Trying to create a new file called test.txt.\n\n");

  int createTest = creat("test.txt");

  if (createTest == -1)
  {
    printf("test.txt already exists, perhaps from an older test. Removing\n");
    printf("it. Please run this test again after we halt.\n\n");
    unlink("test.txt");
    halt();
  }

  else
    printf("test.txt created. Let's try to create test.txt again.\n\n");

  if (creat("test.txt") == -1)
  {
    printf("Create test.txt (correctly) did not work the second time.\n\n");
    return createTest;
  }

  else
  {
    printf("test.txt created again for some reason. Error. Halting!\n\n");
    close(createTest);
    unlink("test.txt");
    halt();
  }
}


int openTest()
{
  printf("Attempting to open a file that does not exist called\n");
  printf("helloearth.txt\n\n");

  int openTest = open("helloearth.txt");

  if (openTest == -1)
    printf("helloearth.txt open correctly failed.\n\n");

  else
  {
    printf("helloearth.txt opened for some reason. Error. Halting!\n\n");
    halt();
  }

  printf("Attempting to open a file that does exist called\n");
  printf("helloworld.txt\n\n");

  openTest = open("helloworld.txt");

  if (openTest == -1)
  {
    printf("helloworld.txt not opened for some reason. Error. Halting!\n\n");
    halt();
  }

  else
  {
    printf("helloworld.txt opened!\n\n");
    return openTest;
  }
}


int
main()
{
  int testLoc = createTest();
  int testOpen = openTest();

  close(testLoc);
  if (unlink("test.txt") == 0)
    printf("Final unlink worked! All tests passed! Halting! \n");
  else
    printf("Final unlink did not work. There is a problem. Halting! \n");
  printf("End of testing program for Task 1.\n");
  halt();
}
