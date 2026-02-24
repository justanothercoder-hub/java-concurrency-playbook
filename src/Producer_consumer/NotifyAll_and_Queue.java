package Producer_consumer;

import java.util.LinkedList;
import java.util.Queue;

class CoffeeCounter {

    // The shared belt holding the coffees
    private final Queue<String> drinks = new LinkedList<>();
    // The maximum number of coffees the counter can hold at once
    private final int MAX_DRINKS = 2;

    // PRODUCER METHOD: Adds a coffee to the counter
    public synchronized void makeCoffee(String name) {

        // If the counter is full, the Barista must wait for space to open up.
        while(drinks.size() == MAX_DRINKS) {
            try {
                System.out.println("⚠️ Counter is full, " + Thread.currentThread().getName() + " is waiting...");
                wait(); // Drop the lock and go to sleep
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Space is available! Put the coffee on the counter.
        drinks.offer(name);
        System.out.println("☕ " + Thread.currentThread().getName() + " made: " + name);

        // Wake up any sleeping Customers to tell them a coffee is ready.
        notifyAll();
    }

    // CONSUMER METHOD: Takes a coffee from the counter
    public synchronized void pickupCoffee() {

        // If the counter is empty, the Customer must wait for the Barista to make something.
        while(drinks.isEmpty()) {
            try {
                System.out.println("😴 Counter is empty, " + Thread.currentThread().getName() + " is waiting...");
                wait(); // Drop the lock and go to sleep
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Coffee is here! Take it off the counter.
        String name = drinks.poll();
        System.out.println("✅ " + name + " is picked up by " + Thread.currentThread().getName());

        // Wake up any sleeping Baristas to tell them space just opened up.
        notifyAll();
    }
}

class Manager {
    public static void main(String args[]) {
        CoffeeCounter counter = new CoffeeCounter();

        // THREAD 1: The Producer
        Thread producer = new Thread(() -> {
            String[] coffeeNames = {"Espresso", "Latte", "Cappuccino", "Americano"};
            for(String name : coffeeNames) {
                counter.makeCoffee(name);
                try {
                    Thread.sleep(1); // Barista works incredibly fast!
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Barista");

        // THREAD 2: The Consumer
        Thread consumer = new Thread(() -> {
            for(int i = 0; i < 4; i++) {
                counter.pickupCoffee();
                try {
                    Thread.sleep(1500); // Customer is very slow to pick up drinks
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Customer");
//CHANGE THE THREAD SLEEPING TIME ACCORDINGLY TO TEST THE WAIT-----
        producer.start();
        consumer.start();
    }
}