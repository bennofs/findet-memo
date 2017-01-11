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
import tu.emi.findetmemo.classes.SingleAudioPlayer;
import tu.emi.findetmemo.data.Memo;
import tu.emi.findetmemo.data.MemoRepository;
import tu.emi.findetmemo.data.TextMemo;

public class MainActivity extends AppCompatActivity {

    private static final int EDIT_MEMO_REQUEST = 1;

    private MemoRepository memos;

    private SingleAudioPlayer singlePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        FabSpeedDial menuAddNote = (FabSpeedDial) findViewById(R.id.fab_menu_add_note);
        if (menuAddNote == null) throw new RuntimeException("fab menu not found");
        menuAddNote.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_add_text:
                        editMemo(TextMemo.createEmpty());
                        break;
                    case R.id.action_add_audio:
                        Intent intent = new Intent(MainActivity.this, NewAudioMemoActivity.class);
                        startActivityForResult(intent, EDIT_MEMO_REQUEST);
                    default:
                        break;
                }
                return super.onMenuItemSelected(menuItem);
            }
        });


        this.memos = new MemoRepository(this);

        RecyclerView viewMemoList = (RecyclerView) findViewById(R.id.recyclerview_memolist);
        if (viewMemoList == null) throw new RuntimeException("recyclerview memo list not found");
        viewMemoList.setLayoutManager(new LinearLayoutManager(this));
        viewMemoList.setAdapter(new MemoSummaries(memos, this));

        this.singlePlayer = new SingleAudioPlayer(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT_MEMO_REQUEST:
                handleNewMemo(resultCode, (Memo) data.getSerializableExtra(Memo.EXTRA_MEMO));
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleNewMemo(int resultCode, Memo memo) {
        if (resultCode != RESULT_OK || memo.isEmpty()) return;
        memos.update(memo);

        Toast t = Toast.makeText(this, "Updated memo " + memo.common.title, Toast.LENGTH_LONG);
        t.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        singlePlayer.suspend();

        super.onPause();
    }

    public SingleAudioPlayer getSingleAudioPlayer() {
        return singlePlayer;
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


    public void editMemo(TextMemo memo) {
        Intent intent = new Intent(this, TextMemoActivity.class);
        intent.putExtra(Memo.EXTRA_MEMO, memo);

        startActivityForResult(intent, MainActivity.EDIT_MEMO_REQUEST);
    }
}
