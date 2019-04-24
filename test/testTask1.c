/* testTask1.c
 *	Simple program to test syscalls from Task 1.
 */

#include "syscall.h"

int
main()
{
    creat("test.txt");
    /* not reached */
}
