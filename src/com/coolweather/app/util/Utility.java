package com.coolweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class Utility {
	/**
	 * �����ʹ�����������ص�ʡ������
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
			String response){
		
		if(!TextUtils.isEmpty(response)){
			String[] provinces = response.split(",");
			if (provinces != null && provinces.length > 0) {
				for (String p : provinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceName(array[1]);
					province.setProvinceCode(array[0]);
					
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * �����ʹ�����������ص��м�����
	 */
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, 
			String response, int provinceId){
		
		if(!TextUtils.isEmpty(response)){
			String[] cities = response.split(",");
			if (cities != null && cities.length > 0) {
				for (String c : cities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityName(array[1]);
					city.setCityCode(array[0]);
					city.setProvinceId(provinceId);
					
					coolWeatherDB.saveCity(city);
					
				}
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * �����ʹ�����������ص��ؼ�����
	 */
	public synchronized static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, 
			String response, int cityId){
		if (!TextUtils.isEmpty(response)) {
			String[] counties = response.split(",");
			if (counties != null && counties.length > 0) {
				for (String c : counties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyName(array[1]);
					county.setCountyCode(array[0]);
					county.setCityId(cityId);
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * �����������Żص�JSON���ݣ��������ݴ洢�����أ�
	 */
	public static void handleWeatherResponse(Context context, String response){
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp2");
			String temp2 = weatherInfo.getString("temp1");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, 
					weatherDesp, publishTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �����������ص������������ݴ洢������sharePreferences�ļ��С�
	 */
	public static void saveWeatherInfo(Context context, String cityName, String weatherCode,
			String temp1, String temp2, String weatherDesp, String publishTime){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��", Locale.CHINA);
		
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();

		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}

	
}


















