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
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import pl.dawidurbanski.tcpgamepad.ADdrone.Message;

public class Tabedctivity extends AppCompatActivity {

    //Fragments
    LogsFragment mLogFragment = LogsFragment.newInstance("logs");
    ConnectionFragment mConnectionFragment = ConnectionFragment.newInstance();
    GamePadFragment mGamePadFragment = GamePadFragment.newInstance();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

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
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mConnectionFragment.onLog = new ConnectionFragment.OnEvent() {
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
            if(mConnectionFragment.isConnected()) {
                mConnectionFragment.disconnect();
            }else{
                mConnectionFragment.connect();
            }
            }
        });
        //--
    }

    private void Log2List(String str) {
        mLogFragment.Log2List(str);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        GamepadInput.GamepadKey key = mGamePadFragment.onKey(keyCode,event,true);
        if(key!=null)
        {
            Log2List("key down: " + key.toString());
            mConnectionFragment.sendMessage("buttonDown:" + key.toString() + "\n");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        GamepadInput.GamepadKey key = mGamePadFragment.onKey(keyCode,event,false);
        if(key!=null)
        {
            Log2List("key up:" + key.toString());
            mConnectionFragment.sendMessage("buttonUp:" + key.toString()+"\n");
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        GamepadInput.GamepadAxis axis = mGamePadFragment.onGenericMotionEvent(event);
        if(axis!=null)
        {
            Log2List("Move : " + axis.toString());
            //mConnectionFragment.sendMessage("axis:" + axis.toString() + "\n");

            //String message = Message.generate(axis.dpadControleStickX, axis.dpadControleStickY, axis.rightControleStickY).toString()
            String message = Message.generateString(axis.leftControleStickX, axis.leftControleStickY, axis.rightControleStickY);
            Log2List("Send: '"+message+"'");
            mConnectionFragment.sendMessage(message);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Pair<String,Fragment> mNamedFragments[];

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            mNamedFragments = new Pair[]
                    {       //TODO: use strings.xml
                            Pair.create("logs",      mLogFragment ),
                            Pair.create("gamepad",   mGamePadFragment),
                            Pair.create("connection",mConnectionFragment)
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
