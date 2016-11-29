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

public class NewTextMemoActivity extends AppCompatActivity {

    private EditText editTextTitle;
    private EditText editTextContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_text_memo);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextTitle = (EditText) findViewById(R.id.edittext_newtextmemo_title);
        editTextContent = (EditText) findViewById(R.id.edittext_newtextmemo_content);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_text_memo, menu);
        return true;
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
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

    void finishSuccess() {
        TextMemo memo = TextMemo.create(editTextTitle.getText().toString(), editTextContent.getText().toString());
        Intent result = new Intent();
        result.putExtra(Memo.EXTRA_MEMO, memo);
        setResult(RESULT_OK, result);
        finish();
    }

    void finishCancel() {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();

    }
}
