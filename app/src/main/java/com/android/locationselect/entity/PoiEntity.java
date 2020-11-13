package com.android.locationselect.entity;

/**
 * @ProjectName: LocationSelect
 * @Package: com.android.locationselect.entity
 * @ClassName: PoiEntity
 * @Author: 1984629668@qq.com
 * @CreateDate: 2020/11/13 10:26
 */
public class PoiEntity {
    private String id;
    private String code;
    private String bigType;
    private String middleType;
    private String smallType;
    private String midCategory;
    private String subCategory;

    @Override
    public String toString() {
        return "PoiEntity{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", bigType='" + bigType + '\'' +
                ", middleType='" + middleType + '\'' +
                ", smallType='" + smallType + '\'' +
                ", midCategory='" + midCategory + '\'' +
                ", subCategory='" + subCategory + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBigType() {
        return bigType;
    }

    public void setBigType(String bigType) {
        this.bigType = bigType;
    }

    public String getMiddleType() {
        return middleType;
    }

    public void setMiddleType(String middleType) {
        this.middleType = middleType;
    }

    public String getSmallType() {
        return smallType;
    }

    public void setSmallType(String smallType) {
        this.smallType = smallType;
    }

    public String getMidCategory() {
        return midCategory;
    }

    public void setMidCategory(String midCategory) {
        this.midCategory = midCategory;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }
}
