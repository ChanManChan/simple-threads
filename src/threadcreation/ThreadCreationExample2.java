package threadcreation;

public class ThreadCreationExample2 {
    public static void main(String[] args) {
        Thread thread = new CustomThread();
        thread.start();
    }

    private static class CustomThread extends Thread {
        @Override
        public void run() {
            this.setName("Custom Thread");
            System.out.println("Hello from: " + this.getName());
        }
    }
}