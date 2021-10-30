package threadcreation;

public class ThreadCreationExample1 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Code that will run in  a new thread
                System.out.println("We are now in thread: " + Thread.currentThread().getName());
                System.out.println("Current thread priority is: " + Thread.currentThread().getPriority());
            }
        });

        thread.setName("New Worker Thread");
        thread.setPriority(Thread.MAX_PRIORITY);

        System.out.println("We are in thread: " + Thread.currentThread().getName() + " before starting a new thread");
        thread.start(); // this will instruct the JVM to create a new thread and pass it to the operating system
        System.out.println("We are in thread: " + Thread.currentThread().getName() + " after starting a new thread");
        // Thread.sleep(1000); // instructs the OS to not schedule the current thread until that time passes, during that time this thread is not consuming any CPU
    }
}
