package datasharing;

// Data Race problem:-
// 1> Compiler and CPU may execute the instructions out of order to optimize performance and hardware utilization
// 2> They will do so while maintaining the logical correctness of the code.
// 3> Out of order execution by the compiler and CPU are important features to speed up the code.
// eg 1:- function like below will never be executed out of order (we will never experience a data race here)
// public void someFunction() {
//        // this will not be executed out of order because every line of code depends on the result from the previous line of code
//        x = 1;
//        y = x + 2;
//        z = y + 10;
// }
// eg 2:- function like blow has no dependencies between the two lines of code. So from the CPU and compilers point of view these two lines can be rearranged
// public void increment() {
//        x++;
//        y++;
// }

// Synchronized - Solves both Race Condition and Data Race. But has a performance penalty.
// Volatile -
// 1> Solves Race Condition for read/write from/to long and double
// 2> Solves all Data Races by guaranteeing order

// Rule of Thumb:-
// Every shared variable (modified by at least one thread) should be either
// 1> Guarded by a synchronized block (or any type of lock)
// 2> or Declared volatile
public class DataRaceExample {

    public static void main(String[] args) throws InterruptedException {
        SharedClass sharedClass = new SharedClass();

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                sharedClass.increment();
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                sharedClass.checkForDataRace();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        System.out.println("process finished");
    }

    public static class SharedClass {
        // declared a shared variable as volatile guarantees the code that comes before the access to a volatile variable will be executed before that access instruction
        // and code that comes after the access to a volatile variable will be executed after that access instruction
        // this is equivalent to a memory fence or a memory barrier
        private volatile int x = 0;
        private volatile int y = 0;

        public void increment() {
            // adding the volatile keyword made these two lines locked in place and the CPU or compiler cannot rearrange them and therefore will always be executed in order.
            x++;
            y++;
        }

        public void checkForDataRace() {
            if (y > x) {
                // because x++ and y++ are always executed in that order, this condition is never satisfied because the
                // invariant that logically should hold despite the variance in the scheduling order is "x >= y"
                System.out.println("y > x - Data Race is detected");
            }
        }
    }
}
