package motocitizen.app.general;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import motocitizen.Activity.AccidentDetailsActivity;
import motocitizen.MyApp;
import motocitizen.app.general.gcm.GCMRegistration;
import motocitizen.app.general.user.Auth;
import motocitizen.main.R;
import motocitizen.startup.Startup;
import motocitizen.utils.Const;

public class AccidentsGeneral {
    private MyApp myApp = null;
    private static int       inplaceAcc;
    private static Accident  currentPoint;
    public static  Accidents points;
    public static  Auth      auth;
    private static Integer[] sorted;

    public static int getInplaceID() {
        return inplaceAcc;
    }

    public static void setInPlace(int id) {
        inplaceAcc = id;
        for (int key : points.keySet()) {
            if (points.getPoint(key).isInPlace()) {
                points.getPoint(key).setLeave(auth.getID());
            }
        }
        points.getPoint(id).setInPlace(auth.getID());
    }

    public static void setLeave(int id) {
        Accident acc = points.getPoint(id);
        // Вероятно попадается инцидент которого уже нет в списке.
        if (acc != null && acc.isInPlace()) {
            points.getPoint(id).setLeave(auth.getID());
        }
    }

    public static int getCurrentPointID() {
        return currentPoint.getId();
    }

    public static void setCurrentPoint(Accident point) {
        currentPoint = point;
    }

    public AccidentsGeneral(Context context) {
        myApp = (MyApp) context.getApplicationContext();
        inplaceAcc = 0;
        auth = myApp.getMCAuth();
        new MyLocationManager(context);
        points = new Accidents(context);
        new GCMRegistration(context);
    }

    private static Accident getCurrent() {
        makeSortedList();
        if (currentPoint != null) {
            return currentPoint;
        } else if (points.keySet().size() != 0) {
            return points.getPoint(sorted[0]);
        } else {
            return null;
        }
    }

    private static void makeSortedList() {
        List<Integer> list = new ArrayList<>();
        list.addAll(points.keySet());
        sorted = new Integer[list.size()];
        list.toArray(sorted);
        Arrays.sort(sorted, Collections.reverseOrder());
    }

    private static void inflateYesterdayRow(Context context, ViewGroup view) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view.addView(li.inflate(R.layout.yesterday_row, view, false));
    }

    private static FrameLayout noAccidentsNotification(Context context) {
        FrameLayout fl = new FrameLayout(context);
        TextView    tv = new TextView(context);
        tv.setText("Нет событий");
        tv.setTextColor(Color.RED);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.GRAY);
        fl.setBackgroundColor(Color.GRAY);
        fl.addView(tv, Const.lp);
        return fl;
    }

    private static void drawList(Context context) {
        ViewGroup view = (ViewGroup) ((Activity) context).findViewById(R.id.accListContent);
        view.removeAllViews();
        //AccidentTypes.refresh();
        boolean noYesterday = true;
        if (points.error.equals("ok") || points.error.equals("no_new")) {
            makeSortedList();
            for (Integer aSorted : sorted) {
                Accident acc = points.getPoint(aSorted);
                if (acc.isVisible()) {
                    if (!acc.isToday() && noYesterday) {
                        inflateYesterdayRow(context, view);
                        noYesterday = false;
                    }
                    FrameLayout tr = acc.createAccRow(context);
                    view.addView(tr);
                } else {
                    acc.row_id = 0;
                }
            }
            if (sorted.length == 0) {
                view.addView(noAccidentsNotification(context));
            } else if (currentPoint.getId() == 0) {
                points.setSelected(context, currentPoint.getId());
            } else {
                points.setSelected(context, currentPoint.getId());
            }
        } else {
            // TODO Сюда вкрячить сообщение об ошибке
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static TableRow getDelimiterRow(Context context, String text) {
        TableRow tr = new TableRow(context);
        TextView tw = new TextView(tr.getContext());
        tr.setLayoutParams(Const.trlp);
        tw.setTextColor(Color.BLACK);
        tw.setBackgroundColor(Color.LTGRAY);
        tw.setGravity(Gravity.CENTER);
        tw.setText(text);
        tw.setLayoutParams(Const.trlp);
        tr.addView(tw);
        return tr;
    }

    public static void refresh(Context context) {
        points.load();
        Startup.map.placeAcc(context);
        redraw(context);
        Startup.map.placeAcc(context);
    }

    public static void refreshPoints(Context context, JSONObject data) {
        if (data != null) {
            try {
                JSONArray arr = data.getJSONArray("list");
                points.update(arr);
                Startup.map.placeAcc(context);
                redraw(context);
                Startup.map.placeAcc(context);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void redraw(Context context) {
        currentPoint = getCurrent();
        if (currentPoint == null) {
            return;
        }
        drawList(context);
    }

    public static void toDetails(Context context) {
        toDetails(context, currentPoint.getId());
    }

    public static void toDetails(Context context, int id) {
        currentPoint = points.getPoint(id);
        if (currentPoint != null) {
            points.setSelected(context, id);
            Intent intent = new Intent(context, AccidentDetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("accidentID", currentPoint.getId());
            intent.putExtras(bundle);
            context.startActivity(intent);

        } else {
            Toast.makeText(context, context.getString(R.string.cant_open_incident), Toast.LENGTH_LONG).show();
        }
    }
}
