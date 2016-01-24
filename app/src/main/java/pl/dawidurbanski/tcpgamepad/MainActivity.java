package pl.dawidurbanski.tcpgamepad;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    Gamepad gamepad = new Gamepad();
    GamepadInput gamepadInput = new GamepadInput();
    Settings settings = new Settings();
    TextView textView = null;

    FloatingActionButton fab;

    ListView listView = null;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;//DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW


    private void initChildren() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textView = (TextView) findViewById(R.id.textView);


        listView = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        listView.setAdapter(adapter);

        ArrayList<InputDevice> gamepads = gamepad.getGameControllerIds();

        if (!gamepads.isEmpty()) {
            String name = gamepads.get(0).getName();
            if (textView != null)
                textView.setText("GamePad: " + name);
        }
    }

    private void Log2List(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        listItems.add(sdf.format(new Date()) + ":" + str);
        adapter.notifyDataSetChanged();
        listView.setSelection(adapter.getCount() - 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Settings.getInstance().load(getApplicationContext());

        setContentView(R.layout.activity_main);
        initChildren();

        gamepadInput.addOnKeyListener(new GamepadInput.KeyListener() {
            @Override
            public void onKey(GamepadInput.GamepadKey key) {
                Log.i("dawid", "joy click" + key.toString());
                Log2List("Clicked : " + key.toString());
            }
        });

        gamepadInput.addOnMoveListener(new GamepadInput.AxisListener() {
            @Override
            public void onMove(GamepadInput.GamepadAxis axis) {
                Log.i("dawid", "joy move" + axis.toString());
                Log2List("Move : " + axis.toString());
            }
        });


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Connecting to " + Settings.getInstance().adress + ":" + Settings.getInstance().port + "...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                connect();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        GamepadInput.GamepadKey key = gamepadInput.onKeyDown(keyCode, event);
        if (key != null) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (gamepadInput.onGenericMotionEvent(event)) return true;
        return super.onGenericMotionEvent(event);
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
        switch (id) {
            case R.id.action_connection:
                Intent intent2 = new Intent(MainActivity.this, ConnectionSettingsActivity.class);
                startActivity(intent2);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * asynchronous TCP connection
     */
    private interface OnEvent { public void run();   }
    public class TCPconnectionTask extends AsyncTask<Void, Void, Boolean> {

        TCPclient tcPclient = null;

        private OnEvent onEnd = null,onFail = null;

        TCPconnectionTask() {
            tcPclient = new TCPclient(new TCPclient.OnMessageReceived() {
                @Override
                public void messageReceived(String message) {
                    Log.i("dawid", "incomming message: " + message);
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                tcPclient.run(Settings.getInstance().adress, Settings.getInstance().port);
            } catch (Exception e) {
                Log.e("dawid", e.getMessage());
                if(onFail!=null)  onFail.run();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(onEnd!=null)  onEnd.run();
            mAuthTask = null;
        }

        @Override
        protected void onCancelled() {
            if (tcPclient != null)
                tcPclient.stop();
        }
    }

    /**
     * Keep track of the task to ensure we can cancel it if requested.
     */
    private TCPconnectionTask mAuthTask = null;

    private void connect() {
        if (mAuthTask == null) {
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            fab.setImageDrawable(getResources().getDrawable(R.mipmap.ic_on));
            mAuthTask = new TCPconnectionTask();
            mAuthTask.onEnd=new OnEvent() {
                @Override
                public void run() {
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_off));
                }
            };
            mAuthTask.onFail=new OnEvent() {
                @Override
                public void run() {
                    fab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_off));
                }
            };

            mAuthTask.execute((Void) null);
        }
    }
}

