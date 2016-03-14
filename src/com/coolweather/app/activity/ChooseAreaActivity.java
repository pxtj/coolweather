package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	
	private static final String TAG = "chooseAreaActivity";
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private static final String HTTPURL_BASE = "https://api.heweather.com/x3/";
	private static final String APIKEY = "715527c84d72473d9c3eaa6dbad19757";
	private static final String HTTPURL_HOT_CITIESLIST = HTTPURL_BASE + "citylist?search=hotworld&key=" + APIKEY;
	private static final String HTTPURL_ALLCHINA_CITIESLIST = HTTPURL_BASE + "citylist?search=allchina&key=" + APIKEY;
	private static final String HTTPURL_ALLWORLD_CITIESLIST = HTTPURL_BASE + "citylist?search=allworld&key=" + APIKEY;
	private static final String HTTPURL_CITY_WEATHER = HTTPURL_BASE + "weather?cityid=";
	private static final String HTTPURL_CITY_ATTRACTIONs = HTTPURL_BASE + "attractions?cityid=";
	
	private static final String HTTPURL = "http://www.weather.com.cn/data/list3/city";
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	
	private int currentLevel;
	
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		if (pref.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.choose_area);
		titleText = (TextView)findViewById(R.id.title_text);
		listView = (ListView)findViewById(R.id.list_view);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		queryProvinces();	//load province data;
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					Log.d(TAG, "selectedProvince: " + selectedProvince.toString());
					queryCities();
				}else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(position).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		
	}
	
	/**
	 * 查询全国所有的省份，优先从数据库查询，如果没有查询到再去服务器查询
	 */
	public void queryProvinces(){
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province p : provinceList) {
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else {
			queryFromServer(null, "Province");
		}
	}

	/**
	 * 查询选中的省份中的所有市，优先从数据库查询，如果没有查询到再去服务器查询
	 */
	public void queryCities(){
		Log.d(TAG, "in queryCities.");
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City c : cityList) {
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else {
			Log.d(TAG, "query from server");
			queryFromServer(selectedProvince.getProvinceCode(), "City");
		}
		
	}
	
	
	/**
	 * 查询选中的市的所有的县，优先从数据库查询，如果没有查询到再去服务器查询
	 */
	public void queryCounties(){
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County c : countyList) {
				dataList.add(c.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else {
			queryFromServer(selectedCity.getCityCode(), "County");
		}
	}
	
	/**
	 * 根据传入的代号和类型从服务器上获取省市县的数据
	 * @param type 城市列表：citylist；城市天气：weather；景点天气：attractions
	 * @param code 城市ID:id code，城市列表类型:allchina、hotworld、allworld；
	 */
	private void queryFromServer(final String code, final String type){
		String httpUrl;
		if (!TextUtils.isEmpty(code)) {
			httpUrl = HTTPURL + code + ".xml";
		} else {
			httpUrl = HTTPURL + ".xml";
		}
		/*
		if (type.equals("")) {
			httpUrl = HTTPURL_BASE + type + "?search=" + code + "&key=" + APIKEY;
		} else {
			httpUrl = HTTPURL_BASE + type + "?cityid=" + code + "&key=" + APIKEY;
		}
		*/
		//httpUrl = HTTPURL_BASE + "weather"+ "?cityid=" + "CN101010300" + "&key=" + APIKEY;

		showProgressDialog();
		
		HttpUtil.sendHttpRequest(httpUrl, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if (type.equals("Province")) {
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				} else if (type.equals("City")) {
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				} else if (type.equals("County")) {
					result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				
				if (result) {
					//通过runOnUiThread()方法回到主线程处理逻辑；
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							if (type.equals("Province")) {
								queryProvinces();
							} else if (type.equals("City")) {
								queryCities();
							} else if (type.equals("County")) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				//通过runOnUiThread()方法回到主线程处理逻辑；
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_LONG).show();
					}
				});
			}

		});
	}
	
	private void showProgressDialog(){
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载，请稍候...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	private void closeProgressDialog(){
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

}

