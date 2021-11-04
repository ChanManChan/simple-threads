package interthreadcommunication;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringJoiner;

// Whenever using a queue to decouple multithreaded components, apply back-pressure and limit the size of the queue to prevent it from crashing our app
public class MatrixMultiplicationExample {
    private static final String INPUT_FILE = "./out/matrices";
    private static final String OUTPUT_FILE = "./out/matrices_results.txt";
    private static final int N = 10;

    public static void main(String[] args) throws IOException {
        ThreadSafeQueue threadSafeQueue = new ThreadSafeQueue();
        File inputFile = new File(INPUT_FILE);
        File outputFile = new File(OUTPUT_FILE);

        MatricesReaderProducer matricesReaderProducer = new MatricesReaderProducer(new FileReader(inputFile), threadSafeQueue);
        MatricesMultiplierConsumer matricesMultiplierConsumer = new MatricesMultiplierConsumer(new FileWriter(outputFile), threadSafeQueue);

        matricesReaderProducer.start();
        matricesMultiplierConsumer.start();
    }

    private static class MatricesMultiplierConsumer extends Thread {
        private final ThreadSafeQueue queue;
        private final FileWriter fileWriter;

        public MatricesMultiplierConsumer(FileWriter fileWriter, ThreadSafeQueue queue) {
            this.fileWriter = fileWriter;
            this.queue = queue;
        }

        private static void saveMatrixToFile(FileWriter fileWriter, float[][] matrix) throws IOException {
            for (int r = 0; r < N; r++) {
                StringJoiner stringJoiner = new StringJoiner(", ");
                for (int c = 0; c < N; c++) {
                    stringJoiner.add(String.format("%.2f", matrix[r][c]));
                }
                fileWriter.write(stringJoiner.toString());
                fileWriter.write('\n');
            }
            fileWriter.write('\n');
        }

        @Override
        public void run() {
            while (true) {
                MatricesPair matricesPair = queue.remove();
                if (matricesPair == null) {
                    System.out.println("No more matrices to read from the queue, consumer is terminating");
                    break;
                }
                float[][] result = multiplyMatrices(matricesPair.matrix1, matricesPair.matrix2);
                try {
                    saveMatrixToFile(fileWriter, result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private float[][] multiplyMatrices(float[][] m1, float[][] m2) {
            float[][] result = new float[N][N];
            for (int r = 0; r < N; r++) {
                for (int c = 0; c < N; c++) {
                    for (int k = 0; k < N; k++) {
                        result[r][c] += m1[r][k] * m2[k][c];
                    }
                }
            }
            return result;
        }
    }

    private static class MatricesReaderProducer extends Thread {
        private final Scanner scanner;
        private final ThreadSafeQueue queue;

        public MatricesReaderProducer(FileReader reader, ThreadSafeQueue queue) {
            this.scanner = new Scanner(reader);
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                float[][] matrix1 = readMatrix();
                float[][] matrix2 = readMatrix();
                if (matrix1 == null || matrix2 == null) {
                    queue.terminate();
                    System.out.println("No more matrices to read. Producer thread is terminating.");
                    return;
                }

                MatricesPair matricesPair = new MatricesPair();
                matricesPair.matrix1 = matrix1;
                matricesPair.matrix2 = matrix2;

                queue.add(matricesPair);
            }
        }

        private float[][] readMatrix() {
            float[][] matrix = new float[N][N];
            for (int r = 0; r < N; r++) {
                if (!scanner.hasNext()) {
                    return null;
                }
                String[] line = scanner.nextLine().split(","); // matrix elements in that row
                for (int c = 0; c < N; c++) {
                    matrix[r][c] = Float.parseFloat(line[c]);
                }
            }
            scanner.nextLine();
            return matrix;
        }

    }

    private static class ThreadSafeQueue {
        private final Queue<MatricesPair> queue = new LinkedList<>(); // not thread safe linked list
        private boolean isEmpty = true; // indicates whether our queue contains any matrices or not.
        private boolean isTerminate = false; // is used to signal the consumer that the producer has nothing more to offer and the consumer needs to terminate its thread
        private static final int CAPACITY = 5; // to safeguard against out of memory exception implement a BACKPRESSURE on our producer

        // all three methods are synchronized which keeps our operation on the queue atomic and also allows us to use the wait() and notify() methods inside them
        public synchronized void add(MatricesPair matricesPair) { // called by the producer to add a pair of matrices into the queue
            while (queue.size() == CAPACITY) {
                // before adding an item to the queue, we are going to check if we already reached the maximum capacity of the queue
                try {
                    wait(); // if we are at maximum capacity, producer wait to be woken up by the consumer
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            queue.add(matricesPair);
            isEmpty = false;
            notifyAll(); // if a consumer is waiting for work, we will notify it so that the consumer would wake up
        }

        public synchronized MatricesPair remove() { // called by the consumer to consume and remove a pair of matrices from the queue
            MatricesPair matricesPair = null;
            while (isEmpty && !isTerminate) {
                // the consumer has nothing to consume from the queue at the moment, so it would call the wait method and go to sleep releasing the lock on the queue
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (queue.size() == 1) {
                isEmpty = true;
            }

            if (queue.isEmpty() && isTerminate) {
                return null;
            }

            System.out.println("queue size: " + queue.size());
            matricesPair = queue.remove(); // return the next element in the queue

            if (queue.size() == CAPACITY - 1) {
                notifyAll(); // wake up the producer to start filling up the capacity again.
            }

            return matricesPair;
        }

        public synchronized void terminate() { // called by the producer to let the consumer know that once the queue becomes empty the consumer should terminate its thread
            isTerminate = true;
            notifyAll(); // to wake up all the potentially waiting consumer threads
        }
    }

    private static class MatricesPair {
        public float[][] matrix1;
        public float[][] matrix2;
    }
}
