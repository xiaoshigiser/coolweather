package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("city")
    public String cityName;

    //"id"与weatherId建立映射关系
    @SerializedName("id")
    public String weatherId;
}
