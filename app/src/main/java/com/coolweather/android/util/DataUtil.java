package com.coolweather.android.util;

import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Bing_pic;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 处理返回Json数据的工具类
 */
public class DataUtil {
    /**
     * 处理省级的返回数据
     * @param result 返回的省的Json数据
     */
    public static boolean handleProvince(String result){
        if(!TextUtils.isEmpty(result)){
            try {
                JSONArray allprovince = new JSONArray(result);
                for (int i = 0; i < allprovince.length(); i++) {
                    JSONObject jsonObject = allprovince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.setProvinceName(jsonObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理市级的返回数据
     * @param result 返回的市级的Json数据
     * @param provinceId 市所在的省的ID
     */
    public static boolean handleCity(String result,int provinceId){
        if(!TextUtils.isEmpty(result)){
            try {
                JSONArray allcity = new JSONArray(result);
                for (int i = 0; i < allcity.length(); i++) {
                    JSONObject jsonObject = allcity.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setCityName(jsonObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理县级的返回数据
     * @param result 返回的县级的Json数据
     * @param cityId 县所在市的ID
     */
    public static boolean handleCounty(String result,int cityId){
        if(!TextUtils.isEmpty(result)){
            try {
                JSONArray allcounty = new JSONArray(result);
                for (int i = 0; i < allcounty.length(); i++) {
                    JSONObject jsonObject = allcounty.getJSONObject(i);
                    County county = new County();
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCountyName(jsonObject.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 处理返回的天气数据
     * @param response 返回的天起json数据
     */
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public static Bing_pic handleBing_picResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("images");
            String Bing = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(Bing,Bing_pic.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
