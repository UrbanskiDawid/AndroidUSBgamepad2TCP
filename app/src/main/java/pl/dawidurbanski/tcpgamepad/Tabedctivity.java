package pl.dawidurbanski.tcpgamepad;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import pl.dawidurbanski.tcpgamepad.ADdrone.Message;
import pl.dawidurbanski.tcpgamepad.Connection.ConnectionFragment;
import pl.dawidurbanski.tcpgamepad.GamePad.GamePadFragment;
import pl.dawidurbanski.tcpgamepad.GamePad.GamePadInput;
import pl.dawidurbanski.tcpgamepad.Logs.LogsFragment;

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
        ViewPager mViewPager;
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

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

    private void sentMessage(String name,float axis1,float axis2,float axis3,float axis4)
    {
        String axisStr = ""
                + String.format("%+.01f ", axis1)+ ","
                + String.format("%+.01f ", axis2)+ " "
                + String.format("%+.01f ", axis3)+ ","
                + String.format("%+.01f ", axis4)+ " ";

        byte [] message = Message.generate(axis1, axis2, axis3, axis4);
        String messageStr = Message.toStringAsInts(message).replace("|", "\n");

        String isSendStr;
        if(mSectionsPagerAdapter.mConnectionFragment.sendBytes(message))
        {  isSendStr="yes";}
        else
        {  isSendStr="no";}

        Log2List("Move : '"+name+"'" + axisStr + "\n" + messageStr + "\nsent: "+isSendStr);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        GamePadInput.GamePadAxis axis = mSectionsPagerAdapter.mGamePadFragment.onGenericMotionEvent(event);
        if(axis!=null)
        {
            sentMessage("gampad", axis.leftControleStickX, axis.leftControleStickY, axis.rightControleStickX, axis.rightControleStickY);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        //Fragments
        public LogsFragment mLogFragment = LogsFragment.newInstance("logs");
        public ConnectionFragment mConnectionFragment = ConnectionFragment.newInstance();
        public GamePadFragment mGamePadFragment = GamePadFragment.newInstance();

        private Pair<CharSequence,Fragment> mNamedFragments[];

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            mNamedFragments = new Pair[]
                    {
                            Pair.create("log",        mLogFragment ),
                            Pair.create("gamePad",    mGamePadFragment),
                            Pair.create("connection", mConnectionFragment)
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
