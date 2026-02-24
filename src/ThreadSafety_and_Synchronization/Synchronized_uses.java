package ThreadSafety_and_Synchronization;

// --- CLASS 1: THE SHARED RESOURCE ---
class CoffeeShop {

    // 1. GLOBAL STATE: Shared by every single coffee shop in the system.
    // (Note: Just missing the word 'int' here!)
    private static int globalbeans= 100;

    // 2. LOCAL STATE: Belongs only to this specific coffee shop building.
    private int localbeans=0;

    // 3. THE DUMMY LOCK: A dedicated padlock just for the milk steamer.
    private final Object lock = new Object();

    // TYPE A: Static Synchronized
    // Locks the entire CoffeeShop blueprint. No matter how many shops exist,
    // only ONE thread in the entire program can run this at a time.
    static synchronized void consumeGlobalBeans(){
        globalbeans--;
    }

    // TYPE B: Instance Synchronized
    // Locks 'this' specific CoffeeShop building. This prevents two baristas
    // from adding to 'localbeans' at the exact same millisecond and losing count.
    synchronized void brewCoffee(String barista){
        localbeans++;
        consumeGlobalBeans();
        System.out.println(barista + " brewed a coffee. Local beans: " + localbeans + ", Global beans: " + globalbeans);
    }

    // TYPE C: Synchronized Block (Fine-grained locking)
    // We don't lock the whole method! Anyone can walk up to the counter...
    void steamMilk(String barista){

        // ...but they have to grab this specific padlock to actually use the steamer.
        synchronized(lock){
            try {
                Thread.sleep(2500); // Simulate time taken to steam milk
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println(barista + " is steaming milk.");
        }
    }
}

// --- CLASS 2: THE MANAGER ---
class CoffeeManager {

    // (Note: Java requires 'throws InterruptedException' here to use .join() below!)
    public static void main(String[] args)throws InterruptedException {

        // Build one coffee shop
        CoffeeShop shop = new CoffeeShop();

        // Give Barista 1 their instruction manual
        Runnable barista1 = () -> {
            for (int i = 0; i < 5; i++) {
                shop.brewCoffee("Barista 1");
                shop.steamMilk("Barista 1");
            }
        };

        // Give Barista 2 their instruction manual
        Runnable barista2 = () -> {
            for (int i = 0; i < 5; i++) {
                shop.brewCoffee("Barista 2");
                shop.steamMilk("Barista 2");
            }
        };

        // Hire the threads and hand them their manuals
        Thread thread1 = new Thread(barista1);
        Thread thread2 = new Thread(barista2);

        // Tell them to start working at the exact same time
        thread1.start();
        thread2.start();

        // The Manager (Main thread) freezes here and waits for them to finish their shifts
        thread1.join();
        thread2.join();
    }
}