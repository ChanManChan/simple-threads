package datasharing;

import java.util.Random;

public class AtomicOperations {
    // Atomic operations:-
    // 1> Assignments to primitive types(excluding double and long)
    // 2> Assignments to references
    // 3> Assignments to double and long using volatile keyword
    public static void main(String[] args) {
        Metrics metrics = new Metrics();
        BusinessLogic businessLogicThread1 = new BusinessLogic(metrics);
        BusinessLogic businessLogicThread2 = new BusinessLogic(metrics);
        MetricsPrinter metricsPrinter = new MetricsPrinter(metrics);

        businessLogicThread1.start();
        businessLogicThread2.start();
        metricsPrinter.start();
    }

    public static class MetricsPrinter extends Thread {
        private final Metrics metrics;

        public MetricsPrinter(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                double currentAverage = metrics.getAverage(); // because the getAverage() method is not synchronized
                // we can guarantee the MetricsPrinter will not slow down the BusinessLogics threads as it can be
                // performed 100% in parallel to the other threads
                System.out.println("Current average is: " + currentAverage);
            }
        }
    }

    public static class BusinessLogic extends Thread {
        private final Metrics metrics;
        private final Random random = new Random();

        public BusinessLogic(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void run() {
            while (true) {
                long startTime = System.currentTimeMillis();

                try {
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long endTime = System.currentTimeMillis();
                metrics.addSample(endTime - startTime);
            }
        }
    }

    public static class Metrics {
        private long count = 0;
        private volatile double average = 0.0; // Volatile makes assignments to long or double atomic, however incrementing a volatile variable still involves multiple operations

        public synchronized void addSample(long sample) {
            double currentSum = average * count;
            count++;
            average = (currentSum + sample) / count; // the volatile keyword will guarantee write and read from the average variable to be atomic
        }

        public double getAverage() {
            // completely safe to call without synchronization
            // assignments and reads from primitive types and references are atomic
            // however average is a double which is not thread safe without adding the volatile keyword
            return average;
        }
    }
}
