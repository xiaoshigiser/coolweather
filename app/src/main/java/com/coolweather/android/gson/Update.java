package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Update {
    //"loc"与updateTime建立映射关系
    @SerializedName("loc")
    public String updateTime;
}
