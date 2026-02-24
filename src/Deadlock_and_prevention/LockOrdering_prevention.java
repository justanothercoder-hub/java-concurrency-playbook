package Deadlock_and_prevention;

class Database {
    private final String name;
    private int dataRows;

    public Database(String name, int dataRows) {
        this.name = name;
        this.dataRows = dataRows;
    }

    public String getName() { return name; }
    public int getRows() { return dataRows; }

    public void addRows(int count) { dataRows += count; }
    public void removeRows(int count) { dataRows -= count; }
}

class SyncTask implements Runnable {
    private final Database dbFrom;
    private final Database dbTo;

    // We pass the master array into the task so the thread can use it as a "rulebook".
    // It will look at this array to figure out which database is supposed to be locked first.
    private final Database[] masterArray;

    public SyncTask(Database dbFrom, Database dbTo, Database[] masterArray) {
        this.dbFrom = dbFrom;
        this.dbTo = dbTo;
        this.masterArray = masterArray;
    }

    private int getIndex(Database db) {
        for (int i = 0; i < masterArray.length; i++) {
            if (masterArray[i] == db) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void run() {
        int indexFrom = getIndex(dbFrom);
        int indexTo = getIndex(dbTo);

        Database firstLock;
        Database secondLock;

        // WHY WE USE THE ARRAY:
        // This 'if' statement is the magic that prevents the deadlock.
        // Even if Thread 2 wants to modify dbWest first, it checks the array and realizes
        // dbEast has a lower index (0). So, it forces itself to lock dbEast first instead.
        // Because both threads follow this exact same math, they will never cross paths!
        if (indexFrom < indexTo) {
            firstLock = dbFrom;
            secondLock = dbTo;
        } else {
            firstLock = dbTo;
            secondLock = dbFrom;
        }

        synchronized (firstLock) {
            System.out.println(Thread.currentThread().getName() + " locked " + firstLock.getName() + " (Found at Index " + Math.min(indexFrom, indexTo) + ")");

            try { Thread.sleep(100); } catch (InterruptedException ignored) {}

            synchronized (secondLock) {
                System.out.println(Thread.currentThread().getName() + " locked " + secondLock.getName() + " (Found at Index " + Math.max(indexFrom, indexTo) + ")");

                dbFrom.removeRows(10);
                dbTo.addRows(10);

                System.out.println("✅ Synced 10 rows from " + dbFrom.getName() + " to " + dbTo.getName());
            }
        }
    }
}
class DatabaseSyncManager {

    public static void main(String[] args) throws InterruptedException {
        Database dbEast = new Database("DB-East", 500);
        Database dbWest = new Database("DB-West", 500);

        // WHY WE USE THE ARRAY HERE:
        // We establish one single "Source of Truth" for the entire application.
        // By putting East at index 0 and West at index 1, we create a permanent global order.
        Database[] globalRegistry = new Database[]{dbEast, dbWest};

        // WHY WE USE THESE TWO THREADS:
        // We create these two specific threads to simulate a "head-on collision."
        // Thread 1 wants to lock East, then West.
        // Thread 2 wants to lock West, then East.
        // Without the array logic above, these two threads would instantly cause a Deadlock.
        Thread t1 = new Thread(new SyncTask(dbEast, dbWest, globalRegistry), "Thread-EastToWest");
        Thread t2 = new Thread(new SyncTask(dbWest, dbEast, globalRegistry), "Thread-WestToEast");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("\n🎉 No Deadlocks! Execution finished safely.");
        System.out.println(dbEast.getName() + " final rows: " + dbEast.getRows());
        System.out.println(dbWest.getName() + " final rows: " + dbWest.getRows());
    }
}