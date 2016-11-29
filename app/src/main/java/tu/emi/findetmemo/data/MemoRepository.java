package tu.emi.findetmemo.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

public class MemoRepository {
    private HashMap<UUID, Memo> mData;
    private HashSet<Observer> mObservers;

    public MemoRepository() {
        this.mData = new HashMap<>();
        this.mObservers = new HashSet<>();
    }

    public Memo add(Memo memo) {
        mData.put(memo.uuid, memo);
        onChange(null, memo);
        return memo;
    }

    private void onChange(Memo oldItem, Memo newItem) {
        for (Observer o : mObservers) o.onChange(oldItem, newItem);
    }

    public Collection<Memo> all() {
        return mData.values();
    }

    private int findUnusedId() {
        int id = 1;
        Random rand = new Random();
        do {
            id = rand.nextInt();
        } while (mData.containsKey((Integer) id));
        return id;
    }

    public static interface Observer {
        public void onChange(Memo oldItem, Memo newItem);
    }

    public Observer registerObserver(Observer observer) {
        mObservers.add(observer);
        return observer;
    }

    public void unregisterObserver(Observer observer) {
        mObservers.remove(observer);
    }

}
