package tu.emi.findetmemo.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import tu.emi.findetmemo.data.Memo;
import tu.emi.findetmemo.data.MemoRepository;
import tu.emi.findetmemo.view.BaseViewHolder;
import tu.emi.findetmemo.view.ViewTemplate;

public class MemoSummaries extends RecyclerView.Adapter<BaseViewHolder> {
    private final Comparator<Memo> comparator;
    private final MemoRepository memos;

    private MemoRepository.Observer observer = null;
    private int attachedCount = 0;

    private final ArrayList<Memo> sortedMemos;
    private final HashMap<Integer, ViewTemplate> viewTypes;

    private final Activity parent;

    public MemoSummaries(MemoRepository memos, Activity parent) {
        this.comparator = new Memo.TitleComparator();
        this.memos = memos;
        this.parent = parent;

        this.sortedMemos = new ArrayList<>(memos.all());
        Collections.sort(sortedMemos, comparator);

        this.viewTypes = new HashMap<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        if (attachedCount >= 1) {
            attachedCount++;
            return;
        }

        observer = memos.registerObserver(new MemoRepository.Observer() {
            @Override
            public void onUpdated(Memo newItem) {
                int size = sortedMemos.size();
                int oldIndex = -1;
                int newIndex = size;
                for(int i = 0; i < size && (oldIndex < 0 || newIndex == size); ++i) {
                    Memo m = sortedMemos.get(i);
                    if(m.uuid.equals(newItem.uuid)) {
                        oldIndex = i;
                    }

                    if(comparator.compare(m, newItem) >= 0) {
                        newIndex = i;
                    }
                }

                if(oldIndex < 0) throw new IllegalArgumentException("memo to update does not exist");
                sortedMemos.remove(oldIndex);

                if(newIndex > oldIndex) newIndex--;
                sortedMemos.add(newIndex, newItem);

                notifyItemMoved(oldIndex, newIndex);
                notifyItemChanged(newIndex);
            }

            @Override
            public void onAdded(Memo newItem) {
                int i = Arrays.binarySearch(sortedMemos.toArray(new Memo[0]), newItem, comparator);
                if(i < 0) i = -(i + 1);
                sortedMemos.add(i, newItem);
                notifyItemInserted(i);
            }

            @Override
            public void onRemoved(Memo oldItem) {
                int size = sortedMemos.size();
                for(int i = 0; i < size; ++i) {
                    if(sortedMemos.get(i).uuid.equals(oldItem.uuid)) {
                        sortedMemos.remove(i);
                        notifyItemRemoved(i);
                        return;
                    }
                }
                throw new IllegalArgumentException("memo to remove does not exist");
            }
        });

        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (attachedCount > 1) {
            attachedCount--;
            return;
        }

        memos.unregisterObserver(observer);
        attachedCount--;
        assert (attachedCount == 0);

        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        ViewTemplate template = sortedMemos.get(position).summaryViewTemplate();
        viewTypes.put(template.layoutRes, template);
        return template.layoutRes;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return viewTypes.get(viewType).createViewHolder(root);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.bind(sortedMemos.get(position), parent);
    }

    @Override
    public int getItemCount() {
        return sortedMemos.size();
    }
}
