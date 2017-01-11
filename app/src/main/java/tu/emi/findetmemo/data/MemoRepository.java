package tu.emi.findetmemo.data;

import android.content.Context;
import android.widget.Toast;

import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class MemoRepository {
    private final Context context;
    private HashMap<UUID, Memo> mData;
    private final HashSet<Observer> mObservers;
    private final XStream xstream;
    private final File storagePath;

    public MemoRepository(Context context) {
        this.context = context;
        this.mData = new HashMap<>();
        this.mObservers = new HashSet<>();
        this.xstream = new XStream();
        this.storagePath = context.getFileStreamPath("memos.xml");
        load();
    }

    public void update(Memo newItem) {
        if (mData.containsKey(newItem.uuid))
            onUpdated(newItem);
        else
            onAdded(newItem);
        mData.put(newItem.uuid, newItem);
        save();
    }

    public Memo remove(UUID key) {
        Memo oldItem = mData.remove(key);
        if (oldItem != null) onRemoved(oldItem);
        save();
        return oldItem;
    }

    public Memo get(UUID key) {
        return mData.get(key);
    }

    private void load() {
        if (!storagePath.exists()) return;
        //noinspection unchecked
        mData = (HashMap<UUID, Memo>)xstream.fromXML(storagePath);
    }

    private void save() {
        try {
            OutputStream out = new FileOutputStream(storagePath);
            xstream.toXML(mData, out);
            out.close();
            Toast msg = Toast.makeText(context, "saved memos", Toast.LENGTH_SHORT);
            msg.show();
        } catch (IOException e) {
            Toast msg = Toast.makeText(context, "failed to save memos: " + e.getLocalizedMessage(), Toast.LENGTH_LONG);
            msg.show();
        }
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
