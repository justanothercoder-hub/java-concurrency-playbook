package Locks_and_Synchronization;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class ChargingStation {

    // The single, physical padlock for this charging station
    ReentrantLock stationLock = new ReentrantLock();

    // METHOD 1: The Standard Lock (Wait forever)
    void chargeNormal(String carName){
        stationLock.lock(); // Blocks until it gets the key
        try{
            // Just a cool utility to double-check that THIS thread actually owns the key
            if(stationLock.isHeldByCurrentThread()) {
                System.out.println(carName + " safely holds the lock and is charging.");
            }
            Thread.sleep(4000);
            System.out.println(carName + " has finished charging.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stationLock.unlock(); // Always release the key in a finally block!
        }
    }

    // METHOD 2: The Impatient Lock (Fail fast)
    void chargeImpatient(String carName){
        // tryLock() returns true instantly if the key is available, false if not
        if(stationLock.tryLock()){
            try{
                System.out.println(carName + " grabbed the open station and is charging.");
                Thread.sleep(4000);
                System.out.println(carName + " has finished charging.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                stationLock.unlock(); // Only unlock if we actually got the lock!
            }
        } else {
            System.out.println(carName + " couldn't acquire the lock instantly and is leaving.");
        }
    }

    // METHOD 3: The Patient Lock (Wait up to 5 seconds)
    void chargeWithPatienceSuccess(String carName){
        try {
            // Waits exactly 5 seconds. Since Car 1 takes 4 seconds, this WILL succeed!
            if(stationLock.tryLock(5, TimeUnit.SECONDS)){
                try{
                    System.out.println(carName + " waited successfully and is charging.");
                    Thread.sleep(4000);
                    System.out.println(carName + " has finished charging.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    stationLock.unlock();
                }
            } else {
                System.out.println(carName + " couldn't acquire the lock within 5 seconds and is leaving.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // METHOD 4: The Semi-Patient Lock (Wait up to 2 seconds)
    void chargeWithPatienceFail(String carName){
        try {
            // Waits 2 seconds. Since Car 1 takes 4 seconds, this WILL fail!
            if(stationLock.tryLock(2, TimeUnit.SECONDS)){
                try{
                    System.out.println(carName + " is charging at the station.");
                    Thread.sleep(4000);
                    System.out.println(carName + " has finished charging.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    stationLock.unlock();
                }
            } else {
                System.out.println(carName + " waited 2 seconds, got tired, and is leaving.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

class CarManager {
    public static void main(String[] args) throws InterruptedException {
        ChargingStation station = new ChargingStation();

        Thread car1 = new Thread(() -> station.chargeNormal("Car 1"));
        Thread car2 = new Thread(() -> station.chargeImpatient("Car 2"));
        Thread car3 = new Thread(() -> station.chargeWithPatienceSuccess("Car 3"));
        Thread car4 = new Thread(() -> station.chargeWithPatienceFail("Car 4"));

        // 1. START CAR 1 FIRST!
        car1.start();

        // 2. Let Car 1 fully plug in and lock the station
        Thread.sleep(500);

        // 3. Now let the other three cars arrive and fight over the locked station
        car2.start();
        car3.start();
        car4.start();

        // Wait for everyone to finish
        car1.join();
        car2.join();
        car3.join();
        car4.join();

        System.out.println("All cars processed!");
    }
}