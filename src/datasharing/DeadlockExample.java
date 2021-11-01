package datasharing;

import java.util.Random;

// Conditions for Deadlock
// 1> Mutual Exclusion - Only one thread can have exclusive access to a resource
// 2> Hold and Wait - At least one thread is holding a resource and is waiting for another resource
// 3> Non-preemptive allocation - A resource is released only after the thread is done using it
// 4> Circular wait - A chain of at least two threads each one is holding one resource and waiting for another resource
// Solution:-
// 1> Avoid circular wait - Enforce a strict order in lock acquisition everywhere
public class DeadlockExample {

    public static void main(String[] args) {
        Intersection intersection = new Intersection();
        Thread trainAThread = new Thread(new TrainA(intersection));
        Thread trainBThread = new Thread(new TrainB(intersection));
        trainAThread.setName("Thread A");
        trainBThread.setName("Thread B");

        trainAThread.start();
        trainBThread.start();
    }

    public static class TrainB implements Runnable {
        private final Intersection intersection;
        private final Random random = new Random();

        public TrainB(Intersection intersection) {
            this.intersection = intersection;
        }

        @Override
        public void run() {
            while (true) {
                long sleepingTime = random.nextInt(100);
                try {
                    Thread.sleep(sleepingTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                intersection.takeRoadB();
            }
        }
    }

    public static class TrainA implements Runnable {
        private final Intersection intersection;
        private final Random random = new Random();

        public TrainA(Intersection intersection) {
            this.intersection = intersection;
        }

        @Override
        public void run() {
            while (true) {
                long sleepingTime = random.nextInt(100);
                try {
                    Thread.sleep(sleepingTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                intersection.takeRoadA();
            }
        }
    }

    public static class Intersection {
        // Intersection class is ready and safe from collisions
        private final Object roadA = new Object();
        private final Object roadB = new Object();

        public void takeRoadA() {
            synchronized (roadA) {
                System.out.println("Road A is locked by thread " + Thread.currentThread().getName());

                synchronized (roadB) {
                    System.out.println("Train is passing through road A");
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void takeRoadB() {
            synchronized (roadA) { // roadB lock first will cause a deadlock, enforcing a strict order on lock acquisition prevents deadlocks
                System.out.println("Road A is locked by thread " + Thread.currentThread().getName());

                synchronized (roadB) {
                    System.out.println("Train is passing through road B");
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
