package interthreadcommunication;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

// Compare LockFree performance with Blocking implementation
public class LockFreeDataStructure {

    public static void main(String[] args) throws InterruptedException {
//         StandardStack<Integer> stack = new StandardStack<>(); // 224,505,251 operations were performed in 10 seconds
        LockFreeStack<Integer> stack = new LockFreeStack<>(); // 493,227,940 operations were performed in 10 seconds
        Random random = new Random();

        for (int i = 0; i < 100_000; i++) {
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();
        int pushingThreads = 2;
        int poppingThreads = 2;

        for (int i = 0; i < pushingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.push(random.nextInt());
                }
            });

            thread.setDaemon(true);
            threads.add(thread);
        }

        for (int i = 0; i < poppingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.pop();
                }
            });

            thread.setDaemon(true);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        Thread.sleep(10_000);
        System.out.println(String.format("%,d operations were performed in 10 seconds", stack.getCounter()));
    }

    // Lock free data structure using AtomicReference and compareAndSet
    public static class LockFreeStack<T> {
        private final AtomicReference<StackNode<T>> head = new AtomicReference<>();
        private final AtomicInteger counter = new AtomicInteger(0); // to keep track of number of operations we perform so that we can compare the performance later

        public void push(T value) {
            StackNode<T> newHeadNode = new StackNode<>(value, null);
            // we need a loop because many thread either push or pop items onto or from the LockFreeStack at the same time, and we may need a few attempts to succeed
            while (true) {
                StackNode<T> currentHeadNode = head.get();
                newHeadNode.next = currentHeadNode;
                if (head.compareAndSet(currentHeadNode, newHeadNode)) { // atomically point the newHeadNode to point to the currentHeadNode
                    // if it reaches here, it means the head was not modified by any other thread between reading the head and writing to the head
                    break;
                } else {
                    // between reading from the head and trying to update the head with the newHeadNode, the currentHeadNode has changed
                    // do all the operations again after waiting for 1 nanosecond
                    LockSupport.parkNanos(1);
                }
            }
            counter.incrementAndGet();
        }

        public T pop() {
            StackNode<T> currentHeadNode = head.get();
            StackNode<T> newHeadNode;

            while (currentHeadNode != null) {
                newHeadNode = currentHeadNode.next;
                if (head.compareAndSet(currentHeadNode, newHeadNode)) { // checks if the expected value still holds (currentHeadNode) and then it sets the head to the new candidate (newHeadNode)
                    break;
                } else {
                    // repeat the process until we succeed
                    LockSupport.parkNanos(1);
                    currentHeadNode = head.get(); // re-read from the head and try again, since the currentHeadNode changed since we last read it
                }
            }
            counter.incrementAndGet();
            return currentHeadNode != null ? currentHeadNode.value : null;
        }

        public int getCounter() {
            return counter.get();
        }
    }

    // This StandardStack is going to be shared by multiple Threads
    // Blocked implementation of the stack using a linked list of StackNodes
    public static class StandardStack<T> {
        private StackNode<T> head;
        private int counter = 0; // to keep track of number of operations we perform so that we can compare the performance later

        public synchronized void push(T value) {
            StackNode<T> newHead = new StackNode<>(value, null);
            newHead.next = head; // ** race condition is possible here
            head = newHead;      // head reference can change its value between the read and the write
            counter++;
        }

        public synchronized T pop() {
            if (head == null) {
                counter++;
                return null;
            }

            T value = head.value; // ** race condition is possible here, head reference can change its value between the read and the write
            head = head.next; // old head is garbage collected since there is no more references to that StackNode
            counter++;
            return value;
        }

        public int getCounter() {
            return counter;
        }
    }

    private static class StackNode<T> {
        public T value;
        public StackNode<T> next;

        public StackNode(T value, StackNode<T> next) {
            this.value = value;
            this.next = next;
        }
    }
}
