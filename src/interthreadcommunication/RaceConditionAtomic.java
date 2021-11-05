package interthreadcommunication;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Issues with locks:-
 * 1> Deadlocks are generally unrecoverable and the more locks in the application the higher the changes for a deadlock
 * 2> Slow critical section - Multiple threads using the same lock, one thread holds the lock for very long. All the threads become as slow as the slowest thread
 * 3> Priority Inversion - Two threads sharing a lock
 * Low priority thread (background document saver)
 * High priority thread (UI)
 * Low priority thread acquires the lock, and is preempted by the operating system (scheduled out)
 * High priority thread cannot progress because of the low priority thread is not scheduled to release the lock
 * 4> Thread dies, get interrupted or forgets to release a lock (Kill Tolerance), leaves all thread hanging forever. Unrecoverable just like deadlock
 * 5> Performance overhead in having contention over a lock
 * Thread A acquires a lock
 * Thread B tries to acquire a lock and gets blocked
 * Thread B is scheduled out (context switch)
 * Thread B is scheduled back (context switch)
 * <p>
 * Why locks?
 * Multiple threads accessing shared resources
 * At least one thread is modifying the shared resources
 * Non-atomic operations on one shared resource
 * A single Java operation turns into one or more hardware operations
 * eg:- counter++ turns into at least 3 hardware instructions
 * read counter
 * calculate the new value
 * store new value back to counter
 * Between the execution of these several hardware instructions another thread can modify the value of counter
 * <p>
 * Lock Free solution:-
 * Utilize operations which are guaranteed to be one hardware operation
 * A single hardware operation is atomic by definition and thread safe
 * <p>
 * Why locks?
 * Multiple threads accessing shared resources
 * At least one thread is modifying the shared resources
 * Non-atomic operations on one shared resource
 * A single Java operation turns into one or more hardware operations
 * eg:- counter++ turns into at least 3 hardware instructions
 * read counter
 * calculate the new value
 * store new value back to counter
 * Between the execution of these several hardware instructions another thread can modify the value of counter
 * <p>
 * Lock Free solution:-
 * Utilize operations which are guaranteed to be one hardware operation
 * A single hardware operation is atomic by definition and thread safe
 * <p>
 * Review of all Atomic operations:-
 * 1> Read/Assignment on all primitive types (except for long and double)
 * 2> Read/Assignment on all references
 * 3> Read/Assignment on volatile long and double
 * 4> Atomic classes located in the java.util.concurrent.atomic package
 * <p>
 * Avoiding Data Races:-
 * Read/Assignment on all volatile primitive types and references
 * <p>
 * Why locks?
 * Multiple threads accessing shared resources
 * At least one thread is modifying the shared resources
 * Non-atomic operations on one shared resource
 * A single Java operation turns into one or more hardware operations
 * eg:- counter++ turns into at least 3 hardware instructions
 * read counter
 * calculate the new value
 * store new value back to counter
 * Between the execution of these several hardware instructions another thread can modify the value of counter
 * <p>
 * Lock Free solution:-
 * Utilize operations which are guaranteed to be one hardware operation
 * A single hardware operation is atomic by definition and thread safe
 * <p>
 * Review of all Atomic operations:-
 * 1> Read/Assignment on all primitive types (except for long and double)
 * 2> Read/Assignment on all references
 * 3> Read/Assignment on volatile long and double
 * 4> Atomic classes located in the java.util.concurrent.atomic package
 * <p>
 * Avoiding Data Races:-
 * Read/Assignment on all volatile primitive types and references
 */

/**
 * Why locks?
 * Multiple threads accessing shared resources
 * At least one thread is modifying the shared resources
 * Non-atomic operations on one shared resource
 *      A single Java operation turns into one or more hardware operations
 *      eg:- counter++ turns into at least 3 hardware instructions
 *              read counter
 *              calculate the new value
 *              store new value back to counter
 *      Between the execution of these several hardware instructions another thread can modify the value of counter
 */

/**
 * Lock Free solution:-
 * Utilize operations which are guaranteed to be one hardware operation
 * A single hardware operation is atomic by definition and thread safe
 */

/**
 * Review of all Atomic operations:-
 * 1> Read/Assignment on all primitive types (except for long and double)
 * 2> Read/Assignment on all references
 * 3> Read/Assignment on volatile long and double
 * 4> Atomic classes located in the java.util.concurrent.atomic package
 */

/**
 * Avoiding Data Races:-
 * Read/Assignment on all volatile primitive types and references
 */

/**
 * AtomicInteger is a great tools for concurrent counting, without the complexity of using a lock
 * AtomicInteger should be used only when atomic operations are needed
 * It's on par and sometimes more performant than a regular integer with a lock as protection
 * If used only by a single thread, a regular integer is preferred
 */

public class RaceConditionAtomic {

    public static void main(String[] args) throws InterruptedException {
        InventoryCounter inventoryCounter = new InventoryCounter();
        IncrementingThread incrementingThread = new IncrementingThread(inventoryCounter);
        DecrementingThread decrementingThread = new DecrementingThread(inventoryCounter);

        incrementingThread.start();
        decrementingThread.start();

        incrementingThread.join();
        decrementingThread.join();

        System.out.println("We current have " + inventoryCounter.getItems() + " items");
    }

    public static class DecrementingThread extends Thread {
        private final InventoryCounter inventoryCounter;

        public DecrementingThread(InventoryCounter inventoryCounter) {
            this.inventoryCounter = inventoryCounter;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10_000; i++) {
                inventoryCounter.decrement();
            }
        }
    }

    public static class IncrementingThread extends Thread {
        private final InventoryCounter inventoryCounter;

        public IncrementingThread(InventoryCounter inventoryCounter) {
            this.inventoryCounter = inventoryCounter;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10_000; i++) {
                inventoryCounter.increment();
            }
        }
    }

    private static class InventoryCounter {
        private final AtomicInteger items = new AtomicInteger(0);

        public void increment() {
            items.incrementAndGet();
        }

        public void decrement() {
            items.decrementAndGet();
        }

        public int getItems() {
            return items.get();
        }
    }
}
