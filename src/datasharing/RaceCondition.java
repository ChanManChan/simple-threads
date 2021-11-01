package datasharing;

// Race Condition:-
// 1> Condition when multiple threads are accessing a shared resource
// 2> At least one thread is modifying the resource
// 3> The timing of threads scheduling may cause incorrect results
// 4> The core problem is non-atomic operations performed on the shared resource
// Solution:-
// 1> Identification of the critical section where the race condition is happening
// 2> Protection of the critical section by a synchronized block
public class RaceCondition {
    public static void main(String[] args) throws InterruptedException {
        InventoryCounter inventoryCounter = new InventoryCounter();
        IncrementingThread incrementingThread = new IncrementingThread(inventoryCounter);
        DecrementingThread decrementingThread = new DecrementingThread(inventoryCounter);

        incrementingThread.start();
        decrementingThread.start();

        incrementingThread.join();
        decrementingThread.join();

        System.out.println("We currently have " + inventoryCounter.getItems() + " items"); // if everything goes well then we expect to have zero items in the end
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

    // Despite the fact that our threads execute methods on a shared object "InventoryCounter" we are able to restrict
    // them to execute in the critical section one thread at a time (because we used the synchronized keyword).
    private static class InventoryCounter {
        private int items = 0;
        private final Object lock = new Object(); // instead of using synchronized keyword in the method declaration
        // CRITICAL SECTION ----------------
        // locking mechanism to solve the concurrency issue (race condition)
        // synchronized is applied per object. Every synchronized method is a different door to a room where if you lock
        // one door all the other doors gets locked immediately and automatically
        //        public synchronized void increment() { // synchronization happens on the object level
        //            items++;
        //        }

        public void increment() {
            // concurrent execution
            synchronized (this.lock) {
                // non-concurrent execution
                items++;
            }
            // concurrent execution
        }

        // When thread1 is executing increment(), thread2 cannot execute decrement()
        // And when thread2 is executing decrement(), thread1 cannot execute increment()
        // That is because both methods are synchronized and belong to the same object.

        public void decrement() {
            synchronized (this.lock) {
                // Only one thread at a time can execute the code inside here.
                // eg:- when thread1 is executing code inside this block, thread2 is blocked and vice versa.
                items--;
            }
        }

        public int getItems() {
            synchronized (this.lock) {
                return items;
            }
        }
    }
}
