package ThreadPools_Executors;

import java.util.concurrent.*;

// --- CLASS 1: THE WORKER TASK ---
// We implement Runnable because this is a fire-and-forget job.
// We don't need any data returned to the main thread.
class DownloadTask implements Runnable {

    private final int ImageId;

    // We pass the ID through the constructor so the worker knows which image to grab
    DownloadTask(int ImageId) {
        this.ImageId = ImageId;
    }

    @Override
    public void run() {
        // Thread.currentThread().getName() lets us see exactly which worker grabbed this task
        System.out.println(Thread.currentThread().getName() + " is downloading image #" + ImageId);

        try {
            // Simulate a heavy 3-second file download
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + " finished downloading image #" + ImageId);
    }
}

// --- CLASS 2: THE MANAGER (Main) ---
class Main {
    public static void main(String[] args){

        // Hire exactly 3 workers. Even if we have 5 tasks, only 3 will download concurrently.
        // The other 2 will wait in the queue until a worker finishes.
        ExecutorService imgExecutor = Executors.newFixedThreadPool(3);

        System.out.println("--- DOWNLOADING IMAGES ---");

        // The Main Thread rapid-fires 5 tasks into the Executor's inbox instantly
        for (int i = 1; i <= 5; i++) {
            DownloadTask myTask = new DownloadTask(i);

            // .execute() is specifically for Runnables.
            // It triggers the signal for the workers to start, and then the Main Thread walks away.
            imgExecutor.execute(myTask);
        }

        // Tell the Executor to close up shop once the current queue is empty.
        // If you forget this, your Java program will stay running forever waiting for new tasks!
        imgExecutor.shutdown();

        // This will print almost immediately, proving the Main Thread didn't wait for the downloads!
        System.out.println("Main thread is done assigning work. The background workers are handling the rest!");
    }
}