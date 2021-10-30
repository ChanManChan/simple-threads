package threadcoordination;

public class ThreadCoordinationExample1 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new BlockingTask());
        thread.start();
        Thread.sleep(10000);
        thread.interrupt();
    }

    private static class BlockingTask implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(500000); // the entire app will wait on this blocking thread to finish despite the fact that the main thread is already long gone
            } catch (InterruptedException e) { // this exception is going to be thrown when the current thread is interrupted externally
                System.out.println("Exiting blocking thread");
            }
        }
    }
}
