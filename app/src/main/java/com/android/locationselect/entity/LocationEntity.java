package com.android.locationselect.entity;

/**
 * @ProjectName: LocationSelect
 * @Package: com.android.locationselect.entity
 * @ClassName: LocationEntity
 * @Author: 1984629668@qq.com
 * @CreateDate: 2020/10/14 9:48
 */
public class LocationEntity {
    private String address;
    private String lat;
    private String lng;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
