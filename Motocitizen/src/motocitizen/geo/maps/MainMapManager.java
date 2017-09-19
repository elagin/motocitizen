package motocitizen.geo.maps;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import motocitizen.content.Content;
import motocitizen.content.accident.Accident;
import motocitizen.dictionary.Medicine;
import motocitizen.dictionary.Type;
import motocitizen.geo.geolocation.MyLocationManager;
import motocitizen.main.R;
import motocitizen.router.Router;
import motocitizen.utils.DateUtils;
import motocitizen.utils.LocationUtils;

public class MainMapManager {
    private static final int DEFAULT_ZOOM = 16;

    private GoogleMap map;
    private Marker    user;
    private String    selected;
    private Map<String, Integer> accidents = new HashMap<>();

    private List<DelayedAction> delayedAction = new ArrayList<>();

    private class OnMapCreated implements OnMapReadyCallback {
        private final Context context;

        OnMapCreated(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            init(context);
            placeAccidents(context);
            for (DelayedAction current : delayedAction) {
                current.makeAction();
            }
            delayedAction.clear();
        }
    }

    public MainMapManager(FragmentActivity activity) {

        jumpToPoint(LocationUtils.toLatLng(MyLocationManager.getLocation()));
        selected = "";
        android.support.v4.app.FragmentManager     fragmentManager     = activity.getSupportFragmentManager();
        final SupportMapFragment                   mapFragment         = new SupportMapFragment();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.google_map, mapFragment, "MAP").commit();

        mapFragment.getMapAsync(new OnMapCreated(activity));
    }

    public void placeAccidents(Context context) {
        if (map == null) return;
        map.clear();
        if (user != null) user.remove();
        Location location = MyLocationManager.getLocation();
        user = map.addMarker(new MarkerOptions().position(LocationUtils.toLatLng(location)).title(Type.USER.getText()).icon(Type.USER.getIcon()));

        for (Accident accident : Content.INSTANCE.getVisible()) {
            String title = accident.getType().getText();
            title += accident.getMedicine() != Medicine.UNKNOWN ? ", " + accident.getMedicine().getText() : "";
            title += ", " + DateUtils.getIntervalFromNowInText(context, accident.getTime()) + " назад";

            int age = (int) (((new Date()).getTime() - accident.getTime().getTime()) / 3600000);

            float alpha = age < 2 ? 1.0f : age < 6 ? 0.5f : 0.2f;

            Marker marker = map.addMarker(new MarkerOptions().position(accident.getCoordinates()).title(title).icon(accident.getType().getIcon()).alpha(alpha));
            accidents.put(marker.getId(), accident.getId());
        }
    }

    public void animateToPoint(final Location location) {
        if (map == null) delayedAction.add(() -> animateToPoint(location));
        else
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LocationUtils.toLatLng(location), DEFAULT_ZOOM));
    }

    public void jumpToPoint(final LatLng latLng) {
        if (map == null) delayedAction.add(() -> jumpToPoint(latLng));
        else
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    private void init(final Context context) {
        map.clear();
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(marker -> {
            String id = marker.getId();
            if (selected.equals(id) && accidents.containsKey(id)) {
                toDetails(context, accidents.get(selected));
            } else {
                marker.showInfoWindow();
                selected = id;
            }
            return true;
        });
        map.setOnMapLongClickListener(latLng -> Router.INSTANCE.toExternalMap((Activity) context, latLng));
    }

    @SuppressWarnings({ "MissingPermission" })
    public void enableLocation() {
        if (map == null) delayedAction.add(() -> map.setMyLocationEnabled(true));
        else map.setMyLocationEnabled(true);
    }

    private void toDetails(Context context, int id) {
        Bundle bundle = new Bundle();
        bundle.putInt("accidentID", id);
        Router.INSTANCE.goTo((Activity) context, Router.Target.DETAILS, bundle);
    }

    public void zoom(int zoom) {
        if (map == null) return;
        map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    @FunctionalInterface
    interface DelayedAction {
        void makeAction();
    }
}