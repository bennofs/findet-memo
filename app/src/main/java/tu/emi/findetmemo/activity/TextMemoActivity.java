package tu.emi.findetmemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import tu.emi.findetmemo.R;
import tu.emi.findetmemo.data.Memo;
import tu.emi.findetmemo.data.TextMemo;

public class TextMemoActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextContent;
    private TextMemo memo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_memo);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        memo = (TextMemo) getIntent().getSerializableExtra(Memo.EXTRA_MEMO);

        editTextTitle = (EditText) findViewById(R.id.edittext_newtextmemo_title);
        editTextContent = (EditText) findViewById(R.id.edittext_newtextmemo_content);

        editTextTitle.setText(memo.common.title);
        editTextContent.setText(memo.textBody);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_text_memo, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        finishSuccess();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_newtextmemo_cancel:
                finishCancel();
                break;
            case android.R.id.home:
                finishSuccess();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void finishSuccess() {
        final String title = editTextTitle.getText().toString().trim();
        final String content = editTextContent.getText().toString().trim();
        final Memo changedMemo =
                memo.withTextBody(content).withCommon(memo.common.withTitle(title));

        Intent result = new Intent();
        result.putExtra(Memo.EXTRA_MEMO, changedMemo);
        setResult(RESULT_OK, result);
        finish();
    }

    private void finishCancel() {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();

    }
}
