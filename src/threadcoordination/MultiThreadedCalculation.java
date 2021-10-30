package threadcoordination;

import java.math.BigInteger;

public class MultiThreadedCalculation {

    public static void main(String[] args) throws InterruptedException {
        BigInteger base1 = new BigInteger("2");
        BigInteger power1 = new BigInteger("9");
        BigInteger base2 = new BigInteger("2");
        BigInteger power2 = new BigInteger("9");
        BigInteger result = calculateResult(base1, power1, base2, power2);
        System.out.println("The result is: " + result);
    }

    public static BigInteger calculateResult(BigInteger base1, BigInteger power1, BigInteger base2, BigInteger power2) throws InterruptedException {
        PowerCalculatingThread thread1 = new PowerCalculatingThread(base1, power1);
        PowerCalculatingThread thread2 = new PowerCalculatingThread(base2, power2);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        return thread1.getResult().add(thread2.getResult());
    }

    private static class PowerCalculatingThread extends Thread {
        private BigInteger result = BigInteger.ONE;
        private final BigInteger base;
        private final BigInteger power;

        public PowerCalculatingThread(BigInteger base, BigInteger power) {
            this.setName("Worker Thread " + base + "^" + power);
            this.base = base;
            this.power = power;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.result = pow(base, power);
        }

        private BigInteger pow(BigInteger base, BigInteger power) {
            BigInteger powerResult = BigInteger.ONE;
            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
                powerResult = powerResult.multiply(base);
            }
            return powerResult;
        }

        public BigInteger getResult() {
            return result;
        }
    }
}