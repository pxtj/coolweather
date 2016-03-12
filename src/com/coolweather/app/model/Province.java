package com.coolweather.app.model;

public class Province {
	private int id;
	private String provinceName;
	private String provinceCode;
	
	public Province(){}
	public Province(int id, String name, String code){
		this.id = id;
		this.provinceName = name;
		this.provinceCode = code;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public String getProvinceCode() {
		return provinceCode;
	}
	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

}
