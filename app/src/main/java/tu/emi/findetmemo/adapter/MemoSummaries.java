package tu.emi.findetmemo.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import tu.emi.findetmemo.R;
import tu.emi.findetmemo.activity.MainActivity;
import tu.emi.findetmemo.data.Memo;
import tu.emi.findetmemo.data.MemoRepository;
import tu.emi.findetmemo.view.BaseViewHolder;
import tu.emi.findetmemo.view.ViewTemplate;

public class MemoSummaries extends RecyclerView.Adapter<BaseViewHolder> {
    private final Comparator<Memo> comparator;
    private final MemoRepository memos;

    private MemoRepository.Observer observer = null;
    private final ItemTouchHelper touchHelper;
    private int attachedCount = 0;

    private final ArrayList<Memo> sortedMemos;
    private final SparseArray<ViewTemplate> viewTypes;

    private final MainActivity parent;

    private class SwipeToDelete extends ItemTouchHelper.Callback {
        private final VectorDrawableCompat iconDelete;

        private SwipeToDelete() {
            super();
            iconDelete = VectorDrawableCompat.create(parent.getResources(), R.drawable.ic_delete_white_24dp, parent.getTheme());
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, ItemTouchHelper.LEFT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            BaseViewHolder holder = (BaseViewHolder) viewHolder;
            holder.destroy();
            Memo memo = sortedMemos.get(viewHolder.getAdapterPosition());
            memos.remove(memo.uuid);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            Paint p = new Paint();
            View v = viewHolder.itemView;
            p.setColor(0xFFFF0000);
            c.drawRect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom(), p);

            double padding = v.getHeight() * 0.1;
            int left = (int) (v.getRight() - v.getHeight() + padding);
            int right = (int) (v.getRight() - padding);
            int top = (int) (v.getTop() + padding);
            int bottom = (int) (v.getBottom() - padding);

            iconDelete.setBounds(left, top, right, bottom);
            iconDelete.draw(c);

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

    }

    public MemoSummaries(MemoRepository memos, MainActivity parent) {
        this.comparator = new Memo.TitleComparator();
        this.memos = memos;
        this.parent = parent;
        this.touchHelper = new ItemTouchHelper(new SwipeToDelete());

        this.sortedMemos = new ArrayList<>(memos.all());
        Collections.sort(sortedMemos, comparator);

        this.viewTypes = new SparseArray<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        if (attachedCount >= 1) {
            attachedCount++;
            return;
        }


        touchHelper.attachToRecyclerView(recyclerView);

        observer = memos.registerObserver(new MemoRepository.Observer() {
            @Override
            public void onUpdated(Memo newItem) {
                int size = sortedMemos.size();
                int oldIndex = -1;
                int newIndex = size;
                for (int i = 0; i < size && (oldIndex < 0 || newIndex == size); ++i) {
                    Memo m = sortedMemos.get(i);
                    if (m.uuid.equals(newItem.uuid)) {
                        oldIndex = i;
                    }

                    if (comparator.compare(m, newItem) >= 0) {
                        newIndex = i;
                    }
                }

                if (oldIndex < 0)
                    throw new IllegalArgumentException("memo to update does not exist");
                sortedMemos.remove(oldIndex);

                if (newIndex > oldIndex) newIndex--;
                sortedMemos.add(newIndex, newItem);

                notifyItemMoved(oldIndex, newIndex);
                notifyItemChanged(newIndex);
            }

            @Override
            public void onAdded(Memo newItem) {
                int i = Arrays.binarySearch(sortedMemos.toArray(new Memo[0]), newItem, comparator);
                if (i < 0) i = -(i + 1);
                sortedMemos.add(i, newItem);
                notifyItemInserted(i);
            }

            @Override
            public void onRemoved(Memo oldItem) {
                int size = sortedMemos.size();
                for (int i = 0; i < size; ++i) {
                    if (sortedMemos.get(i).uuid.equals(oldItem.uuid)) {
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

        touchHelper.attachToRecyclerView(null);
        memos.unregisterObserver(observer);
        attachedCount--;
        if (attachedCount != 0) throw new RuntimeException("shouldn't happen");

        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        ViewTemplate template = sortedMemos.get(position).summaryViewTemplate();
        viewTypes.put(template.layoutRes, template);
        return template.layoutRes;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View root = LayoutInflater.from(viewGroup.getContext()).inflate(viewType, viewGroup, false);
        return viewTypes.get(viewType).createViewHolder(root, this.parent);
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
