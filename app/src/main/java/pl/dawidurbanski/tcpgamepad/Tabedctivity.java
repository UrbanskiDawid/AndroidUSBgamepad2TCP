package pl.dawidurbanski.tcpgamepad;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import pl.dawidurbanski.tcpgamepad.ADdrone.Message;
import pl.dawidurbanski.tcpgamepad.ADdrone.MessageRetransmissionLogic;
import pl.dawidurbanski.tcpgamepad.Connection.ConnectionFragment;
import pl.dawidurbanski.tcpgamepad.GamePadHandler.GamePadFragment;
import pl.dawidurbanski.tcpgamepad.GamePadHandler.GamePadInput;
import pl.dawidurbanski.tcpgamepad.LatencyTest.OpticalLatencyTestFragment;
import pl.dawidurbanski.tcpgamepad.Logs.LogsFragment;
import pl.dawidurbanski.tcpgamepad.VirtualGamePad.VirtualGamePadFragment;
import pl.dawidurbanski.tcpgamepad.tools.CustomViewPager;
import pl.dawidurbanski.tcpgamepad.tools.OnBackKeyActions;

public class Tabedctivity extends AppCompatActivity implements Message.ADdroneMessageInterface {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    private CustomViewPager mViewPager = null;

    private Toolbar mToolbar = null;
    private FloatingActionButton fab;

    private ImageView mSignal = null;

    private MessageRetransmissionLogic mMessageRetransmissionLogic = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tabedctivity, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabedctivity);

        Settings.getInstance().load(getApplicationContext());

        //Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(null);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId())
            {
                case R.id.menu_optical_latency_tester:
                    startOpticalLatencyTest();
                break;
                case R.id.menu_about:
                    AboutFragment.popup(getSupportFragmentManager());
                break;
            }
            return false;
            }
        });
        //----

        mOnBack = new OnBackKeyActions(2000, getApplicationContext());
        mOnBack.setDefaultAction(null, new OnBackKeyActions.iActions() {
            @Override
            public void onDoublePress() { Tabedctivity.super.onBackPressed(); }
        });

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager= (CustomViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                onViewPagerSelectPage(position);
            }

            @Override
            public void onPageSelected(int position) {
                onViewPagerSelectPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mSectionsPagerAdapter.mConnectionFragment.onLog = new ConnectionFragment.OnEvent() {
            @Override
            public void run(String str) {
                Log2List(str);
            }
        };

        //Floating Action Button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(mSectionsPagerAdapter.mConnectionFragment.isConnected()) {
                mSectionsPagerAdapter.mConnectionFragment.disconnect();
            }else{
                mSectionsPagerAdapter.mConnectionFragment.connect();
            }
            }
        });
        //--

        mSignal = (ImageView)findViewById(R.id.signal);

        //handle messages
        mMessageRetransmissionLogic = new MessageRetransmissionLogic(new MessageRetransmissionLogic.iEvents() {
            @Override
            public void onTransmit(byte[] message) {
                Log2List("outgoing: 0x"+Message.toHexString(message) );
                mSectionsPagerAdapter.mConnectionFragment.sendBytes(message);
            }
        });
        //---
    }

    private void startOpticalLatencyTest() {

        if (!mSectionsPagerAdapter.mConnectionFragment.isConnected()) {
            new AlertDialog.Builder(Tabedctivity.this)
            .setTitle(getString(R.string.app_name))
            .setMessage(getString(R.string.error_mustBeConnect))
            .setPositiveButton("OK", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
            return;
        }

        mMessageRetransmissionLogic.stop();

        new AlertDialog.Builder(Tabedctivity.this)
        .setTitle(getString(R.string.app_name))
        .setMessage(getString(R.string.OpticalLatencyTestFragment_howto))
        .setPositiveButton("START", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSectionsPagerAdapter.mConnectionFragment.connect();
                //TODO: confirm connection
                DialogFragment d = new OpticalLatencyTestFragment();
                d.show(getSupportFragmentManager(), "OpticalLatencyTestFragment");
            }
        })
        .setNegativeButton("CANCEL", null)
        .setIcon(android.R.drawable.ic_dialog_info)
        .show();

        mMessageRetransmissionLogic.reset();
    }

    OnBackKeyActions mOnBack = null;

    @Override
    public void onBackPressed() {
        mOnBack.onPress();
    }

    private int DrawableID = -1;
    private int DrawableID_signal = R.drawable.ic_signal_cellular_off_black_24dp;
    private void onConnectionStatusChange(ConnectionFragment.ConnectionStatus newStatus) {
        Log.v("ConnectionStatusChange",newStatus.toString());

        switch (newStatus)
        {
            case disconnected: DrawableID = android.R.drawable.button_onoff_indicator_off;
                               DrawableID_signal = R.drawable.ic_signal_cellular_off_black_24dp;
            break;
            case connected:    DrawableID = android.R.drawable.button_onoff_indicator_on;
                               DrawableID_signal = R.drawable.ic_signal_cellular_4_bar_black_24dp;
            break;
            case connecting:   DrawableID = android.R.drawable.ic_menu_recent_history;
            break;
            case error:        DrawableID = android.R.drawable.ic_dialog_alert;
                               DrawableID_signal = R.drawable.ic_signal_cellular_off_black_24dp;
            break;
        }
        if(DrawableID==-1)
            return;

        runOnUiThread(new Runnable() {
            public void run() {
                fab.setImageDrawable( getResources().getDrawable(DrawableID, getBaseContext().getTheme()) );
                mSignal.setImageDrawable( getResources().getDrawable(DrawableID_signal, getBaseContext().getTheme()) );
            }
        });
    }

    /**
     * put string to user visible logs
     */
    private void Log2List(String str) {
        mSectionsPagerAdapter.mLogFragment.Log2List(str);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        GamePadInput.GamePadKey key = mSectionsPagerAdapter.mGamePadFragment.onKey(keyCode,event,true);
        if(key!=null)
        {
            Log2List("key down: " + key.toString());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        GamePadInput.GamePadKey key = mSectionsPagerAdapter.mGamePadFragment.onKey(keyCode,event,false);
        if(key!=null)
        {
            Log2List("key up:" + key.toString());
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * this queues message to be handled by MessageRetransmissionLogic
     */
    @Override
    public void sendMessage(String name, float axis1, float axis2, float axis3, float axis4)
    {
        String axisStr = ""
            + String.format("%+.01f ", axis1)+ ","
            + String.format("%+.01f ", axis2)+ " "
            + String.format("%+.01f ", axis3)+ ","
            + String.format("%+.01f ", axis4)+ " ";

        Log2List("Move: '"+name+"'" + axisStr + "; re-transmission"+Settings.getInstance().getMessageRetransmissionTimeInSec()+"sec");

        mMessageRetransmissionLogic.sendMessage(name,axis1,axis2,axis3,axis4);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        GamePadInput.GamePadAxis axis = mSectionsPagerAdapter.mGamePadFragment.onGenericMotionEvent(event);
        if(axis!=null)
        {
            sendMessage("gamePad", axis.leftControleStickX, axis.leftControleStickY, axis.rightControleStickX, axis.rightControleStickY);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    private int mSelectedPageID = -1;

    //called by ViewPager
    private void onViewPagerSelectPage(int position)
    {
        if(position== mSelectedPageID) return;
        mSelectedPageID =position;
        Log.d(Tabedctivity.class.getName(),"selected tab: "+position);

        if(position==3) {
            setFullScreenMode(true);
            mOnBack.setCustomAction(
                "Please click BACK again to exit full-screen mode",
                new OnBackKeyActions.iActions() {
                    @Override
                    public void onDoublePress() {
                        setFullScreenMode(false);
                    }},
                true);
            Toast.makeText(getApplicationContext(),"Please click BACK twice to exit full-screen mode",Toast.LENGTH_SHORT).show();
        }
        else{
            setFullScreenMode(false);
        }
    }

    public void setFullScreenMode(boolean fullScreen)
    {
        android.support.v7.app.ActionBar ab=getSupportActionBar();
        Window window = getWindow();

        if(fullScreen) {
            mViewPager.setPagingEnabled(false);
            if(ab!=null) ab.hide();
            if(fab!=null) fab.hide();
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            mTabLayout.setVisibility(View.GONE);
        }else{
            mViewPager.setPagingEnabled(true);
            if(ab!=null) ab.show();
            if(fab!=null) fab.show();
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            mTabLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        //BUG: this is not how you use FragmentMenager
        //Fragments
        public LogsFragment mLogFragment = LogsFragment.newInstance();
        public ConnectionFragment mConnectionFragment = ConnectionFragment.newInstance();
        public GamePadFragment mGamePadFragment = GamePadFragment.newInstance();
        public VirtualGamePadFragment mVirtualGamePad = new VirtualGamePadFragment();

        private Pair<CharSequence,Fragment> mNamedFragments[];

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            mNamedFragments = new Pair[]
                    {
                            Pair.create("log",        mLogFragment ),
                            Pair.create("gamePad",    mGamePadFragment),
                            Pair.create("connection", mConnectionFragment),
                            Pair.create("virtualGamePad",mVirtualGamePad)
                    };

            mVirtualGamePad.onMove = new VirtualGamePadFragment.OnEvent() {
                @Override
                public void onMove(float x, float y, float a, float b) {
                    // throttle is value form 0 to 1
                    float throttle = (b + 1.0f) / 2.0f;
                    sendMessage("virtual",x,y,a,throttle);
                }
            };

            mConnectionFragment.onSave =new ConnectionFragment.OnEvent() {
                @Override
                public void run(String str) {
                    mMessageRetransmissionLogic.reset();
                }
            };

            mConnectionFragment.onConnectionStatusChange = new ConnectionFragment.OnConnectionStatusEvent() {
                @Override
                public void change(ConnectionFragment.ConnectionStatus newStatus) {
                    onConnectionStatusChange(newStatus);
                }
            };

            mConnectionFragment.onLog = new ConnectionFragment.OnEvent() {
                @Override
                public void run(String str) {
                    Log2List("ConnectionFragment: "+str);
                }
            };
        }

        @Override
        public Fragment getItem(int position) {
            return mNamedFragments[position].second;
        }

        @Override
        public int getCount() {
            return mNamedFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position<0 && position> mNamedFragments.length)
                return null;
            return mNamedFragments[position].first;
        }
    }
}
