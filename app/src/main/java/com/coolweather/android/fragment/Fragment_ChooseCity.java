package com.coolweather.android.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.coolweather.android.MainActivity;
import com.coolweather.android.R;
import com.coolweather.android.WeatherActivity;
import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.DataUtil;
import com.coolweather.android.util.HttpUtil;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Fragment_ChooseCity extends Fragment {
    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText1;
    private TextView titleText2;
    private ListView listView;
    private ArrayAdapter adapter;
    private List dataList = new ArrayList<>();
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<County> countyList;
    /**
     * 选中的省份
     */
    private Province selectedProvince;
    /**
     * 选中的城市
     */
    private City selectedCity;
    /**
     * 选择页面的等级
     */
    private int currentLevel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_city, container, false);
        titleText1 = (TextView) view.findViewById(R.id.back);
        titleText2 = (TextView) view.findViewById(R.id.cityname);
        listView = (ListView) view.findViewById(R.id.citylist);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    //onActivityCreate被弃用，用onAttach监听生命周期来替代，不要忘了用完之后要及时remove
    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        //requireActivity() 返回的是宿主activity
        requireActivity().getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull @NotNull LifecycleOwner source, @NonNull @NotNull Lifecycle.Event event) {
                if (event.getTargetState() == Lifecycle.State.CREATED) {   //Activity执行onCreate时执行
                    //对列表设置监听事件
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (currentLevel == LEVEL_PROVINCE) {
                                selectedProvince = provinceList.get(position);
                                queryCities();
                            } else if (currentLevel == LEVEL_CITY) {
                                selectedCity = cityList.get(position);
                                queryCounties();
                            }else if(currentLevel == LEVEL_COUNTY){
                                String weatherId = countyList.get(position).getWeatherId();
                                if(getActivity() instanceof MainActivity){
                                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                                    intent.putExtra("weather_id", weatherId);
                                    startActivity(intent);
                                    getActivity().finish();    //把这个活动杀死
                                }else if (getActivity() instanceof  WeatherActivity){
                                    WeatherActivity activity = (WeatherActivity) getActivity();

                                    activity.drawerLayout.closeDrawers();
                                    activity.swipeRefresh.setRefreshing(true);
                                    activity.requestWeather(weatherId);
                                    activity.getIntent().putExtra("weather_id", weatherId);
                                }
                            }
                        }
                    });
                    titleText1.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (currentLevel == LEVEL_COUNTY) {
                                queryCities();
                            } else if (currentLevel == LEVEL_CITY) {
                                queryProvinces();
                            }
                        }
                    });
                    queryProvinces();
                    getLifecycle().removeObserver(this);  //这里是删除观察者
                }
            }
        });
    }

    private void queryProvinces() {
        titleText2.setText("中国");
        titleText1.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private void queryCounties() {
        titleText2.setText(selectedCity.getCityName());
        titleText1.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            String address = "http://guolin.tech/api/china/"+ selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
            queryFromServer(address,"county");
        }
    }

    private void queryCities() {
        titleText2.setText(selectedProvince.getProvinceName());
        titleText1.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
                adapter.notifyDataSetChanged();
                listView.setSelection(0);
                currentLevel = LEVEL_CITY;
            }
            else{
                String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
                queryFromServer(address,"city");
            }
    }

    private void queryFromServer(String address, String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if(type.equals("province")){
                    result = DataUtil.handleProvince(responseText);
                }else if(type.equals("city")){
                    result = DataUtil.handleCity(responseText,selectedProvince.getId());
                }else if(type.equals("county")){
                    result = DataUtil.handleCounty(responseText,selectedCity.getId());
                }
                if(result){
                    //去主线程改变UI
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if(type.equals("province")){
                                queryProvinces();
                            }else if(type.equals("city")){
                                queryCities();
                            }else if(type.equals("county")){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void closeProgressDialog() {
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载…");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
