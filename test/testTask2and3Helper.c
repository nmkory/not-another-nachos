#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  char *addr = 100;
  printf("\nWe are inside a new process. Virtual memory is the same but physical memory is different.\n");
  printf("Reading virtual address 100 in this process: %c%c%c%c%c%c%c%c%c%c%c%c\n", addr[0], addr[1], addr[2], addr[3], addr[4], addr[5], addr[6], addr[7], addr[8], addr[9], addr[10], addr[11]);
  addr = "hello world!";
  printf("A string has been loaded into this process at address 100. It says: %c%c%c%c%c%c%c%c%c%c%c%c\n", addr[0], addr[1], addr[2], addr[3], addr[4], addr[5], addr[6], addr[7], addr[8], addr[9], addr[10], addr[11]);
  printf("Returning from this process\n\n");
  return 0;
}
