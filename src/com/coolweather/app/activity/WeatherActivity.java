package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishTimeText;
	private TextView currentDateText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private Button switchCityBtn;
	private Button refreshWeatherBtn;
	
	private TextView toText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.weather_layout);
		
		weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
		cityNameText = (TextView)findViewById(R.id.city_name);
		publishTimeText = (TextView)findViewById(R.id.publish_time_text);
		currentDateText = (TextView)findViewById(R.id.current_date);
		weatherDespText = (TextView)findViewById(R.id.weather_desp);
		temp1Text = (TextView)findViewById(R.id.temp1);
		temp2Text = (TextView)findViewById(R.id.temp2);
		toText = (TextView)findViewById(R.id.to_text);

		switchCityBtn = (Button)findViewById(R.id.switch_city);
		refreshWeatherBtn = (Button)findViewById(R.id.refresh_weather);
		
		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			//如果有县级代号就去查询对应的天气
			publishTimeText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.VISIBLE);
			cityNameText.setVisibility(View.VISIBLE);
			queryWeatherCode(countyCode);
		} else {
			//没有县级代号就直接显示本地天气；
			showWeather();
		}

		switchCityBtn.setOnClickListener(this);
		refreshWeatherBtn.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishTimeText.setText("同步中...");
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = pref.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
				break;
		}
	}

	private void queryWeatherCode(String countyCode){
		String httpUrl = "http://www.weather.com.cn/data/list3/city" +
				countyCode + ".xml";
		queryFromServer(httpUrl, "countyCode");
	}
	
	private void queryWeatherInfo(String weatherCode){
		String httpUrl = "http://www.weather.com.cn/data/cityinfo/" +
				weatherCode + ".html";
		queryFromServer(httpUrl, "weatherCode");
	}
	
	private void queryFromServer(final String httpUrl, final String type){
		HttpUtil.sendHttpRequest(httpUrl, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if (type.equals("countyCode")) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null & array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if (type.equals("weatherCode")) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						publishTimeText.setText("加载失败！");
					}
				});
				
			}
		});
	}
	
	
	
	public void showWeather(){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		cityNameText.setText(pref.getString("city_name", ""));
		publishTimeText.setText(pref.getString("publish_time", ""));
		currentDateText.setText(pref.getString("current_date", ""));
		weatherDespText.setText(pref.getString("weather_desp", ""));
		temp1Text.setText(pref.getString("temp1", ""));
		temp2Text.setText(pref.getString("temp2", ""));
		toText.setText("~");
		
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
	
	
	
	
	
	
	
	
	
}
