package pl.dawidurbanski.tcpgamepad;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import pl.dawidurbanski.tcpgamepad.ADdrone.Message;
import pl.dawidurbanski.tcpgamepad.Connection.ConnectionFragment;
import pl.dawidurbanski.tcpgamepad.GamePadHandler.GamePadFragment;
import pl.dawidurbanski.tcpgamepad.GamePadHandler.GamePadInput;
import pl.dawidurbanski.tcpgamepad.Logs.LogsFragment;
import pl.dawidurbanski.tcpgamepad.VirtualGamePad.VirtualGamePadFragment;

public class Tabedctivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private CustomViewPager mViewPager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabedctivity);

        Settings.getInstance().load(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mSectionsPagerAdapter.mConnectionFragment.onLog = new ConnectionFragment.OnEvent() {
            @Override
            public void run(String str) {
                Log2List(str);
            }
        };

        //Floating Action Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { tick(); }
        }, 0, Settings.getInstance().messageRetransmissionRate);
    }

    /*
     * this will run on messageRetransmissionRate
     */
    private void tick() {
        if(message==null)
            return;

        mSectionsPagerAdapter.mConnectionFragment.sendBytes(message);
        if(--messageNum==0) {
            Log2List("Move: timeout");
            message = null;
        }
    }

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

    private int messageNum =0;
    private byte [] message = null;//if null no data will be sent
    private void sentMessage(String name,float axis1,float axis2,float axis3,float axis4)
    {
        messageNum = Settings.getInstance().messageRetransmissionNum;
        message = Message.generate(axis1, axis2, axis3, axis4);
        String messageStr = Message.toStringAsInts(message).replace("|", "\n");

        String axisStr = ""
                + String.format("%+.01f ", axis1)+ ","
                + String.format("%+.01f ", axis2)+ " "
                + String.format("%+.01f ", axis3)+ ","
                + String.format("%+.01f ", axis4)+ " ";
        Log2List("Move: '"+name+"'" + axisStr + " re-transmission"+Settings.getInstance().getMessageRetransmissionTime()+"sec \n" + messageStr);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        GamePadInput.GamePadAxis axis = mSectionsPagerAdapter.mGamePadFragment.onGenericMotionEvent(event);
        if(axis!=null)
        {
            sentMessage("gamePad", axis.leftControleStickX, axis.leftControleStickY, axis.rightControleStickX, axis.rightControleStickY);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    private int mSelectedPage = -1;

    //called by ViewPager
    private void onViewPagerSelectPage(int position)
    {
        if(position==mSelectedPage) return;
        mSelectedPage=position;

        if(position==3)  mViewPager.setPagingEnabled(false);
        else             mViewPager.setPagingEnabled(true);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

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
                    sentMessage("virtual",x,y,a,throttle);
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
