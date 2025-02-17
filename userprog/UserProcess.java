package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Encapsulates the state of a user process that is not contained in its user
 * thread (or threads). This includes its address translation state, a file
 * table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see nachos.vm.VMProcess
 * @see nachos.network.NetProcess
 */
public class UserProcess {
	/**
	 * Allocate a new process.
	 */
	public UserProcess() {
		// Passing all 24 test cases.
		// Set Kernel.shellProgram = testTask1.coff to run Task 1 tests.
		// Set Kernel.shellProgram = testTask2and3.coff to run Task 2 and 3 tests.
		
		// Project 2 Task 2: comment out and place in loadSection
		//int numPhysPages = Machine.processor().getNumPhysPages();

		//pageTable = new TranslationEntry[numPhysPages];
		//for (int i = 0; i < numPhysPages; i++)
		//	pageTable[i] = new TranslationEntry(i, i, true, false, false, false);
		
		// Project 2 Task 1: Initialize OpenFiles array
		myFileSlots = new OpenFile[16];
		
		// Project 2 Task 1:  Initialize stdin/stdout slots in OpenFiles array
		// File descriptor 0 refers to keyboard input (UNIX stdin)
		myFileSlots[0] = UserKernel.console.openForReading();
		// File descriptor 1 refers to display output (UNIX stdout)
		myFileSlots[1] = UserKernel.console.openForWriting();
		
		// Project 2 Task 3: Initialize counters and children
		children = new ArrayList<UserProcess>();
		childrenStatus = new HashMap<Integer, Integer>();
		lock.acquire();
		processID = processIDCounter;
		processIDCounter++;	
		lock.release();
	}

	/**
	 * Allocate and return a new process of the correct class. The class name is
	 * specified by the <tt>nachos.conf</tt> key <tt>Kernel.processClassName</tt>.
	 *
	 * @return a new process of the correct class.
	 */
	public static UserProcess newUserProcess() {
		return (UserProcess) Lib.constructObject(Machine.getProcessClassName());
	}

	/**
	 * Execute the specified program with the specified arguments. Attempts to load
	 * the program, and then forks a thread to run it.
	 *
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the program was successfully executed.
	 */
	public boolean execute(String name, String[] args) {
		//System.out.println("Process ID " + processID + " is executing " + name);
		if (!load(name, args))
			return false;

		processThread = new UThread(this);
		processThread.setName(name).fork();

		return true;
	}

	/**
	 * Save the state of this process in preparation for a context switch. Called by
	 * <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		Machine.processor().setPageTable(pageTable);
	}

	/**
	 * Read a null-terminated string from this process's virtual memory. Read at
	 * most <tt>maxLength + 1</tt> bytes from the specified address, search for the
	 * null terminator, and convert it to a <tt>java.lang.String</tt>, without
	 * including the null terminator. If no null terminator is found, returns
	 * <tt>null</tt>.
	 *
	 * @param vaddr     the starting virtual address of the null-terminated string.
	 * @param maxLength the maximum number of characters in the string, not
	 *                  including the null terminator.
	 * @return the string read, or <tt>null</tt> if no null terminator was found.
	 */
	public String readVirtualMemoryString(int vaddr, int maxLength) {
		Lib.assertTrue(maxLength >= 0);

		byte[] bytes = new byte[maxLength + 1];

		int bytesRead = readVirtualMemory(vaddr, bytes);

		for (int length = 0; length < bytesRead; length++) {
			if (bytes[length] == 0)
				return new String(bytes, 0, length);
		}

		return null;
	}

	/**
	 * Transfer data from this process's virtual memory to all of the specified
	 * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 *
	 * @param vaddr the first byte of virtual memory to read.
	 * @param data  the array where the data will be stored.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data) {
		return readVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from this process's virtual memory to the specified array. This
	 * method handles address translation details. This method must <i>not</i>
	 * destroy the current process if an error occurs, but instead should return the
	 * number of bytes successfully copied (or zero if no data could be copied).
	 *
	 * @param vaddr  the first byte of virtual memory to read.
	 * @param data   the array where the data will be stored.
	 * @param offset the first byte to write in the array.
	 * @param length the number of bytes to transfer from virtual memory to the
	 *               array.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		if (vaddr < 0 || vaddr > lastVAddr)
			return 0;
		
		// Amount to transfer is length of data or remaining amount of vmemory.
		int amount = Math.min(length, lastVAddr - vaddr + 1);
		final int finalAmount = amount;
		
		// totalAmountCopied is our return value.
		int totalAmountCopied;
		for (totalAmountCopied = 0; totalAmountCopied < finalAmount; )
		{
			// Get virtual page number.
			int vpn = Processor.pageFromAddress(vaddr);
			
			// Get offset of the address.
			int addressOffset = Processor.offsetFromAddress(vaddr);
			
			// Get physical page number using the virtual page number.
			int ppn = pageTable[vpn].ppn;
			
			// Make the physical address using the physical page num and offset.
			int physicalAddress = Processor.makeAddress(ppn, addressOffset);
			
			// Set amountToCopyInThisIteration based on the amount left or page minus offset.
			int amountToCopyInThisIteration = Math.min(amount, pageSize - addressOffset);
			
			// Copy that chunk of memory into the byte array.
			System.arraycopy(memory, physicalAddress, data, offset + totalAmountCopied, amountToCopyInThisIteration);
			
			// Increment total amount copied by the amount we just copied.
			totalAmountCopied += amountToCopyInThisIteration;
			
			// Increment the virtual address by the amount we just copied.
			vaddr += amountToCopyInThisIteration;
			
			// Decrement amount by the amount we just copied.
			amount -= amountToCopyInThisIteration;
		}  // After for loop, we have copied as much as we can.

		return totalAmountCopied;
	}  //readVirtualMemory()

	/**
	 * Transfer all data from the specified array to this process's virtual memory.
	 * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 *
	 * @param vaddr the first byte of virtual memory to write.
	 * @param data  the array containing the data to transfer.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data) {
		return writeVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from the specified array to this process's virtual memory. This
	 * method handles address translation details. This method must <i>not</i>
	 * destroy the current process if an error occurs, but instead should return the
	 * number of bytes successfully copied (or zero if no data could be copied).
	 *
	 * @param vaddr  the first byte of virtual memory to write.
	 * @param data   the array containing the data to transfer.
	 * @param offset the first byte to transfer from the array.
	 * @param length the number of bytes to transfer from the array to virtual
	 *               memory.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		// for now, just assume that virtual addresses equal physical addresses
		if (vaddr < 0 || vaddr > lastVAddr)
			return 0;

		// Amount to transfer is length of data or remaining amount of vmemory.
		int amount = Math.min(length, lastVAddr - vaddr + 1);
		final int finalAmount = amount;
		
		// totalAmountCopied is our return value.
		int totalAmountCopied;
		for (totalAmountCopied = 0; totalAmountCopied < finalAmount; )
		{
			// Get virtual page number.
			int vpn = Processor.pageFromAddress(vaddr);
			
			// Check if page is marked as readOnly.
			if (pageTable[vpn].readOnly == true)
				return totalAmountCopied;
			
			// Get offset of the address.
			int addressOffset = Processor.offsetFromAddress(vaddr);
			
			// Get physical page number using the virtual page number.
			int ppn = pageTable[vpn].ppn;
			
			// Make the physical address using the physical page num and offset.
			int physicalAddress = Processor.makeAddress(ppn, addressOffset);
			
			// Set amountToCopyInThisIteration based on the amount left or page minus offset.
			int amountToCopyInThisIteration = Math.min(amount, pageSize - addressOffset);
			
			// Copy that chunk of memory into the byte array.
			System.arraycopy(data, offset + totalAmountCopied, memory, physicalAddress, amountToCopyInThisIteration);
			
			// Increment total amount copied by the amount we just copied.
			totalAmountCopied += amountToCopyInThisIteration;
			
			// Increment the virtual address by the amount we just copied.
			vaddr += amountToCopyInThisIteration;
			
			// Decrement amount by the amount we just copied.
			amount -= amountToCopyInThisIteration;
		}  // After for loop, we have copied as much as we can.

		return totalAmountCopied;
	}  //writeVirtualMemory()

	/**
	 * Load the executable with the specified name into this process, and prepare to
	 * pass it the specified arguments. Opens the executable, reads its header
	 * information, and copies sections and arguments into this process's virtual
	 * memory.
	 *
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the executable was successfully loaded.
	 */
	private boolean load(String name, String[] args) {
		Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

		OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
		if (executable == null) {
			Lib.debug(dbgProcess, "\topen failed");
			return false;
		}

		try {
			coff = new Coff(executable);
		} catch (EOFException e) {
			executable.close();
			Lib.debug(dbgProcess, "\tcoff load failed");
			return false;
		}

		// make sure the sections are contiguous and start at page 0
		numPages = 0;
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
			if (section.getFirstVPN() != numPages) {
				coff.close();
				Lib.debug(dbgProcess, "\tfragmented executable");
				return false;
			}
			numPages += section.getLength();
		}

		// make sure the argv array will fit in one page
		byte[][] argv = new byte[args.length][];
		int argsSize = 0;
		for (int i = 0; i < args.length; i++) {
			argv[i] = args[i].getBytes();
			// 4 bytes for argv[] pointer; then string plus one for null byte
			argsSize += 4 + argv[i].length + 1;
		}
		if (argsSize > pageSize) {
			coff.close();
			Lib.debug(dbgProcess, "\targuments too long");
			return false;
		}

		// program counter initially points at the program entry point
		initialPC = coff.getEntryPoint();

		// next comes the stack; stack pointer initially points to top of it
		numPages += stackPages;
		initialSP = numPages * pageSize;

		// and finally reserve 1 page for arguments
		numPages++;
		
		// Project 2 Task 2: Initialize last vaddr
		lastVAddr = Machine.processor().makeAddress(numPages - 1, pageSize - 1);

		if (!loadSections())
			return false;

		// store arguments in last page
		int entryOffset = (numPages - 1) * pageSize;
		int stringOffset = entryOffset + args.length * 4;

		this.argc = args.length;
		this.argv = entryOffset;

		for (int i = 0; i < argv.length; i++) {
			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
			Lib.assertTrue(writeVirtualMemory(entryOffset, stringOffsetBytes) == 4);
			entryOffset += 4;
			Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) == argv[i].length);
			stringOffset += argv[i].length;
			Lib.assertTrue(writeVirtualMemory(stringOffset, new byte[] { 0 }) == 1);
			stringOffset += 1;
		}

		return true;
	}

	/**
	 * Allocates memory for this process, and loads the COFF sections into memory.
	 * If this returns successfully, the process will definitely be run (this is the
	 * last step in process initialization that can fail).
	 *
	 * @return <tt>true</tt> if the sections were successfully loaded.
	 */
	protected boolean loadSections() {
		// If the number of pages for this process is greater than number of
		// physical pages or if number of pages for this process is greater than
		// the number of physical page numbers.
		if (numPages > Machine.processor().getNumPhysPages() || numPages > UserKernel.availPPN.size()) {
			coff.close();
			Lib.debug(dbgProcess, "\tinsufficient physical memory");
			return false;
		}  // Else we have enough room to load these sections.
		
		// Create the pageTable for this process.
		pageTable = new TranslationEntry[numPages];
		
		// For each page, give it a physical page number location.
		for (int i = 0; i < numPages; i++)
			pageTable[i] = new TranslationEntry(i, UserKernel.availPPN.poll(), true, false, false, false);

		// Load sections.
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
			
			// Read in bool to see if this section is read only.
			Boolean isReadOnly = section.isReadOnly();

			Lib.debug(dbgProcess,
					"\tinitializing " + section.getName() + " section (" + section.getLength() + " pages)");

			for (int i = 0; i < section.getLength(); i++) {
				int vpn = section.getFirstVPN() + i;
				if (isReadOnly)
					pageTable[vpn].readOnly = true;
					
				// need ppn at this point
				// for now, just assume virtual addresses=physical addresses
				section.loadPage(i, pageTable[vpn].ppn);
			}
		}

		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		for (int i = 0; i < numPages; i++)
			UserKernel.availPPN.add(pageTable[i].ppn);
	}

	/**
	 * Initialize the processor's registers in preparation for running the program
	 * loaded into this process. Set the PC register to point at the start function,
	 * set the stack pointer register to point at the top of the stack, set the A0
	 * and A1 registers to argc and argv, respectively, and initialize all other
	 * registers to 0.
	 */
	public void initRegisters() {
		Processor processor = Machine.processor();

		// by default, everything's 0
		for (int i = 0; i < processor.numUserRegisters; i++)
			processor.writeRegister(i, 0);

		// initialize PC and SP according
		processor.writeRegister(Processor.regPC, initialPC);
		processor.writeRegister(Processor.regSP, initialSP);

		// initialize the first two argument registers to argc and argv
		processor.writeRegister(Processor.regA0, argc);
		processor.writeRegister(Processor.regA1, argv);
	}

	/**
	 * Handle the halt() system call.
	 */
	private int handleHalt() {
		Machine.halt();

		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;
	}
	
	
	/**
	 * <tt>Project 2 Task 3</tt> Terminate the current process immediately. 
	 * Any open file descriptors belonging to the process are closed. Any 
	 * children of the process no longer have a parent process.
	 *
	 * @param status is returned to the parent process as this process's exit 
	 * status and can be collected using the join syscall.
	 * @return exit() never returns.
	 */
	private int handleExit(int status) {
		unloadSections();
		
		// Close all open file descriptors belonging to the process.
		for (int i = 0; i < 16; i++) {
			if (myFileSlots[i] != null) {
				myFileSlots[i].close();
				myFileSlots[i] = null;
			}  // If there was an open file in that loc, close/null FD.
		}  // All open file descriptors belonging to the process are now closed.
		
		// Children of this process no longer have a parent process.
		for (int i = 0; i < children.size(); i++) {
			children.get(i).parent = null;
		}
		
		// Status is returned to the parent process as this process's exit
		//System.out.println("exit status " + status);
		if (parent != null)
			parent.childrenStatus.put(processID, status);
		
		// If not last exiting process.
		if (processID != 0)
			UThread.currentThread().finish();
		else  // We are last exiting process.
			Kernel.kernel.terminate();
		
		// exit() never returns.
		return 0;		
	}  //handleExit()
	
	
	/**
	 * <tt>Project 2 Task 3</tt> Execute the program stored in the specified 
	 * file, with the specified arguments, in a new child process. The child 
	 * process has a new unique process ID, and starts with stdin opened as file
	 * descriptor 0, and stdout opened as file descriptor 1.
	 *
	 * @param fileName the name of the file containing the executable. Note that
	 * this string must include the ".coff" extension.
	 * @param numArgs the number of arguments to pass to the child process.
	 * @param aryArgs array of pointers to null-terminated strings that 
	 * represent the arguments to pass to the child process.
	 * @return exec() returns the child process's process ID, which can be 
	 * passed to join(). On error, returns -1.
	 */
	private int handleExec(int fileName, int numArgs, int addressOfAddresses) {
		byte[] tempAddress = new byte[4];
		int argAddress;
		//ByteBuffer buf;
		String fName = readVirtualMemoryString(fileName, 256);
		
		if (fName == null || !(fName.endsWith(".coff")))
			return -1;
		
		String[] myArgs = new String[numArgs];
		
		for (int i = 0; i < numArgs; i++) {
			readVirtualMemory(addressOfAddresses, tempAddress);
			argAddress = Lib.bytesToInt(tempAddress, 0);
			//buf = ByteBuffer.wrap(tempAddress);
			//argAddress = buf.getInt();
			myArgs[i] = readVirtualMemoryString(argAddress, 256);
			addressOfAddresses += 4;
		}
		
//		for (int i = 0; i < numArgs; i++) {
//			System.out.println(myArgs[i]);
//		}
		
		UserProcess process = UserProcess.newUserProcess();
	
		process.parent = this;
		children.add(process);
		childrenStatus.put(process.processID, 1);
		
		process.execute(fName, myArgs);
		return process.processID;	
	}  //handleExec()
	
	
	/**
	 * <tt>Project 2 Task 3</tt> Suspend execution of the current process until 
	 * the child process specified by the processID argument has exited. If the 
	 * child has already exited by the time of the call, returns immediately. 
	 * When the current process resumes, it disowns the child process, so that 
	 * join() cannot be used on that process again.
	 *
	 * @param processID is the process ID of the child process, returned by
	 * exec().
	 * @param exitStatusAddr points to an integer where the exit status of the child 
	 * process will be stored.
	 * @return If the child exited normally, returns 1. If the child exited as a
	 * result of an unhandled exception, returns 0. If processID does not refer
	 * to a child process of the current process, returns -1.
	 */
	private int handleJoin(int processID, int exitStatusAddr) {
		int i;
		int childExitStatus;
		for (i = 0; i < children.size(); i++) {
			if (children.get(i).processID == processID)
				break;
		}
		if (i == children.size())
		{
			//System.out.println(i);
			return -1;
		}
		children.get(i).processThread.join();
		
		childExitStatus = childrenStatus.get(children.get(i).processID);
		//System.out.println("join exit status " + childExitStatus);
		
		
		byte [] statusByteAry = new byte[4];

		Lib.bytesFromInt(statusByteAry, 0, 4, childExitStatus);
		writeVirtualMemory(exitStatusAddr, statusByteAry);	
		
		// If child experience an unhandled exception.
		if (childExitStatus >= 1 && childExitStatus <= 7)
			return 0;
		else  // Else child has a different exit status.
			return 1;		
	}  //handleJoin()
	
	
	/**
	 * <tt>Project 2 Task 1</tt> Attempt to open the named disk 
	 * file, creating it if it does not exist, and return a file descriptor that
	 * can be used to access the file.
	 *
	 * @param myAddr the starting virtual address of the null-terminated string.
	 * @return the new file descriptor, or -1 if an error occurred.
	 */
	private int handleCreate(int myAddr) {
		// Comment out to test using Eclipse, use a string fName
		// Check to make sure myAddr is a valid parameter
		if (myAddr < 0)
			return -1;
		
		// Comment out to test using Eclipse, use a string fName
		// Use the parameter to pull the name of file to create from vMemory
		String fName = readVirtualMemoryString(myAddr, 256);
		
		if (fName == null)
			return -1;
		
		// Attempt to open the file to be created to see if it already exists.
		OpenFile tempFile = ThreadedKernel.fileSystem.open(fName, false);
		
		// Check if file is made.
		if (tempFile != null) {
			tempFile.close();
			return -1;
		}  // If tempFile != null, the file was not already made.
		
		// Add file to array if there is space to add the file.
		for (int i = 0; i < 16; i++) {
			if (myFileSlots[i] == null) {
				// Create the file in the open slot.
				myFileSlots[i] = ThreadedKernel.fileSystem.open(fName, true);
				Lib.debug(dbgProcess, "Created " + myFileSlots[i].getName());
				
				// Check to make sure creation was successful.
				if (myFileSlots[i] != null)
					return i;
				else
					return -1;
			}  // If myFileSlots[i] is not null, loop again.
		}
		// If we reach end of for loop, no room in ary so return -1.
		return -1;	
	}  //handleCreate()
	
	/**
	 * <tt>Project 2 Task 1</tt> Attempt to open the named file and return a 
	 * file descriptor.
	 *
	 * @param myAddr the starting virtual address of the null-terminated string.
	 * @return the new file descriptor, or -1 if an error occurred.
	 */
	private int handleOpen(int myAddr) {
		// Comment out to test using Eclipse, use a string fName
		// Check to make sure myAddr is a valid parameter
		if (myAddr < 0)
			return -1;
		
		// Comment out to test using Eclipse, use a string fName
		// Use the parameter to pull the name of file to open from vMemory
		String fName = readVirtualMemoryString(myAddr, 256);
		
		if (fName == null)
			return -1;
		
		// Attempt to open the file if it already exists.
		OpenFile tempFile = ThreadedKernel.fileSystem.open(fName, false);
		
		// Check if file was found and opened.
		if (tempFile == null) {
			return -1;
		}  // If tempFile was not null, then we found and opened the file.
		
		// Add file to array if there is space to add the file.
		for (int i = 0; i < 16; i++) {
			if (myFileSlots[i] == null) {
				// Place open file in this slot.
				myFileSlots[i] = tempFile;
				Lib.debug(dbgProcess, "Opened " + myFileSlots[i].getName());
				
				// Check to make sure we correctly loaded open file into slot.
				if (myFileSlots[i] != null)
					return i;
				else
					tempFile.close();
					return -1;
			}  // If myFileSlots[i] is not null, loop again.
		}
		// If we reach end of for loop, no room in ary so close and return -1.
		tempFile.close();
		return -1;
	}  //handleOpen()
	
	
	/**
	 * <tt>Project 2 Task 1</tt> Attempt to read up to count bytes into buffer
	 * from the file or stream referred to by fileDescriptor.
	 *
	 * @param slotNum location of file in our myFileSlots array
	 * @param vaddr the starting virtual address of the null-terminated string.
	 * @param numBytes the number of bytes to be read.
	 * @return the number of bytes read is returned, on error, -1 is returned.
	 */
	private int handleRead(int slotNum, int vaddr, int numBytes) {
		// Validation checks to make sure parameters are valid.
		if (myFileSlots[slotNum] == null || slotNum < 0 || slotNum >= 16
			|| vaddr < 0 || numBytes <= 0)
			return -1;
		
		// Initialize byte[] for reading in data.
		byte[] dataToBeFilled = new byte[numBytes];
		
		// Read bytes into dataToBeFilled and store int into bytesReadFromFile.
		int bytesReadFromFile = myFileSlots[slotNum].read(dataToBeFilled, 0, numBytes);
		
		// Uncomment for Eclipse testing - print what was read from the file.
		//String s = new String(dataToBeFilled);
		//System.out.println(s);
		
		// Write bytes from dataToBeFilled into vMemory and store int.
		int bytesWrittenToAddr = writeVirtualMemory(vaddr, dataToBeFilled);
		
		// Validate bytesReadFromFile with bytesWrittenToAddr
		if (bytesReadFromFile == bytesWrittenToAddr)
			return bytesReadFromFile;
		else  // Else there was an error so return -1.
			return -1;
	}  //handleRead()
	
	/**
	 * <tt>Project 2 Task 1</tt> Attempt to write up to count bytes from buffer
	 * to the file or stream referred to by fileDescriptor.
	 *
	 * @param slotNum location of file in our myFileSlots array
	 * @param vaddr the starting virtual address of the null-terminated string.
	 * @param numBytes the number of bytes to be written.
	 * @return the number bytes written is returned, on error, -1 is returned.
	 */
	private int handleWrite(int slotNum, int vaddr, int numBytes) {
		// Validation checks to make sure parameters are valid.
		if (myFileSlots[slotNum] == null || slotNum < 0 || slotNum >= 16
			|| vaddr < 0 || vaddr > lastVAddr || numBytes < 0)
			return -1;
		
		if (numBytes == 0)
			return 0;

		// Initialize byte[] for writing in data.
		byte[] dataToBeFilled = new byte[numBytes];
		
		// Read bytes into bytesReadFromAddr that will be written.
		int bytesReadFromAddr = readVirtualMemory(vaddr, dataToBeFilled);
		
		// Uncomment for Eclipse testing - print what was read from the file.
		//String s = new String(dataToBeFilled);
		//System.out.println(s);
		
		// Write bytes from bytesWriteToFile into file and store int.
		int bytesWriteToFile = myFileSlots[slotNum].write(dataToBeFilled, 0, numBytes);
		
		// Validate bytesReadFromAddr with bytesWriteToFile
		if (bytesReadFromAddr == bytesWriteToFile)
			return bytesReadFromAddr;
		else  // Else there was an error so return -1.
			return -1;
	}  //handleWrite()
	
	/**
	 * <tt>Project 2 Task 1</tt> Close a file descriptor, so that it no longer
	 * refers to any file or stream and may be reused.
	 *
	 * @param slotNum location of file in our myFileSlots array
	 * @return 0 on success, or -1 if an error occurred.
	 */
	private int handleClose(int slotNum) {
		// Validation checks to make sure parameters are valid.
		if (myFileSlots[slotNum] == null || slotNum <= 1)
			return -1;
		
		// Close file and set slot to null.
		myFileSlots[slotNum].close();
		myFileSlots[slotNum] = null;
		
		return 0;
	}  //handleClose()
	
	/**
	 * <tt>Project 2 Task 1</tt> Delete a file from the file system. If no
	 * processes have the file open, the file is deleted immediately and the
	 * space it was using is made available for reuse.
	 *
	 * @param myAddr the starting virtual address of the null-terminated string.
	 * @return 0 on success, or -1 if an error occurred.
	 */
	private int handleUnlink(int myAddr) {
		// Validation checks to make sure parameters are valid.
		if (myAddr < 0)
			return -1;
	
		// Use the parameter to pull the name of file to create from vMemory.
		String fName = readVirtualMemoryString(myAddr, 256);
		
		// Remove that file and return 0.
		if (ThreadedKernel.fileSystem.remove(fName))
			return 0;
		else  // Else the remove failed so return -1.
			return -1;
	}  //handleUnlink()
	

	private static final int syscallHalt = 0, syscallExit = 1, syscallExec = 2, syscallJoin = 3, syscallCreate = 4,
			syscallOpen = 5, syscallRead = 6, syscallWrite = 7, syscallClose = 8, syscallUnlink = 9;

	/**
	 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
	 * <i>syscall</i> argument identifies which syscall the user executed:
	 *
	 * <table>
	 * <tr>
	 * <td>syscall#</td>
	 * <td>syscall prototype</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td><tt>void halt();</tt></td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td><tt>void exit(int status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td><tt>int  exec(char *name, int argc, char **argv);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td><tt>int  join(int pid, int *status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td><tt>int  creat(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>5</td>
	 * <td><tt>int  open(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>6</td>
	 * <td><tt>int  read(int fd, char *buffer, int size);
	 *								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>7</td>
	 * <td><tt>int  write(int fd, char *buffer, int size);
	 *								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>8</td>
	 * <td><tt>int  close(int fd);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>9</td>
	 * <td><tt>int  unlink(char *name);</tt></td>
	 * </tr>
	 * </table>
	 * 
	 * @param syscall the syscall number.
	 * @param a0      the first syscall argument.
	 * @param a1      the second syscall argument.
	 * @param a2      the third syscall argument.
	 * @param a3      the fourth syscall argument.
	 * @return the value to be returned to the user.
	 */
	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
		case syscallHalt:			
			return handleHalt();
		case syscallExit:
			return handleExit(a0);
		case syscallExec:
			return handleExec(a0, a1, a2);
		case syscallJoin:
			return handleJoin(a0, a1);
		case syscallCreate:
			return handleCreate(a0);
		case syscallOpen:
			return handleOpen(a0);
		case syscallRead:
			return handleRead(a0, a1, a2);
		case syscallWrite:
			return handleWrite(a0, a1, a2);
		case syscallClose:
			return handleClose(a0);
		case syscallUnlink:
			return handleUnlink(a0);

		default:
			Lib.debug(dbgProcess, "Unknown syscall " + syscall);
			Lib.assertNotReached("Unknown system call! Syscall: " + syscall);
		}
		return 0;
	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>.
	 * The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 *
	 * @param cause the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
		case Processor.exceptionSyscall:
			int result = handleSyscall(processor.readRegister(Processor.regV0), processor.readRegister(Processor.regA0),
					processor.readRegister(Processor.regA1), processor.readRegister(Processor.regA2),
					processor.readRegister(Processor.regA3));
			processor.writeRegister(Processor.regV0, result);
			processor.advancePC();
			break;

		default:
			handleExit(cause);
			Lib.debug(dbgProcess, "Unexpected exception: " + Processor.exceptionNames[cause]);
			Lib.assertNotReached("Unexpected exception");
		}
	}

	// Project 2 Task 1: Array of OpenFiles for the opened files in this process
	protected OpenFile[] myFileSlots;
	
	// Project 2 Task 2: last vaddr
	protected int lastVAddr;
	
	// Project 2 Task 3: processID of this process
	protected int processID;
	
	// Project 2 Task 3: counter of processIDs
	private static int processIDCounter = 0;
	
	// Project 2 Task 3: parent of this process
	protected UserProcess parent;
	
	// Project 2 Task 3: children of this process
	protected ArrayList<UserProcess> children;
	
	// Project 2 Task 3: children statuses of this process
	protected HashMap<Integer, Integer> childrenStatus;
	
	// Project 2 Task 3: process thread
	protected UThread processThread;
	
	/** The program being run by this process. */
	protected Coff coff;

	/** This process's page table. */
	protected TranslationEntry[] pageTable;
	/** The number of contiguous pages occupied by the program. */
	protected int numPages;

	/** The number of pages in the program's stack. */
	protected final int stackPages = 8;

	private int initialPC, initialSP;
	private int argc, argv;
	
	// Project 2 Task 2: lock to make process IDs atomic
	private static Lock lock = new Lock();

	private static final int pageSize = Processor.pageSize;
	private static final char dbgProcess = 'a';
}
