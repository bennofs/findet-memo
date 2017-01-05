package tu.emi.findetmemo.view;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;

import tu.emi.findetmemo.R;
import tu.emi.findetmemo.activity.MainActivity;
import tu.emi.findetmemo.activity.TextMemoActivity;
import tu.emi.findetmemo.data.Memo;
import tu.emi.findetmemo.data.TextMemo;

public class TextMemoSummary extends ViewTemplate {
    public static class ViewHolder extends BaseViewHolder {
        public final TextView viewTitle;
        public final CardView viewCard;
        public final TextView viewDate;

        public ViewHolder(View root) {
            super(root);
            viewTitle = (TextView) findViewById(R.id.textview_memosummary_title);
            viewCard = (CardView) findViewById(R.id.cardview_memosummary_main);
            viewDate = (TextView) findViewById(R.id.textview_memosummary_date);
        }

        @Override
        public void bind(Object data, final Activity parent) {
            final TextMemo memo = (TextMemo) data;
            viewTitle.setText(memo.common.title);

            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(itemView.getContext().getApplicationContext());
            viewDate.setText(dateFormat.format(memo.common.creationDate));
            viewCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(parent, TextMemoActivity.class);
                    intent.putExtra(Memo.EXTRA_MEMO, memo);

                    parent.startActivityForResult(intent, MainActivity.NEW_MEMO_REQUEST);
                }
            });
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
