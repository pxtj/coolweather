package com.coolweather.app.model;

public class City {
	private int id;
	private String cityName;
	private String CityCode;
	private int provinceId;
	
	public City(){}
	public City(int id, String name, String code, int provinceId){
		this.id = id;
		this.cityName = name;
		this.CityCode = code;
		this.provinceId = provinceId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getCityCode() {
		return CityCode;
	}
	public void setCityCode(String cityCode) {
		CityCode = cityCode;
	}
	public int getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}

}
