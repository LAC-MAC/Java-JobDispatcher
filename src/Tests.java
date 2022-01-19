import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.junit.jupiter.api.RepeatedTest;









/**
 * @author mikec
 *
 */
class Tests {
	 DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");  
	 LocalDateTime now = LocalDateTime.now();  
	
	@org.junit.jupiter.api.Test
	//The tests UR1 and 4 for compute threads
	void UR1_4_Compute() {
		System.out.println("\nUR1_4_Compute");
		JobDispatcher dispatcher = new JobDispatcher();
		
		//Specify job for 4 Compute threads and 0 Storage threads
		dispatcher.specifyJob(4, 0);
		//use a loop to create 4 compute threads adding them to a list
		Thread[] computeThreads = new Thread[4];
		for(int i = 0;i < 4; i++) {
			Thread computeThread = new Thread() {			
			public void run () {
				
				dispatcher.queueComputeThread();
				//use the print statement to know when a thread has terminated
				System.out.println("compute thread terminated" + Thread.currentThread().getName());
			}
		};	
		computeThreads[i] = computeThread;
		//start all of the threads
		computeThread.start();
		}
		

		
		
		//Wait for set time and assume that execution has finished:
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace(); };

		
		//All threads should be terminated
		assertEquals(Thread.State.TERMINATED, computeThreads[0].getState()); 
		assertEquals(Thread.State.TERMINATED, computeThreads[1].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[2].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[3].getState());
	}
	
	
	@org.junit.jupiter.api.Test
	//The tests UR1 and 4 for storage workers
	void UR1_4_Storage() {
		System.out.println("\nUR1_4_Storage");
		JobDispatcher dispatcher = new JobDispatcher();
		
		//Specify job for 0 Compute threads and 4 Storage threads
		dispatcher.specifyJob(0, 4);
		//use a loop to create 4 storage threads and add them to a list
		Thread[] storageThreads = new Thread[4];
		for(int i = 0;i < 4; i++) {
			Thread storageThread = new Thread() {			
			public void run () {
				
				dispatcher.queueStorageThread();
				//print statement to know when the threads have terminated
				System.out.println("storage thread terminated" + Thread.currentThread().getName());
			}
		};	
		storageThreads[i] = storageThread;
		//start each thread
		storageThread.start();
		}
		
		
		
		//Wait for set time and assume that execution has finished:
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace(); };

		
		//The all storage threads should have terminated: 
		assertEquals(Thread.State.TERMINATED, storageThreads[0].getState()); 
		assertEquals(Thread.State.TERMINATED, storageThreads[1].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[2].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[3].getState());
		
	}
	

	@org.junit.jupiter.api.Test
	
	//This test UR2 and 4 
	void UR2_4() {
		System.out.println("\nUR2_4");
		JobDispatcher dispatcher = new JobDispatcher();
		
		//Specify job for 1 Compute threads and 4 Storage threads
		dispatcher.specifyJob(1, 4);
		
		//Specify job for 2 Compute threads and 1 Storage threads
		dispatcher.specifyJob(2, 1);
		
		//use a loop to create 5 storage workers
		Thread[] storageThreads = new Thread[5];
		for(int i = 0;i < 5; i++) {
			Thread storageThread = new Thread() {			
			public void run () {
				
				dispatcher.queueStorageThread();
				//print statement for when the thread terminates
				System.out.println("storage thread terminated" + Thread.currentThread().getName());
			}
		};	
		storageThreads[i] = storageThread;
		//start the thread 
		storageThread.start();
		}
		
		//loop to create 4 compute worker 
		Thread[] computeThreads = new Thread[4];
		for(int i = 0;i < 4; i++) {
			Thread computeThread = new Thread() {			
			public void run () {
				
				dispatcher.queueComputeThread();
				//print statement to show the thread terminated
				System.out.println("compute thread terminated" + Thread.currentThread().getName());
			}
		};	
		computeThreads[i] = computeThread;
		//start the thread 
		computeThread.start();
		}
		

		
		
		//Wait for set time and assume that execution has finished:
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace(); };

		
		//All storage threads should be terminated
		//however the compute threads only one should be waiting and the rest should be terminated
		//depends on the order in which they are queued in. 
		assertEquals(Thread.State.TERMINATED, storageThreads[0].getState()); 
		assertEquals(Thread.State.TERMINATED, storageThreads[1].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[2].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[3].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[4].getState());
		assertTrue(
				
				((computeThreads[0].getState().equals(Thread.State.TERMINATED) 
				&& computeThreads[1].getState().equals(Thread.State.WAITING)
				&& computeThreads[2].getState().equals(Thread.State.TERMINATED) 
				&& computeThreads[3].getState().equals(Thread.State.TERMINATED) ))
				
				||
				
				((computeThreads[1].getState().equals(Thread.State.TERMINATED) 
						&& computeThreads[0].getState().equals(Thread.State.WAITING)
						&& computeThreads[2].getState().equals(Thread.State.TERMINATED) 
						&& computeThreads[3].getState().equals(Thread.State.TERMINATED) ))
				
				||
				
				((computeThreads[0].getState().equals(Thread.State.TERMINATED) 
						&& computeThreads[2].getState().equals(Thread.State.WAITING)
						&& computeThreads[1].getState().equals(Thread.State.TERMINATED) 
						&& computeThreads[3].getState().equals(Thread.State.TERMINATED) ))
				
				||
				
				((computeThreads[0].getState().equals(Thread.State.TERMINATED) 
						&& computeThreads[3].getState().equals(Thread.State.WAITING)
						&& computeThreads[2].getState().equals(Thread.State.TERMINATED) 
						&& computeThreads[1].getState().equals(Thread.State.TERMINATED) ))
			
				);
		
			
				
			
	}
	

	@org.junit.jupiter.api.Test
	//This test UR3 
	void UR3() {
		System.out.println("\nUR3");
		JobDispatcher dispatcher = new JobDispatcher();
		
		//Specify job for 1 Compute threads and 4 Storage threads
		dispatcher.specifyJob(1, 4);
		
		
		//use a loop to create 6 threads and add to a list
		Thread[] storageThreads = new Thread[6];
		for(int i = 0;i < 6; i++) {
			Thread storageThread = new Thread() {			
			public void run () {
				
				dispatcher.queueStorageThread();
				//print statement to know when thread terminated 
				System.out.println("storage thread terminated" + Thread.currentThread().getName());
			}
		};	
		storageThreads[i] = storageThread;
		//start each of the threads
		storageThread.start();
		}
		
		//Specify job for 8 Compute threads and 2 Storage threads
		dispatcher.specifyJob(8, 2);
		//use a loop to create 9 compute threads and add to a list 
		Thread[] computeThreads = new Thread[9];
		for(int i = 0;i < 9; i++) {
			Thread computeThread = new Thread() {			
			public void run () {
				
				dispatcher.queueComputeThread();
				//print statement for when thread has terminated 
				System.out.println("compute thread terminated" + Thread.currentThread().getName());
			}
		};	
		computeThreads[i] = computeThread;
		//start each thread 
		computeThread.start();
		}
		

		
		
		//Wait for set time and assume that execution has finished:
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace(); };

		
		//All compute and storage threads should be terminated 
		assertEquals(Thread.State.TERMINATED, storageThreads[0].getState()); 
		assertEquals(Thread.State.TERMINATED, storageThreads[1].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[2].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[3].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[4].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[5].getState());
		
		assertEquals(Thread.State.TERMINATED, computeThreads[0].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[2].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[3].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[4].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[5].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[6].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[7].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[8].getState());
		
	}

	
	@org.junit.jupiter.api.Test
	//This is a second test for UR3
	void UR3One() {
		System.out.println("\nUR3 One");
		JobDispatcher dispatcher = new JobDispatcher();
		
		//Specify job for 4 Compute threads and 1 Storage threads
		dispatcher.specifyJob(4, 1);
		
		
		//use a loop to create 6 new threads and add to a list
		Thread[] storageThreads = new Thread[6];
		for(int i = 0;i < 6; i++) {
			Thread storageThread = new Thread() {			
			public void run () {
				
				dispatcher.queueStorageThread();
				//print statement to show thread is terminated
				System.out.println("storage thread terminated" + Thread.currentThread().getName());
			}
		};	
		storageThreads[i] = storageThread;
		//start each of the thread
		storageThread.start();
		}
		
		
		//a loop to create 9 compute threads and add to a list
		Thread[] computeThreads = new Thread[9];
		for(int i = 0;i < 9; i++) {
			Thread computeThread = new Thread() {			
			public void run () {
				
				dispatcher.queueComputeThread();
				//print statement to show thread has terminated 
				System.out.println("compute thread terminated" + Thread.currentThread().getName());
			}
		};	
		computeThreads[i] = computeThread;
		//start each thread
		computeThread.start();
		}
		
		//Specify job for 5 Compute threads and 5 Storage threads
		dispatcher.specifyJob(5, 5);
		
		
		//Wait for set time and assume that execution has finished:
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace(); };

		
		//All compute and storage threads should be terminated
		assertEquals(Thread.State.TERMINATED, storageThreads[0].getState()); 
		assertEquals(Thread.State.TERMINATED, storageThreads[1].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[2].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[3].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[4].getState());
		assertEquals(Thread.State.TERMINATED, storageThreads[5].getState());
		
		assertEquals(Thread.State.TERMINATED, computeThreads[0].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[2].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[3].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[4].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[5].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[6].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[7].getState());
		assertEquals(Thread.State.TERMINATED, computeThreads[8].getState());
		
	}
	
	
	@org.junit.jupiter.api.Test
	//This test all the UR1-4 
	//It is a random test, it generates random number for compute and storage as well as a third randnum 
	//two jobs are generated where number of compute  workers is equal to (compute - randNum) and 
	//number storage workers is equal to ranNum. The second job is similar but storage worker number (storage - randnum) 
	void randomTest() {
		System.out.println("\nRandom Test");
		JobDispatcher dispatcher = new JobDispatcher();
		
		//new random object
		Random rand = new Random();
		//random int for compute 
		int compute = rand.nextInt(20-5) + 5; 
		//random int for storage
		int storage = rand.nextInt(20-5) + 5;
		//random number
		int randNum = rand.nextInt(5);
		//print all random numbers
		System.out.println("compute " + compute);
		System.out.println("storage " + storage);
		System.out.println("randNum " + randNum);
		//Specify job for (compute-randNum) Compute threads and randNum Storage threads
		dispatcher.specifyJob((compute-randNum), randNum);
		
		
		//loop for creating storage amount of threads and adding them to a list
		Thread[] storageThreads = new Thread[storage];
		for(int i = 0;i < storage; i++) {
			Thread storageThread = new Thread() {			
			public void run () {
				
				dispatcher.queueStorageThread();
				//print statement for when thread is terminated 
				System.out.println("storage thread terminated" + Thread.currentThread().getName());
			}
		};	
		storageThreads[i] = storageThread;
		//start each thread
		storageThread.start();
		}
		
		//Specify job for randNum Compute threads and (storage-randNum) Storage threads
		dispatcher.specifyJob(randNum, (storage-randNum));
		//loop for creating compute number of compute threads and adding to a list
		Thread[] computeThreads = new Thread[compute];
		for(int i = 0;i < compute; i++) {
			Thread computeThread = new Thread() {			
			public void run () {
				
				dispatcher.queueComputeThread();
				//print statement to show thread is terminated
				System.out.println("compute thread terminated" + Thread.currentThread().getName());
			}
		};	
		computeThreads[i] = computeThread;
		//start each thread
		computeThread.start();
		}
		
		
		
		
		//Wait for set time and assume that execution has finished:
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace(); };

		
		//Loop through list of compute threads making sure all are terminated
		for(int i = 0;i < compute; i++) {
			assertEquals(Thread.State.TERMINATED, computeThreads[i].getState());
		}
		
		//loop through list of storage threads making sure all are terminated
		for(int i = 0;i < storage; i++) {
			assertEquals(Thread.State.TERMINATED, storageThreads[i].getState());
		}
		
		
	}

	@org.junit.jupiter.api.Test
	//The tests calling the start of threads manually and in a different order 
	void test() {
		System.out.println("\ntest_manually_starting");
		JobDispatcher dispatcher = new JobDispatcher();
		
		
		//use a loop to create 6 compute threads adding them to a list
		Thread[] computeThreads = new Thread[6];
		for(int i = 0;i < 6; i++) {
			Thread computeThread = new Thread() {			
			public void run () {
				
				dispatcher.queueComputeThread();
				//use the print statement to know when a thread has terminated
				System.out.println("compute thread terminated" + Thread.currentThread().getName());
			}
		};	
		computeThreads[i] = computeThread;
		
		}
		
		//use a loop to create 1 storage thread adding them to a list
		Thread[] storageThreads = new Thread[1];
		for(int i = 0;i < 1; i++) {
			Thread storageThread = new Thread() {			
			public void run () {
				
				dispatcher.queueStorageThread();
				//use the print statement to know when a thread has terminated
				System.out.println("storage thread terminated" + Thread.currentThread().getName());
			}
		};	
		storageThreads[i] = storageThread;
		
		}
		
		//start them all myself at different times 
		computeThreads[0].start();
		computeThreads[3].start();
		computeThreads[4].start();
		computeThreads[1].start();
		computeThreads[2].start();
		computeThreads[5].start();
		storageThreads[0].start();
		

		
		//Specify job for 4 Compute threads and 1 Storage threads
		dispatcher.specifyJob(4, 1);
		//Specify job for 2 Compute threads and 0 Storage threads
		dispatcher.specifyJob(2, 0);
		
		
		//Wait for set time and assume that execution has finished:
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace(); };

		
		//All threads should be terminated
		//Loop through list of compute threads making sure all are terminated
		for(int i = 0;i < 6; i++) {
			assertEquals(Thread.State.TERMINATED, computeThreads[i].getState());
		}
				
		//loop through list of storage threads making sure all are terminated
		for(int i = 0;i < 1; i++) {
			assertEquals(Thread.State.TERMINATED, storageThreads[i].getState());
		}
		
	}
	/**
	 * 
	 * 
	 * 
	 * Write your other tests here
	 * 
	 * 
	 * 
	 */
	
	
	
}
