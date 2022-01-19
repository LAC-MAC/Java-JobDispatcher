# Java-JobDispatcher
Job threads need a certain amount of storage and compute threads to be completed. This program is thread safe dispatcher that allows worker (storage and compute) threads and job threads to queue. Jobs are triggered depenedent on worker resources available.

Thread data strucutures were banned that is why there is my own implementation of a stack as to act a queue for threads.


