package Deadlock_and_prevention;


import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

// --- CLASS 1: THE RESOURCE ---
class BankAccount {
    private final String name;
    private int balance;

    // Instead of using the 'synchronized' word, we give every account
    // an explicit physical padlock that we can control manually.
    private final ReentrantLock lock = new ReentrantLock();

    public BankAccount(String name, int balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() { return name; }
    public int getBalance() { return balance; }
    public ReentrantLock getLock() { return lock; }

    public void deposit(int amount) { balance += amount; }
    public void withdraw(int amount) { balance -= amount; }
}

// --- CLASS 2: THE TASK ---
class TryLockTransferTask implements Runnable {
    private final BankAccount from;
    private final BankAccount to;
    private final int amount;

    // We need a random number generator to make sure threads don't retry
    // at the exact same millisecond and crash into each other again.
    private final Random random = new Random();

    public TryLockTransferTask(BankAccount from, BankAccount to, int amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public void run() {
        // "Keep trying forever until the transfer actually works."
        while (true) {
            try {
                // STEP 1: Grab the first key!
                // "Wait up to 50 milliseconds for the first lock. If I get it, move forward."
                if (from.getLock().tryLock(500, TimeUnit.MILLISECONDS)) {

                    try {
                        System.out.println(Thread.currentThread().getName() + " grabbed " + from.getName());

                        // We pause for a moment to guarantee the other thread grabs its first lock too.
                        Thread.sleep(1000);

                        // STEP 2: Try to grab the second key!
                        // "While holding the first lock, wait 50ms for the second lock."
                        if (to.getLock().tryLock(500, TimeUnit.MILLISECONDS)) {

                            try {
                                System.out.println(Thread.currentThread().getName() + " grabbed " + to.getName());

                                // STEP 3: SUCCESS!
                                // We have both keys! Do the math and permanently exit the 'while(true)' loop.
                                from.withdraw(amount);
                                to.deposit(amount);
                                System.out.println("✅ Transferred $" + amount + " from " + from.getName() + " to " + to.getName());

                                return; // This safely stops the thread because the job is done.

                            } finally {
                                // Always put the second key back on the table!
                                to.getLock().unlock();
                            }

                        } else {
                            // THE ESCAPE HATCH: We couldn't get the second lock.
                            // Instead of freezing forever, we just print a message and give up for now.
                            System.out.println("❌ " + Thread.currentThread().getName() + " couldn't get " + to.getName() + ". Giving up and dropping " + from.getName() + "...");
                        }

                    } finally {
                        // CRITICAL: If we failed to get the second lock, we reach this line.
                        // This forces the thread to drop the FIRST lock so the other thread can use it!
                        from.getLock().unlock();
                    }
                }

                // STEP 4: THE BACKOFF
                // If we reached this line, the thread failed to get both locks and dropped everything.
                // We sleep for a random time (between 0 and 50ms) before the 'while' loop restarts.
                // This staggers the threads so one of them can sneak in and win!
                Thread.sleep(random.nextInt(500));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Transfer interrupted!");
                return;
            }
        }
    }
}

// --- CLASS 3: THE MANAGER ---
 class TryLockDeadlockManager {
    public static void main(String[] args) throws InterruptedException {
        BankAccount accountA = new BankAccount("Account-A", 1000);
        BankAccount accountB = new BankAccount("Account-B", 1000);

        // Setup a classic head-on collision (A->B vs B->A)
        Thread t1 = new Thread(new TryLockTransferTask(accountA, accountB, 100), "T1");
        Thread t2 = new Thread(new TryLockTransferTask(accountB, accountA, 200), "T2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println(" Execution finished safely using tryLock().");
    }
}
