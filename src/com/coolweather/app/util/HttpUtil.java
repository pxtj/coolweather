package com.coolweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.ContentUris;
import android.util.Log;

public class HttpUtil {
	
	private static final String TAG = "HttpUtil";
	
	public static void sendHttpRequest(final String httpUrl, 
			final HttpCallbackListener listener){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					Log.d(TAG, "in run");
					Log.d(TAG, "httpurl: " + httpUrl);
					URL url = new URL(httpUrl);
					connection = (HttpURLConnection)url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					
					connection.connect();
					
					Log.d(TAG, "connection: " + connection.toString());
					InputStream in = connection.getInputStream();
					Log.d(TAG, "in: " + in.toString());
					BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
					Log.d(TAG, "reader: " + reader);
					
					StringBuilder response = new StringBuilder();
					String line;
					while((line = reader.readLine()) != null){
						response.append(line);
					}
					Log.d(TAG, "line: " + line);
					Log.d(TAG, "response: " + response);
					
					if(listener != null){
						listener.onFinish(response.toString());
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					if (listener != null) {
						listener.onError(e);
					}
					
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
				
			}
		}).start();
	}

}
