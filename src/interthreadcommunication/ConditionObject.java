package interthreadcommunication;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Inter-thread - Semaphore as Condition variable
 * Calling the acquire() on a Semaphore is equivalent to checking the condition "Is Number of Permits > 0?"
 * If the condition is not met - thread A goes to sleep until another thread changes the semaphore's state
 * When thread B calls the release() method, thread A wakes up
 * Thread A checks the condition "Is Number of Permits > 0?"
 * If it is, thread A continues to the next instruction
 */

/**
 * Condition variable is a generic way of inter-thread communication
 * It allows us to use any condition we want to stipulate the continuation of the execution of a thread
 * Condition variable is always associated with a lock
 * The lock ensures atomic check and modification of the shared variables, involved in the condition
 */

public class ConditionObject {

    /**
     * These are two different ways to use inter-thread communication to achieve the same goal
     * SomeClass1 is using the instance of the class as the lock as well as the condition variable.
     * SomeClass2 is using an explicit ReentrantLock as the lock and the Condition object as the
     * condition variable.
     * <p>
     * Using a ReentrantLock and the Condition allows more flexibility as the Condition class has
     * methods like awaitUninterruptibly() and awaitUntil(Date deadline)
     * which the class Object does not have.
     * However, it is more verbose.
     */

    class SomeClass1 {
        boolean isCompleted = false;

        public synchronized void declareSuccess() throws InterruptedException {
            while (!isCompleted) {
                wait();
            }

            System.out.println("Success!!");
        }

        public synchronized void finishWork() {
            isCompleted = true;
            notify();
        }
    }

    class SomeClass2 {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        boolean isCompleted = false; // Shared variable

        public void declareSuccess() throws InterruptedException {
            lock.lock();
            try {
                while (!isCompleted) {
                    // before the thread goes to sleep though, the await() method also unlocks the lock for us atomically
                    // it is very important for the await() method to unlock the lock so that another thread can acquire that lock
                    condition.await(); // puts the thread to sleep until another thread signals for it to wake up
                }
            } finally {
                lock.unlock();
            }

            System.out.println("Success!!");
        }

        public void finishWork() {
            lock.lock();
            try {
                isCompleted = true;
                condition.signal(); // wakes up the declareSuccess() method calling thread that is blocked on the await() method
            } finally {
                lock.unlock(); // other thread can only continue after this lock is released by the finishWork() method calling thread
            }
        }
    }
}
