package com.mola.molamolaclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mola.Alarm.AlarmSetting;
import com.mola.ListViewEmptyListener;
import com.mola.database.DAOManager;
import com.mola.utils.MyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class AlarmFragment extends Fragment {
    public AlarmFragment() {
        // Required empty public constructor
    }

    private static final String TAG = "AlarmFragment";
    private ImageView Add=null;
    private ListView listView;
    private DAOManager daoManager;
    private ArrayList<AlarmSetting> listItem;
    private LinearLayout emptyHint;
    private MyAdapter mSimpleAdapter;
    private int mPosition;
    private boolean isFragmentBroadcastReg=false;
    MyUtil myUtil;
    Animation animation;
    //listView位置
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.alarm_fragment, container, false);
        Add=(ImageView)view.findViewById(R.id.Add);
        listView=(ListView)view.findViewById(R.id.lv);
        emptyHint=(LinearLayout)view.findViewById(R.id.empty_hint);

        initAdd();       //设置添加闹钟监听器
        refreshListView();  //初始化刷新listView
        //initReceiveAdapter();
        onOperateListView();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        daoManager=new DAOManager(getContext());
        myUtil=new MyUtil();
        openDatabase();
    }

    public void onOperateListView() {
        if(listItem.size()==0){
            emptyHint.setVisibility(View.VISIBLE);
        }
        else{
            emptyHint.setVisibility(View.INVISIBLE);
        }
    }
    private void initAdd(){
        animation=AnimationUtils.loadAnimation(getContext(),R.anim.click_rotate);
        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "new alarm");
                addButtonAnim();
                Timer timer=new Timer();
                TimerTask timerTask=new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent=new Intent("android.intent.CreateAlarm");
                        startActivity(intent);
                    }
                };
                timer.schedule(timerTask,350);
            }
        });
    }
    public void refreshListView(){
        ArrayList<HashMap<String,Object>> arrayList=new ArrayList<>();
        //每一个记录的显示信息放在一个hashMap里
        for (int i=0;i<listItem.size();i++)
        {
            HashMap<String,Object> map=new HashMap<>();
            map.put("time",getStringHour(listItem.get(i))+":"+getStringMin(listItem.get(i)));
            map.put("isClockOpen","Alarm");
            map.put("clockSwitch",listItem.get(i).isClockOpen());
            map.put("repeat",getItemRepeat(listItem.get(i).getRepeat()));
            arrayList.add(map);
        }
        //显示listView
        mSimpleAdapter=new MyAdapter(getContext(),arrayList,R.layout.simple_list_item
                ,new String[]{"time","isClockOpen","repeat","clockSwitch"},new int[]{R.id.itemTime,R.id.isItemClockOpen,R.id.repeatItem,R.id.clockSwitch});
        listView.setAdapter(mSimpleAdapter);//绑定适配器
        /*
        记录listView的滑块位置
         */
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 不滚动时保存当前滚动到的位置
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mPosition=listView.getFirstVisiblePosition();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        listView.setSelection(mPosition);
        /*
        短按跳转到编辑界面
         **/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(),"the"+listItem.get(position).getId(),Toast.LENGTH_SHORT).show();
                Intent intent=new Intent("android.intent.CreateAlarm");
                intent.putExtra("isEdit",true);
                intent.putExtra("id",listItem.get(position).getId());
                startActivity(intent);
            }
        });
        /*
        长按控制闹钟开关
         **/
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
           @Override
           public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
               return true;
            }
        });
       daoManager.closeDatabase();
       }
    private String getItemRepeat(int repeat){
        switch (repeat) {
            case 0: {
                return "once";   //更新ui
            }
            case 1: {
                return "everyday";
            }
            case 2: {
                return "weekday";
            }
            case 30: {
                return "Monday";   //更新ui
            }
            case 31: {
                return "Tuesday";
            }
            case 32: {
                return "Wednesday";
            }
            case 33: {
                return "Thursday";   //更新ui
            }
            case 34: {
                return "Friday";
            }
            case 35: {
                return "Saturday";
            }
            case 36: {
                return "Sunday";   //更新ui
            }
        }
        return "once";
    }
    public String getStringHour(AlarmSetting alarmSetting){
        if(alarmSetting.getHours()>=10)
            return Integer.toString(alarmSetting.getHours());
        else
            return "0"+Integer.toString(alarmSetting.getHours());
    }
    public String getStringMin(AlarmSetting alarmSetting){
        if(alarmSetting.getMinus()>=10)
            return Integer.toString(alarmSetting.getMinus());
        else
            return "0"+Integer.toString(alarmSetting.getMinus());
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    public void openDatabase(){
        daoManager.getReadableDatabase();
        //从数据库获取相应信息,
        listItem=myUtil.sort(daoManager.findAllData());
    }
    public void addButtonAnim(){
        Add.setAnimation(animation);
        Add.startAnimation(animation);
    }
    //接受Adapater的广播
    public void initReceiveAdapter(){
        if (!isFragmentBroadcastReg)
        {
            IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction("refreshFromAdapter");
            BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle data= intent.getExtras();
                    //TODO
                    Log.d("clock", "onReceiveFromAdapter");
                    openDatabase();
                    refreshListView();
                }
            };
            isFragmentBroadcastReg=true;
            getContext().registerReceiver(broadcastReceiver,intentFilter);
        }
    }
}
