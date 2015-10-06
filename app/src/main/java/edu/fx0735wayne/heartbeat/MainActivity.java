package edu.fx0735wayne.heartbeat;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private static final String LOG_TAG = "AudioRecordTest";
    //语音文件保存路径
    static String FileName = null;



    //语音操作对象
    static private MediaPlayer mPlayer = null;
    static private MediaRecorder mRecorder = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


    }

    //开始录音
    static class startRecordListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(FileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
            mRecorder.start();
        }

    }

    //停止录音
    static class stopRecordListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

    }
    //播放录音
    static class startPlayListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            mPlayer = new MediaPlayer();
            try{
                mPlayer.setDataSource(FileName);
                mPlayer.prepare();
                mPlayer.start();
            }catch(IOException e){
                Log.e(LOG_TAG,"播放失败");
            }
        }

    }
    //停止播放录音
    static class stopPlayListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            mPlayer.release();
            mPlayer = null;
        }

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            //界面控件
            Button startRecord;
            Button startPlay;
            Button stopRecord;
            Button stopPlay;
             /* 开始录音 */
            startRecord = (Button)rootView.findViewById(R.id.startRecord);
            startRecord.setText("Start Record");
            //绑定监听器
            startRecord.setOnClickListener(new startRecordListener());

            //结束录音
            stopRecord = (Button)rootView.findViewById(R.id.stopRecord);
            stopRecord.setText("Stop Record");
            stopRecord.setOnClickListener(new stopRecordListener());

            //开始播放
            startPlay = (Button)rootView.findViewById(R.id.startPlay);
            startPlay.setText("Start Play");
            //绑定监听器
            startPlay.setOnClickListener(new startPlayListener());

            //结束播放
            stopPlay = (Button)rootView.findViewById(R.id.stopPlay);
            stopPlay.setText("Stop Play");
            stopPlay.setOnClickListener(new stopPlayListener());

            //设置sdcard的路径
            FileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            FileName += "/audiorecordtest.3gp";

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
