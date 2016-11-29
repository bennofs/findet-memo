package tu.emi.findetmemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import tu.emi.findetmemo.R;
import tu.emi.findetmemo.adapter.MemoSummaries;
import tu.emi.findetmemo.data.Memo;
import tu.emi.findetmemo.data.MemoRepository;
import tu.emi.findetmemo.data.TextMemo;

public class MainActivity extends AppCompatActivity {

    private static final int NEW_MEMO_REQUEST = 1;

    private MemoRepository memos;

    private RecyclerView viewMemoList;

    public MainActivity() {
        this.memos = new MemoRepository();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        FabSpeedDial menuAddNote = (FabSpeedDial) findViewById(R.id.fab_menu_add_note);
        menuAddNote.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_add_text:
                        Intent intent = new Intent(MainActivity.this, NewTextMemoActivity.class);
                        MainActivity.this.startActivityForResult(intent, NEW_MEMO_REQUEST);
                        break;
                    default:
                        break;
                }
                return super.onMenuItemSelected(menuItem);
            }
        });

        memos.add(TextMemo.create("foo", "BAR"));
        viewMemoList = (RecyclerView) findViewById(R.id.recyclerview_memolist);
        viewMemoList.setLayoutManager(new LinearLayoutManager(this));
        viewMemoList.setAdapter(new MemoSummaries(memos));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case NEW_MEMO_REQUEST:
                handleNewMemo(resultCode, (Memo) data.getSerializableExtra(Memo.EXTRA_MEMO));
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleNewMemo(int resultCode, Memo memo) {
        if (resultCode != RESULT_OK) return;
        memos.add(memo);

        Toast t = Toast.makeText(this, "Created new memo", Toast.LENGTH_LONG);
        t.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}