package com.samsoft.treaviso.background.data;

import java.util.Date;
import java.lang.Math;

import android.os.SystemClock;
import org.json.JSONObject;
import org.json.JSONException;

public class Alarm {
	private String latitude;
	private String longitude;
	private int metros;
	private int active;
	private String path;
	private String name;
	
	private Long id;

    public Alarm()
    {
        id = (long) 0;
    }
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getMetros() {
		return metros;
	}
	public void setMetros(int metros) {
		this.metros = metros;
	}
	public int getActive() {
		return active;
	}
	public void setActive(int active) {
		this.active = active;
	}
	public JSONObject getJSONObject() {
		try {
			JSONObject j = new JSONObject();
			j.put("id", this.id);		
			j.put("latitude", this.latitude);
			j.put("longitude", this.longitude);
			j.put("metros", this.metros);
			j.put("path", this.path);
			j.put("active", this.active); 
			j.put("name", this.name);
			return j;
		} catch(JSONException ex) {
			ex.printStackTrace();
		}
		return null;		
	}	
}
