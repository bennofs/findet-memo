package tu.emi.findetmemo.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

abstract public class BaseViewHolder extends RecyclerView.ViewHolder {
    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    View findViewById(int id) {
        return itemView.findViewById(id);
    }

    public abstract void bind(Object data);
    public abstract void destroy();
}
