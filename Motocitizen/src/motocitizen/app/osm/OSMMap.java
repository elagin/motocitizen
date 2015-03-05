package motocitizen.app.osm;

import java.util.ArrayList;

import motocitizen.app.mc.MCLocation;
import motocitizen.main.R;
import motocitizen.startup.Startup;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnTouchListener;

public class OSMMap {
	public ArrayList<OverlayItem> overlayItemArray;
	public static MapView map;
	private Activity act;
	final JumpToLocation jumpToLocation;
	private static ItemizedIconOverlay<OverlayItem> userOverlay, accOverlay;

	public OSMMap() {
		userOverlay = OSMUserOverlay.getUserOverlay();
		accOverlay = OSMAccOverlay.getOverlay();
		act = (Activity) Startup.context;
		map = (MapView) act.findViewById(R.id.osm_mapview);
		map.setTileSource(TileSourceFactory.MAPNIK);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		map.getController().setZoom(16);

		ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(act);
		jumpToLocation = new JumpToLocation(act);

		map.getOverlays().add(myScaleBarOverlay);
		map.getOverlays().add(jumpToLocation);
		goToUser();
	}

	public static void placeAcc() {
		map.getOverlays().remove(accOverlay);
		accOverlay = OSMAccOverlay.getOverlay();
		map.getOverlays().add(accOverlay);
		map.invalidate();
	}

	public static void placeUser() {
		map.getOverlays().remove(userOverlay);
		userOverlay = OSMUserOverlay.getUserOverlay();
		map.getOverlays().add(userOverlay);
		map.invalidate();
	}

	public static void goToUser() {
		GeoPoint gp = new GeoPoint(MCLocation.current);
		map.getController().animateTo(gp);
		map.invalidate();
	}

	public static void zoom(int zoom){
		map.getController().setZoom(zoom);
	}
	
	public static void jumpToPoint(Location location){
		GeoPoint gp = new GeoPoint(location);
		map.getController().animateTo(gp);
	}
	
	private static class JumpToLocation extends Overlay {
		public float cx, cy, r;
		private int lastZoom;

		public JumpToLocation(Activity act) {
			super(act);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event, MapView map) {
			int[] location = new int[2];
			float x = event.getRawX();
			map.getLocationInWindow(location);
			lastZoom = -1;
			float y = event.getRawY() - location[1];
			if (x > (cx - r) && x < (cx + r) && y > (cy - r) && y < (cy + r)) {
				goToUser();
			}
			return true;
		}

		@Override
		protected void draw(Canvas canvas, MapView arg1, boolean shadow) {
			if (map.isAnimating() || shadow) {
				return;
			}

			if (map.getZoomLevel() != lastZoom) {
				lastZoom = map.getZoomLevel();
				return;
			}

			float w = map.getWidth();
			float h = map.getHeight();

			r = (float) (Math.min(w, h) * 0.07);
			cx = (float) (w - r * 1.2);
			cy = (float) (h - r * 1.2);

			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth((float) (r * 0.1));
			canvas.drawCircle(cx, cy, (float) (r * 0.7), paint);
			canvas.drawLine(cx, (float) (cy + r), cx, (float) (cy - r), paint);
			canvas.drawLine((float) (cx - r), cy, (float) (cx + r), cy, paint);
		}
	}

}
