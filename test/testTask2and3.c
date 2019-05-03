#include "syscall.h"
#include "stdio.h"
#include "stdlib.h"


char *execfile;
int procID[20];
int numArgs;
char *addressOfAddress[20];
int returnVal; 
int exitStatus;


int
main()
{


 
// Test with bad file name

 printf("Test: exec with error arguments with a bad file name\n");

numArgs = 1;
execfile = "TestFail.cof";
addressOfAddress[0] = execfile;
addressOfAddress[1] = 0;

printf("exec %s\n", execfile);
returnVal = exec(execfile, numArgs, addressOfAddress);
if(returnVal == -1){
	printf("end test\n");
}
else 
printf("failed test\n");
}



// Test with wrong argc

printf("Test: exec with error arguments with a wrong argc\n");

numArgs = 1;
execfile = "testTask1.coff";
addressOfAddress[0] = execfile;
addressOfAddress[1] = "echo.coff";

printf("exec %s\n". execfile);
returnVal = exec(execfile, numArgs, addressOfAddress);
if(returnVal != 0){
	printf("return value of exec is %d\n", returnVal);
		printf("end test\n");
}
else
	printf("failed test");



//Test join to a child

printf("test syscall join to a child\n");

numArgs 1;
execfile = "testTask1.coff";
addressOfAddress[0] = execfile;
addressOfAddress[1] = 0;

printf("exec %s\n", execfile);
procID = exec(execfile, numArgs, addressOfAddress);
printf("Child Process Id %d\n", execfile);

returnVal = join(procID[0], &exitStatus);
if(returnVal == 0){
	printf("exit status is %d\n", exitStatus);
	printf("end test\n");
}
else
printf("failed test, return value of join %d\n", returnVal);


