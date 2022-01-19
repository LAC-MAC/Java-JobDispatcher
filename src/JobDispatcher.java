import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.omg.PortableInterceptor.ACTIVE;
// Note that you MUST not use any other `thread safe` classes than the two imported above

public class JobDispatcher implements Dispatcher {
	
	//the main lock
	final ReentrantLock lock = new ReentrantLock();
	//condition for the compute workers
	final Condition computeCon = lock.newCondition();
	//condition for the storage workers
	final Condition storageCon = lock.newCondition();
	//condition for the jobs that are specified
	final Condition jobCon = lock.newCondition();
	//condition for current job to check if there is enough workers
	final Condition checkjob = lock.newCondition();
	//condition for workers to signal current job when they have been dequeued
	final Condition beenUsed = lock.newCondition();
	//condition for when workers cannot queue as current job is dequeuing
	final Condition cannotQCon = lock.newCondition();
	//boolean for when job is done
	boolean jobDone = false;
	//boolean for when workers cannot queue
	boolean cannotQueue = false;

	
	//counter for number of compute workers avaiable 
	int computeNum = 0;
	//counter for number of storage workers avaiable 
	int storageNum = 0;
	//current job getting completed
	Job currentJob = null;
	//stacks that both mimic the condition that workers of each type queue on
	final MyStack computes = new MyStack();
	final MyStack storages = new MyStack();

	@Override
	public void specifyJob(int nComputeThreads, int nStorageThreads) {
		//deals with a zero worker need job
		if(nComputeThreads == 0 && nStorageThreads == 0) {
			//do nothing as this job does not need workers
		}
		else if(nComputeThreads < 0 || nStorageThreads < 0 ) {
			//do nothing as this jobs is not suitable
		}
		else { 
			//create a new thread for the job
		Thread JobThread = new Thread() {			
			public void run () {
				//lock the lock 
				lock.lock();
				try {
					//System.out.println("specifyJob thread  " + Thread.currentThread());
					//create job object
					Job job = new Job(nComputeThreads, nStorageThreads);
					//if there is no current job that is going on then
					if(currentJob == null) {
						//become current job
						currentJob = job;
						//System.out.println("current job thread is  " + Thread.currentThread());
						//call method to signal workers waiting on the outside conditions to join job
					}else {
						//else wait on the job condition, will be signalled when current job is over
						jobCon.await();
						//been awoken become the new job
						currentJob = job;
						
					}
					//check if the current job can be done with the current amount of workers
					currentJob.checkJob();
					
					//been released from checkJob means current job is over
					
					//DEBUG:System.out.println(jobDone);
					//workers can queue again 
					cannotQueue = false;
					//singal all workers that tried to queue while current job was dequeuing
					cannotQCon.signalAll();
					//set currenJob back to null
					currentJob = null;
					//signal next job 
					jobCon.signal();
					//DEBUG:System.out.println("current job thread is done  " + Thread.currentThread());
					
					
					
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
				//finally unlock the lock
				lock.unlock();
					
				}
				
			}
		};
		//start the job thread
		JobThread.start();
		}
	}

		   
	@Override
	public void queueComputeThread() {
		//lock the lock
		lock.lock();
		
		try {
			//if cannot queue is true then
				if(cannotQueue) {
					//await until the current job is done dequeuing 
					cannotQCon.await();
					//DEBUG:System.out.println("awake from compute cannotQ thread "+ Thread.currentThread());
				}
				//DEBUG:System.out.println("queue compute thread "+ Thread.currentThread());
				//add to outside queue that mimics condition
				computes.push(Thread.currentThread());
				//increment number of threads on compute queue
				computeNum++;
				//signal the current job to check if it can be done
				checkjob.signal();
				lock.lock();
				try {
					//await on compute condition until needed by current job 
					computeCon.await();
				}finally {
					lock.unlock();
				}
				
				//if awoken then check if it is the current head of the outside queue
				while(!Thread.currentThread().equals(computes.peek())) {
					//if it isnt the head await again
					//DEBUG:System.out.println("in compute loop thread  " + Thread.currentThread());
					//DEBUG:System.out.println("in compute loop head  " + Thread.currentThread().equals(computes.peek()));
					computeCon.await();
					
				}
				
				//worker has been dequeued by job as job can be completed
				//decrement total number compute workers
				computeNum--;
				//remove it from the compute stack
				computes.pop();
				//signal the curent job that a worker has been released
				beenUsed.signal();
			
			
		}catch (InterruptedException e) {
				//do nothing
			} 
		finally {
			//fianlly unlock
			lock.unlock();
			
				
			
		}
		
		
	}

	@Override
	public void queueStorageThread() {
		//lock the lock
		lock.lock();
		try {	
			//if cannot queue as current job is dequeue then 
				if(cannotQueue) {
					//await until current job has finished dequeing
					cannotQCon.await();
					//DEBUG:System.out.println("awake from compute cannotQ thread "+ Thread.currentThread());
				}
				
				//DEBUG:System.out.println("queue storage thread  " + Thread.currentThread());
				//push this worker onto the stack that mimics the conditon
				storages.push(Thread.currentThread());
				//increment the storage queue by 1
				storageNum++;
				//signal the job to check if it can be done
				checkjob.signal();
				lock.lock();
				try {
					//await on compute condition until needed by current job 
					storageCon.await();
				}finally {
					lock.unlock();
				}
				//if not the current head of the outside storage stack
				while(!Thread.currentThread().equals(storages.peek())) {
					//DEBUG:System.out.println("in storage loop thread  " + Thread.currentThread());
					//DEBUG:System.out.println("in storage loop head  " + Thread.currentThread().equals(computes.peek()));
					storageCon.await();
				}
				//storage thread has been dequeued by current job 
				//decrement the total number of storage workers
				storageNum--;
				//pop from the storage stack 
				storages.pop();
				//signal the current job that this thread has dequeued
				beenUsed.signal();
				
		}catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			 
			
		}finally {
			//finally unlock
			lock.unlock();
		}
	}
	
	

	

	

	

	//this is the class for job it keeps track of the number of workers need of each type for the job to be completed
	//it has a mehtod that checks if a job can be completed 
	//as well as methods for signallAll workers of each type 
	
	private class Job {
		//int that keep track of number need of each type of worker
		final private int capacityCompute, capacityStorage;
	
		


		//constructor
	    Job(int capacityCompute, int capacityStorage){ 
			this.capacityCompute = capacityCompute;
			this.capacityStorage = capacityStorage;
			
			
		}
		
		
			
	
		//this methods gets the job thread to chech if it can be completed
		protected boolean checkJob() {
			
			//lock the lock 
				lock.lock();
				try {
					//DEBUG:System.out.println("In checkjob thread " + Thread.currentThread());
					//check if the number of compute and storage works queued is enough to complete the job 
					if(computeNum >= this.capacityCompute && storageNum >= this.capacityStorage) {
						//set cannot queue to true to stop workers queueing while this job is dequeuing 
						cannotQueue = true;
						//loop for amount of storage threads need
						for(int j = 0; j < this.capacityStorage; j++) {
							//signal all threads
							releaseStorage();
							//await until thread that is awoken signals that it has been used
							beenUsed.await();
							
						}
						//loop for amount of compute threads needed 
						for(int i = 0; i < this.capacityCompute; i++) {
							//signal all compute threads
							releaseCompute();
							//await until thread that is awoken signals that it has been used
							beenUsed.await();
							
						}
						jobDone = true;
						
						
						//return true to break out of check jobs
						return true;
						
						
					}
					//if not enough workers to complete job then await until signalled 
					//DEBUG:System.out.println("job awaited thread " + Thread.currentThread());
					checkjob.await();
					
					checkJob();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally {
					lock.unlock();
				}
			return false;
		}
	
		
		
	
		protected void releaseCompute() {
			lock.lock();
			try {
				computeCon.signalAll();
			} finally {
				lock.unlock();
			}
		}
		
		protected void releaseStorage() {
			lock.lock();
			try {
				storageCon.signalAll();
			} finally {
				lock.unlock();
			}
		}
}
	
	private class MyStack {
		//unlimited size, hopefully wont need bigger than this for the marking tests
	    static final int capacity = 10000000;
	    //head of the stack pointer 
	    int head;
	    //the list that mimics the stack
	    Thread stack[] = new Thread[capacity]; 
	    //if head pointer is negative then stack is empty
	    boolean isEmpty(){
	        return (head < 0);
	    }
	    //constructor
	    MyStack(){
	    	//set it to negative as stack is empty
	        head = -1;
	    }
	    
	    //push ontop of the stack
	    protected boolean push(Thread x){
	    	//if the head pointer is on the last index the stack is full
	        if (head >= (capacity - 1)) {
	            System.out.println("full");
	            return false;
	        }
	        else {
	        	//if still room increment head and place thread there
	            stack[++head] = x;
	            return true;
	        }
	    }
	    
	    protected Thread peek(){
	    	//if stack empty return nothing
	        if (head < 0) {
	            return null;
	        }
	        //return the element in the stack at head pointer
	        else {
	            Thread x = stack[head];
	            return x;
	        }
	    }
	 
	    //pop off of the stack
	    protected Thread pop(){	
	    	//if stack is empty cant pop anything
	        if (head < 0) {
	            System.out.println("empty");
	            return null;
	        }
	        //else decrement head pointer and return thread at old pointer
	        else {
	            Thread x = stack[head--];
	            return x;
	        }
	    }
	 
	   
	}
	
	
	
}
