package creating_managing_threads;

import java.util.concurrent.*;

// Method 1: The easiest way. Just extend the Thread class directly.
class SetBuilder extends Thread {
    @Override
    public void run(){
        System.out.println("Building the movie set");
    }
}

// Method 2: Implement Runnable. This is just an instruction manual,
// so we will need to hand it to a real Thread later.
class LightingCrew implements Runnable{
    @Override
    public void run(){
        System.out.println("Setting up the lights");
    }
}

// Method 3: Implement Callable. We use this when we want the worker
// to actually return a result (like a String) when they finish.
class CameraOperator implements Callable<String>{
    @Override
    public String call(){
        System.out.println("Filming the actors...");
        try {
            Thread.sleep(2000); // Takes 2 seconds to film
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Raw footage of 2 sec is delivered";
    }
}

class MovieProductionCrew{
    public static void main(String[] args){

        // --- HIRING THE CREW AND STARTING THE WORK ---

        // Starting Method 1 (Extending Thread)
        SetBuilder ob= new SetBuilder();
        ob.start();

        // Starting Method 2 (Runnable)
        // We have to pass our Runnable instructions into a new Thread to actually run it.
        Thread t= new Thread(new LightingCrew());
        t.start();

        // Starting Method 3 (Callable)
        // Callables are special. We have to wrap them in a FutureTask (like a claim ticket)
        // before we can hand them to a Thread.
        CameraOperator op= new CameraOperator();
        FutureTask<String> adapter = new FutureTask<>(op);
        Thread t1 = new Thread(adapter);
        t1.start();

        // Method 4: Lambda Runnable
        // A shortcut! We write the instructions directly inside the Thread using '() ->'
        Thread soundEngineer = new Thread(() -> {
            System.out.println("Checking the microphones...");
        });
        soundEngineer.start();

        // Method 5: Lambda Callable
        // Another shortcut! We write a Callable directly inside a FutureTask.
        // Because it has a "return" statement, Java knows it is a Callable.
        FutureTask<Integer> videoEditorTask = new FutureTask<>(() -> {
            System.out.println("Editing the video...");
            Thread.sleep(5000); // Takes 5 seconds to edit
            return 120;
        });
        Thread videoEditorThread = new Thread(videoEditorTask);
        videoEditorThread.start();


        // --- THE DIRECTOR WAITS FOR THE CREW ---
        try {
            // 1. Wait for the standard tasks to finish.
            // .join() forces the main thread to stand here and wait if they aren't done yet.
            ob.join();
            t.join();
            soundEngineer.join();

            // 2. Cash in our FutureTask claim tickets to get the actual data.
            // If the video editor is still doing their 5-second sleep, the main thread
            // will freeze right here and wait for them to wake up and hand over the result.
            String footage = adapter.get();
            int movieLength = videoEditorTask.get();

            // 3. Everyone is done! Print the final results.
            System.out.println("\n--- PRODUCTION WRAPPED ---");
            System.out.println("Camera Operator delivered: " + footage);
            System.out.println("Final Movie Length: " + movieLength + " minutes");

        } catch (Exception e) {
            System.out.println("Production was interrupted!");
            e.printStackTrace();
        }

    }
}