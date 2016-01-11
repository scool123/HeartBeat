package edu.fx0735wayne.heartbeat;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

//test github
//    static Thread recordThread;
//    static Thread recordThread;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private static final String LOG_TAG = "AudioRecordTest";
    //语音文件保存路径
    static String FileName = null;
    //    static double[] x = new double[50];
//    static TextView result;
    private static boolean isprinting;
    private static final int COUNT = 50;
    public static Data data;
    public static double[] res = new double[COUNT];


    /*//语音操作对象
    static private MediaPlayer mPlayer = null;
    static private MediaRecorder mRecorder = null;*/


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

        //创建data实例，开启显示线程
        data = new Data();
        Print p = new Print();
        p.start();

    }

    //实时输出分贝值
    static class MyAudioRecorder  implements View.OnClickListener {
        static final String TAG = "AudioRecord";
        static final int SAMPLE_RATE_IN_HZ = 8000;

        static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord mAudioRecord;
        boolean isGetVoiceRun;
        Object mLock;

        public MyAudioRecorder() {
            mLock = new Object();
            //result = (TextView)rootView.findViewById(R.id.resultTest);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "进入onClick方法");
            if (isGetVoiceRun) {
                Log.e(TAG, "还在录着呢");
                return;
            }
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
            if (mAudioRecord == null) {
                Log.e("sound", "mAudioRecord初始化失败");
            }
            isGetVoiceRun = true;

            /*recordThread = */new Thread(new Runnable() {
                @Override
                public void run() {
                    mAudioRecord.startRecording();
                    short[] buffer = new short[BUFFER_SIZE];
                    int j =0;
                    while (isGetVoiceRun) {
                        //r是实际读取的数据长度，一般而言r会小于buffersize
                        int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                        long v = 0;
                        // 将 buffer 内容取出，进行平方和运算
                        for (int i = 0; i < buffer.length; i++) {
                            v += buffer[i] * buffer[i];
                        }
                        // 平方和除以数据总长度，得到音量大小。
                        double mean = v / (double) r;
                        double volume = 10 * Math.log10(mean);
                        Log.d(TAG, "分贝值:" + volume);
                        res[j] = volume;
                        j++;
                        if(j>=COUNT){
                            synchronized( data ){
                                System.arraycopy(res,0,data.x,0,COUNT);
                                j = 0;
                                data.notify();
                            }
                        }
                        // 大概一秒五十次
                        synchronized (mLock) {
                            try {
                                mLock.wait(1000/COUNT);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    mAudioRecord.stop();
                    mAudioRecord.release();
                    mAudioRecord = null;
                }
            }).start();
        }
    }

    //实时展示数据线程类
    static public class Print extends Thread{
        @Override
        public void run(){
            int i = 0;
            isprinting =true;
            while (isprinting){
                try{
                    synchronized (data){
                        data.wait();}
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                while(i<COUNT){
                    Log.i("测试-" , "分贝值:" + data.x[i]);
                    Log.d("测试-" , "分贝值:" + data.x[i]);
                    i++;
                }

            }
        }
    }

    class Data{
        double[] x;
        Data(){
            x = new double[COUNT];
        }
    }



/*    //开始录音
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

    }*/

    //停止录音
    static class stopRecordListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            /*mRecorder.stop();
            mRecorder.release();
            mRecorder = null;*/
        }

    }
    //播放录音
    static class startPlayListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            /*mPlayer = new MediaPlayer();
            try{
                mPlayer.setDataSource(FileName);
                mPlayer.prepare();
                mPlayer.start();
            }catch(IOException e){
                Log.e(LOG_TAG,"播放失败");
            }*/
        }

    }
    //停止播放录音
    static class stopPlayListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            /*mPlayer.release();
            mPlayer = null;*/
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


/*    @Override
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
    }*/

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
//            TextView result;
            Button startRecord;
            Button startPlay;
            Button stopRecord;
            Button stopPlay;
             /* 开始录音 */
            startRecord = (Button)rootView.findViewById(R.id.startRecord);
            startRecord.setText("Start Record");
            //绑定监听器
            startRecord.setOnClickListener(new MyAudioRecorder());

            //结束录音
            stopRecord = (Button)rootView.findViewById(R.id.stopRecord);
            stopRecord.setText("Stop Record");
            stopRecord.setOnClickListener(new stopRecordListener());
            //显示结果
//            result = (TextView)rootView.findViewById(R.id.resultTest);


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
