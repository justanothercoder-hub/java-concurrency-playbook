package ThreadPools_Executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// --- CLASS 1: THE WORKER TASK ---
// We implement Callable<String> instead of Runnable because we actually
// want the worker to hand us back a piece of text (the String) when they finish.
class ReportTask implements Callable<String>{
    private final int sectionId;

    // Pass the section ID so the worker knows which part of the report to write
    ReportTask(int s_id){
        this.sectionId=s_id;
    }

    @Override
    public String call(){
        // The background worker starts doing this the second they are assigned the task
        System.out.println("Drafting the document for section #" + sectionId + "...");

        try {
            // Simulate the heavy work of generating a PDF section (takes 2 seconds)
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // This return statement is what makes Callable different from Runnable!
        return "The task of section #" + sectionId + " is completed";
    }
}

// --- CLASS 2: THE MANAGER (Main) ---
class Workexecutor{
    public static void main(String[] args){

        // Hire a "Cached" thread pool.
        // Since we are about to submit 4 tasks instantly, this manager will
        // instantly hire exactly 4 concurrent workers to handle them all at once.
        ExecutorService dynamiceEX= Executors.newCachedThreadPool();

        // We need a list to hold onto our "claim tickets" (Futures) so we don't lose them.
        List<Future<String>> futureResults = new ArrayList<>();

        System.out.println("--- ASSIGNING TASKS ---");

        // The Main Thread drops off all 4 tasks in less than a millisecond
        for (int i = 1; i <= 4; i++) {
            ReportTask myDataTask = new ReportTask(i);

            // .submit() drops the task in the queue, signals a worker to start,
            // and instantly hands us a receipt. It DOES NOT wait for the worker to finish!
            Future<String> future = dynamiceEX.submit(myDataTask);

            // Put the receipt in our pocket (the list) so we can cash it in later
            futureResults.add(future);
        }

        System.out.println("--- GATHERING RESULTS ---");

        // Retrieve the completed text using .get()
        try {
            for (Future<String> f : futureResults) {
                // The Main Thread walks up to the counter and hands over the receipt.
                // If the worker is still doing their 2-second sleep, the Main Thread
                // completely freezes right here and waits for them to wake up.
                String result = f.get();
                System.out.println("RESULT RETRIEVED: " + result);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("A task failed!");
        }

        // Halts all actively executing and waiting tasks.
        // Since we already used .get() to wait for everyone to finish their work,
        // this just cleanly fires the workers and closes the manager's office.
        dynamiceEX.shutdownNow();

        System.out.println("All reports generated and manager shut down.");
    }
}