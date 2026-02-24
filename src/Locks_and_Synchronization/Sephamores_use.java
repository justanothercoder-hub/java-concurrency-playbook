package Locks_and_Synchronization;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// Enforces a max-table limit for a small restaurant
class BoutiqueRestaurant {

    // A fixed number of tables (permits)
    private final Semaphore tables;

    // Constructor sets table count AND fairness
    public BoutiqueRestaurant(int totalTables) {
        // 'true' means FAIR MODE: Customers who wait the longest get seated first!
        this.tables = new Semaphore(totalTables, true);
    }

    // Customer arrives and asks for a table
    public boolean seatCustomer(String customer) throws InterruptedException {
        System.out.println(customer + " walked in and asked for a table...");

        // DIFFERENCE: Try to grab a permit, but wait up to 3 seconds if full
        if (tables.tryAcquire(3, TimeUnit.SECONDS)) {
            System.out.println("🍽️ " + customer + " was seated!");
            return true;
        } else {
            System.out.println("⏳ " + customer + " got tired of waiting and left.");
            return false;
        }
    }

    // Customer finishes eating and leaves
    public void leave(String customer) {
        System.out.println("👋 " + customer + " paid the bill and left.");
        tables.release();  // Release the permit so the next person in line can sit
    }
}

// Quick demo – 2 tables, 4 customers
class Main {
    public static void main(String[] args) throws InterruptedException {
        // Only 2 tables available!
        BoutiqueRestaurant restaurant = new BoutiqueRestaurant(2);

        // Create an array to hold our customer threads
        Thread[] customers = new Thread[4];

        for (int i = 1; i <= 4; i++) {
            final String name = "Customer-" + i;

            customers[i-1] = new Thread(() -> dine(restaurant, name));
            customers[i-1].start();

            // Stagger arrivals by 100ms so they enter the queue in order
            Thread.sleep(100);
        }

        // Wait for all customers to finish their lifecycle
        for (Thread t : customers) {
            t.join();
        }
    }

    // helper for a customer's dining session
    private static void dine(BoutiqueRestaurant rest, String name) {
        try {
            // If they got a table...
            if (rest.seatCustomer(name)) {
                // ...eating takes 4 seconds.
                // Since they wait max 3 seconds, Customers 3 & 4 will likely give up!
                Thread.sleep(4000);
                rest.leave(name);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}