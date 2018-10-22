package com.mola.Alarm;

/**
 * Created by Administrator on 2018/2/27.
 */

public class AlarmSetting    {
    private int id;
    private int Repeat;//1~3
    private int Sound;//1~5
    private int minus;
    private int hours;
    private boolean isShake;
    private boolean isClockOpen;
    //精确时间
    private int month;
    private int day;
    private int weekday;
    private String AlarmText;
    public AlarmSetting(){
        setRepeat(0);
        setSound(0);
        setShake(true);
        setAlarmText("alarm");
        setClockOpen(true);
    }

    public int getRepeat() {
        return Repeat;
    }

    public void setRepeat(int repeat) {
        Repeat = repeat;
    }

    public int getSound() {
        return Sound;
    }

    public void setSound(int sound) {
        Sound = sound;
    }

    public boolean isShake() {
        return isShake;
    }

    public void setShake(boolean shake) {
        isShake = shake;
    }

    public String getAlarmText() {
        return AlarmText;
    }

    public void setAlarmText(String alarmText) {
        AlarmText = alarmText;
    }

    public int getMinus() {
        return minus;
    }

    public void setMinus(int minus) {
        if (minus>=59)
            this.minus = 59;
        else if(minus<=0)
            this.minus=0;
        else
            this.minus=minus;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        if (hours>=23)
            this.hours = 23;
        else if(hours<=0)
            this.hours=0;
        else
            this.hours=hours;
    }

    public boolean isClockOpen() {
        return isClockOpen;
    }

    public void setClockOpen(boolean clockOpen) {
        isClockOpen = clockOpen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }
}
