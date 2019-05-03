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
    printf("The test.txt creation successful. Let's try to create\n");
    printf("test.txt again.\n\n");

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
    printf("The open of helloworld.txt was successful!\n\n");
    return openTest;
  }
}


int readAndWriteTest(int wFD, int rFD, char *buffer, int count)
{
  printf("Read 11 bytes from helloworld.txt into virtual memory.\n");

  if (read(rFD, buffer, count) == 11)
  {
    printf("11 bytes have been read into virtual memory.\n");
    printf("They read->%s\n\n", buffer);
  }

  else
  {
    printf("11 bytes have not been read. Error. Halting!\n\n");
    halt();
  }

  printf("Write 11 bytes from virtual memory into test.txt.\n");

  if (write(wFD, buffer, count) == 11)
  {
    printf("11 bytes have been written into test.txt.\n");
    printf("Writing the same 11 bytes to stdout. They say...\n");
    if (write(1, buffer, count) == 11)
    {
      printf("\nRead and write test successful.\n\n");
    }

    else
    {
      printf("\nError. Halting!\n\n");
      halt();
    }

  }

  else
  {
    printf("11 bytes have not been written. Error. Halting!\n\n");
  }
}


int
main()
{
  char* realBuffer = "a";
  printf("Testing program for Task 1.\n\n");
  int testLoc = createTest();
  int testOpen = openTest();
  readAndWriteTest(testLoc, testOpen, realBuffer, 11);

  printf("address of buffer = %d, content = %s\n", realBuffer, realBuffer);

  printf("Testing close.\n");
  if (close(testLoc) == 0 && close(testOpen) == 0)
    printf("test.txt and helloworld.txt successfully closed.\n\n");
  else
  {
    printf("test.txt and helloworld.txt not closed. Halting!\n\n");
    halt();
  }
  printf("Testing unlink of test.txt.\n");
  if (unlink("test.txt") == 0)
    printf("test.txt unlink worked! All tests passed! Halting! \n");
  else
    printf("test.txt unlink did not work. There is a problem. Halting! \n");
  printf("End of testing program for Task 1. Time to halt.\n\n");
  halt();
}
