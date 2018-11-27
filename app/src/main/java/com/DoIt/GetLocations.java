package com.DoIt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.datatype.BmobGeoPoint;

public class GetLocations {
    /**
     * 获取当前地理位置
     * @param activity 调用本方法的页面
     */
    public static Location getLocation(Activity activity) {
        LocationManager locationManager =
                (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (activity.checkCallingOrSelfPermission
                (Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null) {
                return locationManager.getLastKnownLocation(getProvider(locationManager,activity));
            } else {
                Toast.makeText(activity, "无法获取位置", Toast.LENGTH_SHORT).show();
                return null;
            }
        } else {
            Toast.makeText(activity, "应用没有获得权限，无法操作", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    /**
     * 获取可用的定位方式
     * @param manager 定位器
     * @param activity 调用本方法的页面
     */
    private static String getProvider(LocationManager manager,Activity activity){
        List<String> providerList = manager.getProviders(true);
        // 测试一般都在室内，这里颠倒了书上的判断顺序
        if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        } else if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.PASSIVE_PROVIDER)) {
            return LocationManager.PASSIVE_PROVIDER;
        } else {
            Toast.makeText(activity, "没有可用的定位方式", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    /**
     * 计算任务与用户之间的距离
     * @param point 任务的位置
     * @param location 用户的位置
     */
    public static Distance getDistance(BmobGeoPoint point, Location location) {
        double lat_a,lng_a,lat_b,lng_b;
        lat_a = point.getLatitude();
        lng_a = point.getLongitude();
        lat_b = location.getLatitude();
        lng_b = location.getLongitude();
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        double EARTH_RADIUS = 6378137.0;
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        Distance distance = new Distance();
        if (s >= 500) {
            distance.distance = Math.round(s/1000);
            distance.unit = "公里";
        } else {
            distance.distance = s;
            distance.unit = "米";
        }
        return distance;
    }
    /**
     * 通过地理逆编码得到具体位置信息
     * @param context 调用本方法的页面
     * @param la 纬度
     * @param lo 经度
     */
    public static Address getAddress(Context context,double la,double lo) {
        Geocoder geocoder = new Geocoder(context, Locale.CHINESE);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(la, lo, 1);
        } catch (IOException e) {
            Toast.makeText(context, "逆编码地理信息失败", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(context, "获取地址信息失败", Toast.LENGTH_SHORT).show();
            return null;
        } else return addresses.get(0);
    }

    public static class Distance{
        public String unit;//距离单位
        public double distance;//距离
    }
}
