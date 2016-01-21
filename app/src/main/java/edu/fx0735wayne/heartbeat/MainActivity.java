package edu.fx0735wayne.heartbeat;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

//test github
    static AudioRecord mAudioRecord;
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
    /*
     * 2016/1/14
     */
    //显示控件
    static TextView result;
    static Handler handler;

    public static LineChart chart;
    public static LineDataSet setHeart1;
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
    static boolean isGetVoiceRun;
    //实时输出分贝值
    static class MyAudioRecorder  implements View.OnClickListener {
        static final String TAG = "AudioRecord";
        static final int SAMPLE_RATE_IN_HZ = 8000;

        static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);

        //boolean isGetVoiceRun;
        Object mLock;

        public MyAudioRecorder() {
            mLock = new Object();
            //result = (TextView)rootView.findViewById(R.id.resultTest);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "进入onClick方法");
            if (isGetVoiceRun) {
                Toast.makeText(v.getContext(), "still recording",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
            if (mAudioRecord == null) {
                Log.e("sound", "mAudioRecord初始化失败");
            }
            isGetVoiceRun = true;
            isprinting=true;

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
                        //Log.d(TAG, "thread分贝值:" + volume);
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
                    //mAudioRecord.stop();
                    //mAudioRecord.release();
                    //mAudioRecord = null;
                }
            }).start();
        }
    }

    //实时展示数据线程类
    static public class Print extends Thread{
        @Override
        public void run(){
            System.out.println("enter into print run");
            int i = 0;
            int j = 0;
            int[] beat={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//1s内有50个时间间隔,beat用来记录时间间隔内是否有心跳,有心跳值为1,没有为0
            int[] beatVola={0,0,0,0,0,0,0,0,0,0};
            int beatVol=220;//阀值
            int hartRate;
            isprinting =true;
            while (isprinting){
                try{
                    synchronized (data){
                        data.wait();}
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                while(i<COUNT-1){
                    //Log.i("测试-", "分贝值:" + data.x[i]);
                    //Log.d("print测试-" , "分贝值:" + data.x[i]);
                    //if(data.x[i]>beatVol) {//大于阀值时对应间隔的beat值为1
                        beat[i] = (int)data.x[i+1]+(int)data.x[i];
                        //Log.d("print测试-" , "is beat:" + beat[i]);
                    //}
                    //Log.d("print测试-" , "is beat:" + beat[i]);
                    i++;
                }
                //间隔0.1秒即5个样本一个间隔判断其中数值与阈值的大小,大于阈值此0.1秒内有一个心跳,小于阈值此0.2s内没有心跳
                int count=0;
                int gap;
                int[] hartrates={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//记录心率值
                int c=0;//计数有几次心率求出
                while(j<50){
                    //Log.d("print测试---" ,"No."+j+ "is beat:" + beat[j]);
                    if(beat[j]>=85){
                        if(count==0){
                            gap=j;
                        }
                        gap=j-count;
                        count=j;
                        //Log.d("测试-", "No."+j+"心率count:" + count);
                        if(gap>0){
                            hartRate=1000/gap;
                            //Log.d("测试-", "No."+j+"心率:" + hartRate);
                            //循环算出1s内的心率,存放在heartrates数组内
                            hartrates[c++]=hartRate;
                            //Log.d("测试-" , "分贝值:" + data.x[i]);
                        }
                    }
                    beat[j]=0;
                    j++;
                }
                /*计算一秒内心率平均值*/
                hartRate=0;//1s内的平均心率
                int t=0;//数有几个心率可用
                c--;
                for(;c>=1;c--)//第一个心率一般不准不加入计算
                {
                    if(hartrates[c]<260 && hartrates[c]>80)
                    {
                        hartRate+=hartrates[c];
                        t++;
                    }
                }
                if(t>0)hartRate=hartRate/t;//平均值
                else System.out.println("同步部分失败");
                String msg= String.valueOf(hartRate);
                Bundle bd=new Bundle();//创建Bundle对象
                bd.putString("msg", msg);//向Bundle添加数据
                Message message=new Message();//创建Message对象
                message.setData(bd);//向Message中添加数据
                message.what=0;
                handler.sendMessage(message);//调用主控制类中的Handler对象发送消息
                i = 0;
                j = 0;
                beat=new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//1s内有50个时间间隔,beat用来记录时间间隔内是否有心跳,有心跳值为1,没有为0
                beatVola=new int[]{0,0,0,0,0,0,0,0,0,0};
            }
            //result
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
            // TODO Auto-generated method stu
            if(isGetVoiceRun==false)
            {
                Toast.makeText(v.getContext(), "already stop record",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            isprinting=false;
            isGetVoiceRun=false;
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }

    }
    //reset
    static class resetChartListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if(isGetVoiceRun==true)
            {
                Toast.makeText(v.getContext(), "Please stop record first",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(isGetVoiceRun==false)
            {
                setHeart1.clear();
                chart.invalidate();
            }

        }

    }
    //保存图片
    static class saveChartListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            if(isGetVoiceRun==true)
            {
                Toast.makeText(v.getContext(), "Please stop record first",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            SimpleDateFormat sDateFormat    =   new    SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String    date    =    sDateFormat.format(Calendar.getInstance().getTime());
            chart.saveToGallery(date,100);
            Toast.makeText(v.getContext(), "Chart has been save as:"+date+".jpeg",
                    Toast.LENGTH_SHORT).show();
            //System.out.println("save image:"+date);
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
            /*
         *2016/1/14 add TextView
         */
        result = (TextView)rootView.findViewById(R.id.resultView);
        result.setText("On starting...");
        /*handler= new Handler(){
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 0:
                    Bundle b = msg.getData();
                    String str = b.getString("msg");//获取Bundle对象
                    Log.d("textView测试-" , "心率:" + str);
                    result.setText(str);
                        break;
                }
            }
        };*/
            Button startRecord;
            Button startPlay;
            Button stopRecord;
            Button stopPlay;
            // a LineChart is initialized from xml
            chart = (LineChart)rootView.findViewById(R.id.chart);
            chart.invalidate();// Calling this method on the chart will redraw (refresh) it.
            chart.setLogEnabled(false);
            chart.setBackgroundColor(Color.parseColor("#fffff0"));
            chart.setDrawGridBackground(true);
            chart.setGridBackgroundColor(Color.parseColor("#fffff0"));
            chart.setDrawGridBackground(true);
            chart.setTouchEnabled(true);
            chart.setDragEnabled(true);
            chart.setScaleXEnabled(true);
            chart.setDragDecelerationEnabled(true);
            chart.setDragDecelerationFrictionCoef(0.2f);
            chart.setAutoScaleMinMaxEnabled(false);
            chart.setDescription("HeartRate/min");
            //axis-y
            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setEnabled(true);
            leftAxis.setDrawGridLines(true);
            leftAxis.setTextColor(Color.parseColor("#00ccff"));
            leftAxis.setGridColor(Color.parseColor("#ff9900"));
            leftAxis.setAxisLineColor(Color.parseColor("#ffff00"));
            leftAxis.setAxisLineWidth(4f);
            leftAxis.setGridLineWidth(0.5f);
            leftAxis.setTextSize(10f);
            leftAxis.setLabelCount(22, false);
            leftAxis.setStartAtZero(false);
            leftAxis.setAxisMaxValue(240f);
            leftAxis.setAxisMinValue(0f);
            //axis-x
            XAxis xAxis = chart.getXAxis();
            xAxis.setEnabled(true);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelsToSkip(9);
            xAxis.setTextSize(10f);
            xAxis.setAxisLineWidth(4f);
            xAxis.setTextColor(Color.parseColor("#00ccff"));
            xAxis.setDrawAxisLine(true);
            xAxis.setDrawGridLines(true);
            xAxis.setGridLineWidth(0.5f);
            xAxis.setGridColor(Color.parseColor("#ff9900"));
            xAxis.setAxisLineColor(Color.parseColor("#ffff00"));
            //data
            Entry hr1 = new Entry(1, 0);
            final ArrayList<Entry> heartrate1 = new ArrayList<Entry>();
            setHeart1 = new LineDataSet(heartrate1, "heart rate");
            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(setHeart1);
            ArrayList<String> xVals = new ArrayList<String>();
            for(int i = 0;i<421;i++)
            {
                Integer integer = new Integer(i);
                xVals.add(integer.toString());
            }
            final LineData data = new LineData(xVals, dataSets);
            //setHeart1=data.getDataSetByIndex(0);
            chart.setData(data);
            chart.invalidate(); // refresh

            //drawing heartrate line
            handler= new Handler(){
                public void handleMessage(Message msg) {
                    int drawsecound=0;
                    switch(msg.what){
                        case 0:
                            Bundle b = msg.getData();
                            String str = b.getString("msg");//获取Bundle对象
                            //Log.d("chart测试-", "心率:" + str);
                            Entry hr2 = new Entry((float)Integer.valueOf(str).intValue(),drawsecound++);
                            data.addEntry(new Entry((float) Integer.valueOf(str).intValue(), setHeart1.getEntryCount()*3), 0);
                            chart.notifyDataSetChanged();
                            chart.invalidate();
                            //result.setText(str);
                            break;
                    }
                }
            };



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
            startPlay.setText("Reset Chart");
            //绑定监听器
            startPlay.setOnClickListener(new resetChartListener());

            //结束播放
            stopPlay = (Button)rootView.findViewById(R.id.stopPlay);
            stopPlay.setText("Save as Image in Gallery");
            stopPlay.setOnClickListener(new saveChartListener());

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
