package tu.emi.findetmemo.view;

import android.view.View;
import android.widget.TextView;

import tu.emi.findetmemo.R;
import tu.emi.findetmemo.data.TextMemo;

public class TextMemoSummary extends ViewTemplate {
    public static class ViewHolder extends BaseViewHolder {
        public final TextView viewTitle;

        public ViewHolder(View root) {
            super(root);
            viewTitle = (TextView) findViewById(R.id.textview_memosummary_title);
        }

        @Override
        public void bind(Object data) {
            TextMemo memo = (TextMemo) data;
            viewTitle.setText(memo.title);
        }
    }

    public TextMemoSummary() {
        super(R.layout.layout_text_memo_summary);
    }

    @Override
    public BaseViewHolder createViewHolder(View root) {
        return new ViewHolder(root);
    }
}
