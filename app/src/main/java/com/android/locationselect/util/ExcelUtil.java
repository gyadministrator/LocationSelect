package com.android.locationselect.util;

import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;

import com.android.locationselect.entity.PoiEntity;

import java.util.ArrayList;

import jxl.Sheet;
import jxl.Workbook;

/**
 * @ProjectName: LocationSelect
 * @Package: com.android.locationselect.util
 * @ClassName: ExcelUtil
 * @Author: 1984629668@qq.com
 * @CreateDate: 2020/11/13 10:24
 */
public class ExcelUtil {
    private static final String TAG = "ExcelUtil";

    /**
     * 获取 excel 表格中的数据,不能在主线程中调用
     *
     * @param xlsName excel 表格的名称
     * @param index   第几张表格中的数据
     */
    public static ArrayList<PoiEntity> getXlsData(Activity activity, String xlsName, int index) {
        ArrayList<PoiEntity> list = new ArrayList<>();
        AssetManager assetManager = activity.getAssets();

        try {
            Workbook workbook = Workbook.getWorkbook(assetManager.open(xlsName));
            Sheet sheet = workbook.getSheet(index);

            int sheetNum = workbook.getNumberOfSheets();
            int sheetRows = sheet.getRows();
            int sheetColumns = sheet.getColumns();

            Log.d(TAG, "the num of sheets is " + sheetNum);
            Log.d(TAG, "the name of sheet is  " + sheet.getName());
            Log.d(TAG, "total rows is 行=" + sheetRows);
            Log.d(TAG, "total cols is 列=" + sheetColumns);

            for (int i = 0; i < sheetRows; i++) {
                PoiEntity poiEntity = new PoiEntity();
                poiEntity.setId(sheet.getCell(0, i).getContents());
                poiEntity.setCode(sheet.getCell(1, i).getContents());
                poiEntity.setBigType(sheet.getCell(2, i).getContents());
                poiEntity.setMiddleType(sheet.getCell(3, i).getContents());
                poiEntity.setSmallType(sheet.getCell(4, i).getContents());
                poiEntity.setMidCategory(sheet.getCell(5, i).getContents());
                poiEntity.setSubCategory(sheet.getCell(6, i).getContents());

                list.add(poiEntity);
            }

            workbook.close();

        } catch (Exception e) {
            Log.e(TAG, "read error=" + e, e);
        }

        return list;
    }
}
