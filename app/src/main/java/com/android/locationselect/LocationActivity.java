package com.android.locationselect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.android.locationselect.adapter.LocationItemAdapter;
import com.android.locationselect.entity.LocationEntity;
import com.android.locationselect.util.AndroidStatusBarUtils;
import com.android.locationselect.util.RecyclerUtils;
import com.google.gson.Gson;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;

public class LocationActivity extends AppCompatActivity implements View.OnClickListener, LocationSource, PoiSearch.OnPoiSearchListener, XRecyclerView.LoadingListener, LocationItemAdapter.OnLocationItemClickListener {
    private MapView mapView;
    private Button btnBack;
    private Button btnOk;
    private Button btnCancel;
    private EditText etSearch;
    private XRecyclerView recyclerView;
    private int requestCode;
    private LocationEntity locationEntity;
    private String jsonKey;
    private AMap aMap;
    private UiSettings uiSettings;
    private AMapLocationClient aMapLocationClient;
    private AMapLocationClientOption aMapLocationClientOption;
    private LocationSource.OnLocationChangedListener locationChangedListener;
    private int page = 1;
    private boolean isFirst = true;
    private boolean isRefresh = true;
    private AMapLocation mapLocation;
    private LocationItemAdapter locationItemAdapter;
    private boolean isClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        fullScreen(R.color.white);
        initView();
        initData();
        initMap();
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        initSearch();
    }

    private void initSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isRefresh = true;
                getPoi(mapLocation);
            }
        });
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        uiSettings = aMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        aMap.setMyLocationEnabled(true);

        aMapLocationClient = new AMapLocationClient(this);
        aMapLocationClientOption = new AMapLocationClientOption();
        aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        aMapLocationClientOption.setInterval(5000);
        aMapLocationClient.setLocationOption(aMapLocationClientOption);
        //显示定位标记
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.location);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
        //透明定位精度圆圈
        myLocationStyle.strokeWidth(0);
        myLocationStyle.strokeColor(Color.TRANSPARENT);
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        //定位间隔
        myLocationStyle.interval(5000);
        //连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        aMap.setMyLocationStyle(myLocationStyle);
        aMapLocationClient.startLocation();
        aMapLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                init(aMapLocation);
                if (locationChangedListener == null) {
                    return;
                }

                if (aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                    locationChangedListener.onLocationChanged(aMapLocation);
                }
            }
        });
    }

    private void init(AMapLocation aMapLocation) {
        //位置
        LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());

        //地图view中心点和缩放级别设置
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel() - 3));
        //添加marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        Bitmap companyIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.location);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(companyIcon));
        //设置marker锚点偏移量
        markerOptions.anchor(0.5f, 0.5f);
        aMap.addMarker(markerOptions);
        //添加圆形面范围标识
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.strokeWidth(0);
        circleOptions.fillColor(getResources().getColor(R.color.gray66));
        circleOptions.radius(70);
        aMap.addCircle(circleOptions);

        getPoi(aMapLocation);
    }

    private String getKey() {
        return etSearch.getText().toString();
    }

    private void getPoi(AMapLocation aMapLocation) {
        mapLocation = aMapLocation;
        PoiSearch.Query query;
        if (aMapLocation != null) {
            query = new PoiSearch.Query(getKey(), "", aMapLocation.getCityCode());
        } else {
            query = new PoiSearch.Query(getKey(), "", getKey());
        }
        query.setPageSize(10);// 设置每页最多返回多少条poiItem
        query.setPageNum(page);//设置查询页码
        PoiSearch poiSearch = new PoiSearch(this, query);
        if (isFirst && aMapLocation != null) {
            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(aMapLocation.getLatitude(),
                    aMapLocation.getLongitude()), 1000));//设置周边搜索的中心点以及半径
            isFirst = false;
        }
        poiSearch.searchPOIAsyn();
        poiSearch.setOnPoiSearchListener(this);
    }

    /**
     * 通过设置全屏，设置状态栏透明
     */
    protected void fullScreen(int ColorRes) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        if (Build.VERSION.SDK_INT <= 20) {
            //5.0.1以下
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup decorViewGroup = (ViewGroup) window.getDecorView();
            View statusBarView = new View(window.getContext());
            int statusBarHeight = AndroidStatusBarUtils.getStatusBarHeight(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, statusBarHeight);
            params.gravity = Gravity.TOP;
            statusBarView.setLayoutParams(params);
            statusBarView.setBackgroundColor(getResources().getColor(ColorRes));
            decorViewGroup.addView(statusBarView);
            ViewGroup mContentView = window.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
                mChildView.setFitsSystemWindows(true);
            }
        } else {
            Window window = getWindow();
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //设置状态栏颜色
            window.setStatusBarColor(getResources().getColor(ColorRes));
        }
    }

    private void initData() {
        Intent intent = getIntent();
        jsonKey = intent.getStringExtra("jsonKey");
        requestCode = intent.getIntExtra("requestCode", 0);
    }

    public void startActivity(Activity activity, int requestCode, String jsonKey) {
        Intent intent = new Intent(activity, LocationActivity.class);
        intent.putExtra("jsonKey", jsonKey);
        intent.putExtra("requestCode", requestCode);
        activity.startActivityForResult(intent, requestCode);
    }

    private void initView() {
        mapView = findViewById(R.id.mapView);
        btnBack = findViewById(R.id.btn_back);
        btnOk = findViewById(R.id.btn_ok);
        btnCancel = findViewById(R.id.btn_cancel);
        etSearch = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.recyclerView);
        btnBack.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        locationItemAdapter = new LocationItemAdapter(this);
        locationItemAdapter.setLocationItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_ok:
                Intent intent = new Intent();
                intent.putExtra(jsonKey, new Gson().toJson(locationEntity));
                setResult(requestCode, intent);
                Toast.makeText(this, new Gson().toJson(locationEntity), Toast.LENGTH_SHORT).show();
                Log.e("result", "onClick: " + new Gson().toJson(locationEntity));
                //finish();
                break;
            case R.id.btn_cancel:
                etSearch.setText("");
                isFirst = true;
                isRefresh = true;
                getPoi(mapLocation);
                break;
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        locationChangedListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        locationChangedListener = null;
        if (aMapLocationClient != null) {
            aMapLocationClient.stopLocation();
            aMapLocationClient.onDestroy();
        }
        aMapLocationClient = null;
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        ArrayList<PoiItem> pois = poiResult.getPois();
        if (!isClick) {
            if (pois != null && pois.size() > 0) {
                locationEntity = poiToLocationItem(pois.get(0));
            }
        }
        RecyclerUtils.setRecyclerViewData(isRefresh, pois, recyclerView, locationItemAdapter, new LinearLayoutManager(this), this);
    }

    private LocationEntity poiToLocationItem(PoiItem poiItem) {
        if (poiItem != null) {
            locationEntity = new LocationEntity();
            locationEntity.setAddress(poiItem.getProvinceName() + poiItem.getCityName() + poiItem.getTitle());
            LatLonPoint latLonPoint = poiItem.getLatLonPoint();
            locationEntity.setLat(String.valueOf(latLonPoint.getLatitude()));
            locationEntity.setLng(String.valueOf(latLonPoint.getLongitude()));
        }
        return locationEntity;
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        page = page + 1;
        getPoi(mapLocation);
    }

    @Override
    public void clickItem(PoiItem poiItem) {
        isClick = true;
        locationEntity = poiToLocationItem(poiItem);
    }
}
