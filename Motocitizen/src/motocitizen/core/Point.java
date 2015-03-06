package motocitizen.core;

import java.util.HashMap;
import java.util.Map;

import motocitizen.startup.Startup;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class Point {
	public Map<String, String> common;
	public Map<String, String> details;
	public Location location;
	public String type;
	public int id;

	public Point() {
		this(new HashMap<String, String>());
	}

	public Point(Map<String, String> data) {
		common = new HashMap<String, String>();
		details = new HashMap<String, String>();
		// Log.d("KEY:", String.valueOf(data.size()));
		for (String key : data.keySet()) {
			common.put(key, data.get(key));
		}
		try {
			id = Integer.parseInt(common.get("id"));
		} catch (NumberFormatException e) {
			id = 0;
		}
		if (common.containsKey("lon") && common.containsKey("lat")) {
			
			location = new Location(LocationManager.NETWORK_PROVIDER);
			location.setLatitude(Float.parseFloat(common.get("lat")));
			location.setLongitude(Float.parseFloat(common.get("lon")));
			location.setAccuracy(10);
			//Log.d("POINT", location.toString());
		}
	}
	
	public String get(String key) {
		return common.get(key);
	}

	public void set(String key, String value) {
		if (common.containsKey(key)) {
			common.remove(key);
		}
		common.put(key, value);
	}

	public String getDet(String key) {
		return details.get(key);
	}

	public void setDet(String key, String value) {
		if (details.containsKey(key)) {
			details.remove(key);
		}
		details.put(key, value);
	}

	public int getId() {
		return id;
	}

	public String toString() {
		String result = "id:" + String.valueOf(id) + ",";
		for (String key : common.keySet()) {
			result += key + ":" + common.get(key) + ",";
		}
		return result.substring(0, result.length() - 1);
	}
}