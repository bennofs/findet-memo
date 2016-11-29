package tu.emi.findetmemo.view;

import android.view.View;

abstract public class ViewTemplate {
    public final int layoutRes;

    protected ViewTemplate(int layoutRes) {
        this.layoutRes = layoutRes;
    }

    public abstract BaseViewHolder createViewHolder(View root);
}
