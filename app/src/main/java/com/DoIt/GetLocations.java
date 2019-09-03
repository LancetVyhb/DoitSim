package com.DoIt;

import com.tencent.map.geolocation.TencentLocation;

import cn.bmob.v3.datatype.BmobGeoPoint;

public class GetLocations {

    /**
     * 计算任务与用户之间的距离
     *
     * @param point    任务的位置
     * @param location 用户的位置
     */
    public static Distance getDistance(BmobGeoPoint point, TencentLocation location) {
        double lat_a, lng_a, lat_b, lng_b;
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
            distance.distance = Math.round(s / 1000);
            distance.unit = "公里";
        } else {
            distance.distance = s;
            distance.unit = "米";
        }
        return distance;
    }

    public static class Distance {
        public String unit;//距离单位
        public double distance;//距离
    }
}
