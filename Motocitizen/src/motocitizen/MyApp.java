package motocitizen;

import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import motocitizen.app.general.user.Auth;
import motocitizen.content.Region;
import motocitizen.database.Regions;
import motocitizen.startup.Preferences;

public class MyApp extends Application {

    private static MyApp instance;
    private Auth     auth     = null;
    private Geocoder geocoder = null;
    private List<Region> regions;

    public MyApp() {
        instance = this;
        regions = new ArrayList<>();
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    public Preferences getPreferences() {
        return new Preferences(instance.getApplicationContext());
    }

    public Auth getMCAuth() {
        if (auth == null) auth = new Auth(instance);
        return auth;
    }

    public String getAddress(Location location) {
        if (geocoder == null) {
            geocoder = new Geocoder(this);
        }
        StringBuilder res = new StringBuilder();
        try {
            List<Address> list;
            list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (list == null || list.size() == 0)
                return location.getLatitude() + " " + location.getLongitude();
            Address address = list.get(0);
            String locality = address.getLocality();
            if (locality == null) locality = address.getAdminArea();
            if (locality == null && address.getMaxAddressLineIndex() > 0)
                locality = address.getAddressLine(0);

            String thoroughfare = address.getThoroughfare();
            if (thoroughfare == null) thoroughfare = address.getSubAdminArea();

            String featureName = address.getFeatureName();

            if (locality != null) res.append(locality);
            if (thoroughfare != null) {
                if (res.length() > 0) res.append(" ");
                res.append(thoroughfare);
            }
            if (featureName != null) if (res.length() > 0) res.append(" ");
            res.append(featureName);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return res.toString();
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(JSONObject json) {
        regions.clear();
        try {
            JSONArray items = json.getJSONArray("result");
            for(int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                Region region = new Region();
                region.id = item.getString("id");
                region.name = item.getString("region");
                region.lat = item.getDouble("lat");
                region.lon = item.getDouble("lon");
                regions.add(region);
            }
            Regions.setRegions(getAppContext(), regions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Region getRegion(String id) {
        for(int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            if(region.id.equals(id))
                return region;
        }
        return null;
    }
}
