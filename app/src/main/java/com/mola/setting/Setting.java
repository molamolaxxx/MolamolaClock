package com.mola.setting;

/**
 * Created by Administrator on 2018/6/9.
 */

public class Setting {
    public Setting(int timeType, Boolean isAutoClearAlarm, int timeMode) {
        this.timeType = timeType;
        this.isAutoClearAlarm = isAutoClearAlarm;
        this.timeMode=timeMode;
    }
    private int timeType;//时区
    private int timeMode;
    private Boolean isAutoClearAlarm;

    public int getTimeType() {
        return timeType;
    }

    public void setTimeType(int timeType) {
        this.timeType = timeType;
    }

    public int getTimeMode() {
        return timeMode;
    }

    public void setTimeMode(int timeMode) {
        this.timeMode = timeMode;
    }

    public Boolean getAutoClearAlarm() {
        return isAutoClearAlarm;
    }

    public void setAutoClearAlarm(Boolean autoClearAlarm) {
        isAutoClearAlarm = autoClearAlarm;
    }

}
