package motocitizen.geolocation;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import motocitizen.accident.Accident;
import motocitizen.activity.MainScreenActivity;
import motocitizen.content.Content;
import motocitizen.geocoder.MyGeoCoder;
import motocitizen.network.requests.InPlaceRequest;
import motocitizen.network.requests.LeaveRequest;
import motocitizen.utils.Preferences;

class NormalLocationManager implements SecuredLocationManagerInterface {
    /* constants */
    private static final int LOW_INTERVAL     = 60000;
    private static final int LOW_BEST         = 30000;
    private static final int LOW_DISPLACEMENT = 200;

    private static final int HIGH_INTERVAL     = 5000;
    private static final int HIGH_BEST         = 1000;
    private static final int HIGH_DISPLACEMENT = 10;

    private static final int DEFAULT_ACCURACY     = 1000;
    private static final int ARRIVED_MAX_ACCURACY = 300;
    /* end constants */

    private Location                            current;
    private GoogleApiClient                     googleApiClient;
    private LocationRequest                     locationRequest;
    private LocationListener                    locationListener;
    private GoogleApiClient.ConnectionCallbacks connectionCallback;

    NormalLocationManager() {
        setup();
        current = getLocation();
    }

    private void setup() {
        if (connectionCallback == null) connectionCallback = new MyConnectionCallback();
        if (locationListener == null) locationListener = location -> {
            current = location;
            Preferences.getInstance().saveLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            requestAddress();
            checkInPlace(location);
        };
        if (locationRequest == null)
            locationRequest = getProvider(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private LocationRequest getProvider(int accuracy) {
        int interval, bestInterval, displacement;
        switch (accuracy) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                interval = HIGH_INTERVAL;
                bestInterval = HIGH_BEST;
                displacement = HIGH_DISPLACEMENT;
                break;
            case LocationRequest.PRIORITY_LOW_POWER:
            default:
                interval = LOW_INTERVAL;
                bestInterval = LOW_BEST;
                displacement = LOW_DISPLACEMENT;
        }
        LocationRequest lr = new LocationRequest();
        lr.setInterval(interval);
        lr.setFastestInterval(bestInterval);
        lr.setSmallestDisplacement(displacement);
        lr.setPriority(accuracy);
        return lr;
    }

    public Location getDirtyLocation() {
        if (current != null && current.getTime() - (new Date()).getTime() < 30000) return current;
        return getLocation();
    }
    @SuppressWarnings({"MissingPermission"})
    public Location getLocation() {
        if (googleApiClient != null) {
            current = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
        if (current == null) {
            current = new Location(LocationManager.NETWORK_PROVIDER);
            LatLng latlng = Preferences.getInstance().getSavedLatLng();
            current.setLatitude(latlng.latitude);
            current.setLongitude(latlng.longitude);
            current.setAccuracy(DEFAULT_ACCURACY);
        }
        return current;
    }
    @SuppressWarnings({"MissingPermission"})
    private void runLocationService(Context context, int accuracy) {
        setup();
        locationRequest = getProvider(accuracy);
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(connectionCallback).addApi(LocationServices.API).build();
            googleApiClient.connect();
        }
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
        } else {
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        }
    }

    public void sleep(Context context) {
        runLocationService(context, LocationRequest.PRIORITY_LOW_POWER);
    }

    public void wakeup(Context context) {
        runLocationService(context, LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void requestAddress() {
        Location location = getLocation();
        if (current == location) return;
        MainScreenActivity.updateStatusBar(getAddress(location));
    }

    private void checkInPlace(Location location) {
        String login = Preferences.getInstance().getLogin();
        if (login.equals("")) return;
        int currentInplace = Content.getInstance().getInPlaceId();
        if (currentInplace != 0) {
            if (isInPlace(location, currentInplace)) return;
            Content.getInstance().setLeave(currentInplace);
            new LeaveRequest(currentInplace);
        }
        for (int accId : Content.getInstance().keySet()) {
            if (accId == currentInplace) continue;
            if (isArrived(location, accId)) {
                Content.getInstance().setInPlace(accId);
                new InPlaceRequest(accId);
            }
        }
    }

    private boolean isArrived(Location location, int accId) {
        return Content.getInstance().get(accId).getLocation().distanceTo(location) < Math.max(ARRIVED_MAX_ACCURACY, location.getAccuracy());
    }

    private boolean isInPlace(Location location, int accId) {
        Accident acc = Content.getInstance().get(accId);
        return acc != null && location != null && (acc.getLocation().distanceTo(location) - location.getAccuracy() * 2 - 1000 < 0);
    }

    public String getAddress(Location location) {
        //TODO Разобраться. Выглядит страшно.
        StringBuilder res = new StringBuilder();
        try {
            List<Address> list;
            list = MyGeoCoder.getInstance().getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (list == null || list.size() == 0)
                return location.getLatitude() + " " + location.getLongitude();

            Address address  = list.get(0);
            String  locality = address.getLocality();
            if (locality == null) locality = address.getAdminArea();
            if (locality == null && address.getMaxAddressLineIndex() > 0)
                locality = address.getAddressLine(0);

            String thoroughfare = null != address.getThoroughfare() ? address.getThoroughfare() : address.getSubAdminArea();

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
    @SuppressWarnings({"MissingPermission"})
    private class MyConnectionCallback implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle connectionHint) {
            //TODO Это пиздец
            while (!googleApiClient.isConnected()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
            current = getLocation();
        }

        @Override
        public void onConnectionSuspended(int arg0) {
        }
    }
}
