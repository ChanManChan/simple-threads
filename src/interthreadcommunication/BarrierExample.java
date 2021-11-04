package interthreadcommunication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BarrierExample {

    public static void main(String[] args) {
        int numberOfThreads = 8;
        List<Thread> threads = new ArrayList<>();
        Barrier barrier = new Barrier(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            threads.add(new Thread(new CoordinatedWorkRunner(barrier)));
        }

        for (Thread thread : threads) {
            thread.start();
        }
    }

    public static class Barrier {
        private final int numberOfWorkers;
        // Initialize the semaphore to 0, to make sure every thread that tries to acquire the semaphore gets blocked.
        // And the last thread to get to the barrier, releases the semaphore numberOfWorkers - 1 since "numberOfWorkers - 1" threads are blocked on the semaphore.
        private final Semaphore semaphore = new Semaphore(0);
        private int counter = 0;
        private final Lock lock = new ReentrantLock(); // The lock ensures atomic check and modification of the shared variables, involved in the condition

        public Barrier(int numberOfWorkers) {
            this.numberOfWorkers = numberOfWorkers;
        }

        public void barrier() throws InterruptedException {
            lock.lock();
            boolean isLastWorker = false; // Condition variable is always associated with a lock
            try {
                counter++;
                if (counter == numberOfWorkers) {
                    isLastWorker = true;
                }
            } finally {
                lock.unlock();
            }

            if (isLastWorker) {
                System.out.println("----------part 1 finished by all threads----------");
                Thread.sleep(2000);
                semaphore.release(numberOfWorkers - 1);
            } else {
                semaphore.acquire();
            }
        }
    }

    public static class CoordinatedWorkRunner implements Runnable {
        private final Barrier barrier;

        public CoordinatedWorkRunner(Barrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                task();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void task() throws InterruptedException {
            System.out.println(Thread.currentThread().getName() + ": part 1 of the work is finished");

            barrier.barrier();

            System.out.println(Thread.currentThread().getName() + ": part 2 of the work is finished");
        }
    }
}
