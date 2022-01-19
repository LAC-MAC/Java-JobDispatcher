

/**
 * @author mikec
 *
 * This interface MUST not be changed
 * 
 * If you change this interface it will invalidate your project and it will not be marked
 * 
 */
public interface Dispatcher {
	
	public void specifyJob(int nComputeThreads, int nStorageThreads);
	// This method allows the caller to specify a single 'job' to be performed a set of 
	// Compute and Storage threads.
	// 	
	// 	nComputeThreads: specifies the number of Compute threads required for the job
	// 	nStorageThreads: specifies the number of Storage threads required for the job
	
	public void queueComputeThread();
	// called by a Compute thread when it is available to do a job

	public void queueStorageThread();
	// called by a Storage thread when it is available to do a job
}
