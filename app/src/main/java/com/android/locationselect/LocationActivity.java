package com.android.locationselect;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.android.locationselect.adapter.LocationItemAdapter;
import com.android.locationselect.entity.LocationEntity;
import com.android.locationselect.util.RecyclerUtils;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LocationActivity extends AppCompatActivity implements View.OnClickListener, LocationSource, PoiSearch.OnPoiSearchListener, XRecyclerView.LoadingListener, LocationItemAdapter.OnLocationItemClickListener, AMap.OnMapLoadedListener, AMap.OnMapClickListener, AMapLocationListener, AMap.OnMarkerDragListener, AMap.OnCameraChangeListener {
    private MapView mapView;
    private Button btnBack;
    private Button btnOk;
    private Button btnCancel;
    private EditText etSearch;
    private XRecyclerView recyclerView;
    private int requestCode;
    private LocationEntity locationEntity;
    private String jsonKey;
    private int page = 1;
    private boolean isFirst = true;
    private boolean isRefresh = true;
    private AMapLocation currentMapLocation;
    private AMapLocation tempMapLocation;
    private LocationItemAdapter locationItemAdapter;
    private boolean isClick = false;
    private String address;
    private AMap aMap;
    private AMapLocationClient mLocationClient;
    private OnLocationChangedListener mListener;

    private Marker locMarker;
    private Circle ac;
    private Circle c;
    private long start;
    private final Interpolator interpolator = new CycleInterpolator(1);
    private TimerTask mTimerTask;
    private Timer mTimer = new Timer();
    private double lat;
    private double lng;
    private String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE};
    private int permissionCode = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        ImmersionBar immersionBar = ImmersionBar.with(this);
        immersionBar.barColor(R.color.white);
        immersionBar.fitsSystemWindows(true);
        immersionBar.autoDarkModeEnable(true);
        immersionBar.keyboardEnable(true);
        immersionBar.init();
        requestPermission();
        initView();
        initData();
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        initMap();
        initSearch();
    }

    private void requestPermission() {
        for (String s : permissions) {
            if (ContextCompat.checkSelfPermission(this, s)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions,
                        permissionCode);
            }
        }
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
                if (s.length() > 0) {
                    isFirst = false;
                    isRefresh = true;
                    currentMapLocation = null;
                    getPoi(null);
                } else if (s.length() == 0) {
                    isFirst = true;
                    isRefresh = true;
                    getPoi(tempMapLocation);
                }
            }
        });
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        aMap.setOnMapLoadedListener(this);
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerDragListener(this);
        aMap.setOnCameraChangeListener(this);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
    }

    private Marker addMarker(LatLng point) {
        Bitmap bMap = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.location);
        BitmapDescriptor des = BitmapDescriptorFactory.fromBitmap(bMap);
        return aMap.addMarker(new MarkerOptions().position(point).icon(des)
                .anchor(0.5f, 0.5f));
    }

    private String getKey() {
        return etSearch.getText().toString();
    }

    private void getPoi(AMapLocation aMapLocation) {
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
        }
        poiSearch.searchPOIAsyn();
        poiSearch.setOnPoiSearchListener(this);
    }

    private void getPoiDrag(double lat, double lng) {
        PoiSearch.Query query;
        query = new PoiSearch.Query(getKey(), "", getKey());
        query.setPageSize(10);// 设置每页最多返回多少条poiItem
        query.setPageNum(page);//设置查询页码
        PoiSearch poiSearch = new PoiSearch(this, query);
        poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(lat,
                lng), 1000));//设置周边搜索的中心点以及半径
        poiSearch.searchPOIAsyn();
        poiSearch.setOnPoiSearchListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        jsonKey = intent.getStringExtra("jsonKey");
        requestCode = intent.getIntExtra("requestCode", 0);
    }

    public static void startActivity(Activity activity, int requestCode, String jsonKey) {
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
        mapView.onDestroy();
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        try {
            mTimer.cancel();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        deactivate();
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
                finish();
                break;
            case R.id.btn_cancel:
                etSearch.setText("");
                isFirst = true;
                isRefresh = true;
                getPoi(currentMapLocation);
                break;
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        startLocation();
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * 开始定位。
     */
    private void startLocation() {
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mLocationClient.setLocationListener(this);
            // 设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置为单次定位
            mLocationOption.setOnceLocation(true);
            // 设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.startLocation();
        } else {
            mLocationClient.startLocation();
        }
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
        recyclerView.setPullRefreshEnabled(false);
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
        if (isFirst) {
            getPoi(tempMapLocation);
        } else {
            getPoi(currentMapLocation);
        }
    }

    @Override
    public void clickItem(PoiItem poiItem) {
        locationEntity = poiToLocationItem(poiItem);
        LatLng myLocation = new LatLng(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude());
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));
        addLocationMarker(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude());
        isClick = true;
    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (mTimerTask != null) {
                mTimerTask.cancel();
                mTimerTask = null;
            }
            if (aMapLocation.getErrorCode() == 0) {
                LatLng myLocation = new LatLng(aMapLocation.getLatitude(),
                        aMapLocation.getLongitude());
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 19));
                addLocationMarker(aMapLocation);
                //获取周边位置
                tempMapLocation = aMapLocation;
                currentMapLocation = aMapLocation;
                getPoi(aMapLocation);
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": "
                        + aMapLocation.getErrorInfo();
                Toast.makeText(this, errText, Toast.LENGTH_SHORT).show();
                Log.e("AmapErr", errText);
            }
        }
    }

    private void addLocationMarker(AMapLocation aMapLocation) {
        lat = aMapLocation.getLatitude();
        lng = aMapLocation.getLongitude();
        address = aMapLocation.getAddress();
        LatLng myLocation = new LatLng(lat, lng);
        float accuracy = aMapLocation.getAccuracy();
        if (accuracy > 15) {
            accuracy = 15;
        }
        if (locMarker == null) {
            locMarker = addMarker(myLocation);
            ac = aMap.addCircle(new CircleOptions().center(myLocation)
                    .fillColor(Color.argb(100, 255, 218, 185)).radius(accuracy)
                    .strokeColor(Color.argb(255, 255, 228, 185)).strokeWidth(5));
            c = aMap.addCircle(new CircleOptions().center(myLocation)
                    .fillColor(Color.argb(70, 255, 218, 185))
                    .radius(accuracy).strokeColor(Color.argb(255, 255, 228, 185))
                    .strokeWidth(0));
        } else {
            locMarker.setPosition(myLocation);
            ac.setCenter(myLocation);
            ac.setRadius(accuracy);
            c.setCenter(myLocation);
            c.setRadius(accuracy);
        }
        scaleCircle(c);
    }

    private void addLocationMarker(double lat, double lng) {
        LatLng myLocation = new LatLng(lat, lng);
        float accuracy = 15;
        if (locMarker == null) {
            locMarker = addMarker(myLocation);
            ac = aMap.addCircle(new CircleOptions().center(myLocation)
                    .fillColor(Color.argb(100, 255, 218, 185)).radius(accuracy)
                    .strokeColor(Color.argb(255, 255, 228, 185)).strokeWidth(5));
            c = aMap.addCircle(new CircleOptions().center(myLocation)
                    .fillColor(Color.argb(70, 255, 218, 185))
                    .radius(accuracy).strokeColor(Color.argb(255, 255, 228, 185))
                    .strokeWidth(0));
        } else {
            locMarker.setPosition(myLocation);
            ac.setCenter(myLocation);
            ac.setRadius(accuracy);
            c.setCenter(myLocation);
            c.setRadius(accuracy);
        }
        scaleCircle(c);
    }

    public void scaleCircle(final Circle circle) {
        start = SystemClock.uptimeMillis();
        mTimerTask = new circleTask(circle, 1000);
        mTimer.schedule(mTimerTask, 0, 30);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        LatLng latLng = marker.getPosition();
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
        addLocationMarker(latLng.latitude, latLng.longitude);
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        isFirst = false;
        isRefresh = true;
        currentMapLocation = null;
        getPoiDrag(latitude, longitude);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // 销毁定位
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        LatLng latLng = cameraPosition.target;
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
        addLocationMarker(latLng.latitude, latLng.longitude);
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        isFirst = false;
        isRefresh = true;
        currentMapLocation = null;
        getPoiDrag(latitude, longitude);
    }

    private class circleTask extends TimerTask {
        private double r;
        private Circle circle;
        private long duration = 1000;

        circleTask(Circle circle, long rate) {
            this.circle = circle;
            this.r = circle.getRadius();
            if (rate > 0) {
                this.duration = rate;
            }
        }

        @Override
        public void run() {
            try {
                long elapsed = SystemClock.uptimeMillis() - start;
                float input = (float) elapsed / duration;
                //外圈循环缩放
                float t = interpolator.getInterpolation((float) (input - 0.25));
                double r1 = (t + 2) * r;
                //外圈放大后消失
                circle.setRadius(r1);
                if (input > 2) {
                    start = SystemClock.uptimeMillis();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //可在此继续其他操作。
        initMap();
    }
}
