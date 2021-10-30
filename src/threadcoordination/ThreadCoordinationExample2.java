package threadcoordination;

import java.math.BigInteger;

public class ThreadCoordinationExample2 {
    public static void main(String[] args) throws InterruptedException {
        BigInteger base = new BigInteger("20000");
        BigInteger power = new BigInteger("1000000");
        Thread thread = new Thread(new LongComputationTask(base, power));

//        thread.setDaemon(true); // background thread that do not prevent the application from exiting if the main thread terminates
        thread.start();
        Thread.sleep(100);
        thread.interrupt(); // just calling this method is not enough. The interrupt is sent, but we do need a method or logic to handle it.
    }

    private static class LongComputationTask implements Runnable {
        private BigInteger base;
        private BigInteger power;

        public LongComputationTask(BigInteger base, BigInteger power) {
            this.base = base;
            this.power = power;
        }

        @Override
        public void run() {
            System.out.println(base + "^" + power + " = " + pow(base, power));
        }

        private BigInteger pow(BigInteger base, BigInteger power) {
            BigInteger result = BigInteger.ONE;

            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
                if (Thread.currentThread().isInterrupted()) { // if we don't want to handle the thread interruption gracefully then we can simply set the daemon property of the thread to true
                    System.out.println("Prematurely interrupted computation");
                    return BigInteger.ZERO;
                }
                result = result.multiply(base);
            }
            return result;
        }
    }
}
