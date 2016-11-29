package tu.emi.findetmemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
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

    public MemoSummaries(MemoRepository memos) {
        this.comparator = new Memo.TitleComparator();
        this.memos = memos;

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
            public void onChange(Memo from, Memo to) {
                if (from == null) {
                    MemoSummaries.this.add(to);
                } else if (to == null) {
                    MemoSummaries.this.notifyItemRemoved(sortedMemos.indexOf(from));
                } else {
                    MemoSummaries.this.notifyItemChanged(sortedMemos.indexOf(to));
                }
            }
        });

        super.onAttachedToRecyclerView(recyclerView);
    }

    private void add(Memo memo) {
        int i = 0;
        for (; i < sortedMemos.size(); ++i) {
            if (comparator.compare(sortedMemos.get(i), memo) > 0) break;
        }

        sortedMemos.add(i, memo);
        notifyItemInserted(i);
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
        holder.bind(sortedMemos.get(position));
    }

    @Override
    public int getItemCount() {
        return sortedMemos.size();
    }
}
