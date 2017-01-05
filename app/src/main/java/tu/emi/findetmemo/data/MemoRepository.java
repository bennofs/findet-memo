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

    public Memo update(Memo newItem) {
        if(mData.containsKey(newItem.uuid))
            onUpdated(newItem);
        else
            onAdded(newItem);
        return mData.put(newItem.uuid, newItem);
    }

    public Memo remove(UUID key) {
        Memo oldItem = mData.remove(key);
        if(oldItem != null) onRemoved(oldItem);
        return oldItem;
    }

    public Memo get(UUID key) {
        return mData.get(key);
    }

    private void onAdded(Memo newItem) {
        for (Observer o : mObservers) o.onAdded(newItem);
    }

    private void onUpdated(Memo newItem) {
        for (Observer o : mObservers) o.onUpdated(newItem);
    }

    private void onRemoved(Memo oldItem) {
        for (Observer o : mObservers) o.onRemoved(oldItem);
    }

    public Collection<Memo> all() {
        return mData.values();
    }

    public interface Observer {
        void onUpdated(Memo newItem);
        void onAdded(Memo newItem);
        void onRemoved(Memo oldItem);
    }

    public Observer registerObserver(Observer observer) {
        mObservers.add(observer);
        return observer;
    }

    public void unregisterObserver(Observer observer) {
        mObservers.remove(observer);
    }

}
