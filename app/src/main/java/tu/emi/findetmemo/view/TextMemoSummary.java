package tu.emi.findetmemo.view;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;

import tu.emi.findetmemo.R;
import tu.emi.findetmemo.activity.MainActivity;
import tu.emi.findetmemo.data.TextMemo;

public class TextMemoSummary extends ViewTemplate {
    public static class ViewHolder extends BaseViewHolder {
        public final TextView viewTitle;
        public final CardView viewCard;
        public final TextView viewDate;
        public final Button viewShare;

        private final MainActivity parent;

        public ViewHolder(View root, MainActivity parent) {
            super(root);
            this.parent = parent;

            viewTitle = (TextView) findViewById(R.id.textview_memosummary_title);
            viewCard = (CardView) findViewById(R.id.cardview_memosummary_main);
            viewDate = (TextView) findViewById(R.id.textview_memosummary_date);
            viewShare = (Button) findViewById(R.id.button_memosummary_share);
        }

        @Override
        public void bind(Object data) {
            final TextMemo memo = (TextMemo) data;
            viewTitle.setText(memo.common.title);

            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(itemView.getContext().getApplicationContext());
            viewDate.setText(dateFormat.format(memo.common.creationDate));
            viewCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parent.editMemo(memo);
                }
            });

            viewShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, memo.sharedText());
                    shareIntent.setType("text/plain");
                    parent.startActivity(shareIntent);
                }
            });
        }

        @Override
        public void destroy() {}
    }

    public TextMemoSummary() {
        super(R.layout.layout_text_memo_summary);
    }

    @Override
    public BaseViewHolder createViewHolder(View root, MainActivity parent) {
        return new ViewHolder(root, parent);
    }
}
