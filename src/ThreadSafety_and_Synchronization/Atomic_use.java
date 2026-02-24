package ThreadSafety_and_Synchronization;

import java.util.concurrent.atomic.AtomicBoolean;

// --- CLASS 1: THE SHARED RESOURCE ---
class FlashSale {

    // Atomic variables use CPU-level math to update safely. No 'synchronized' needed!
    // false = prize is still available
    private final AtomicBoolean prizeClaimed = new AtomicBoolean(false);

    public void tryClaimPrize(String user) {

        // compareAndSet(expectedOldValue, newValue)
        // "If the value is EXACTLY false right now, instantly make it true and return true."
        // If someone else already made it true, do nothing and return false.
        boolean success = prizeClaimed.compareAndSet(false, true);

        if (success) {
            System.out.println("🎉 " + user + " WAS FIRST! Prize claimed!");
        } else {
            System.out.println("❌ " + user + " was too late. Prize is gone.");
        }
    }
}

// --- CLASS 2: THE MANAGER ---
class AtomicManager {
    public static void main(String[] args) {
        FlashSale sale = new FlashSale();

        // 4 users trying to click the "Claim" button at the exact same millisecond
        new Thread(() -> sale.tryClaimPrize("User 1")).start();
        new Thread(() -> sale.tryClaimPrize("User 2")).start();
        new Thread(() -> sale.tryClaimPrize("User 3")).start();
        new Thread(() -> sale.tryClaimPrize("User 4")).start();
    }
}
