package motocitizen.content;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import motocitizen.MyApp;
import motocitizen.accident.Accident;
import motocitizen.database.Favorites;
import motocitizen.network.AsyncTaskCompleteListener;
import motocitizen.utils.SortedHashMap;

public class Content extends SortedHashMap<Accident> {
    private int           inPlace;
    public  List<Integer> favorites;

    {
        inPlace = 0;
    }

    public Content() {
        favorites = Favorites.getFavorites();
    }

    public int getInplaceId() {
        return inPlace;
    }

    public void setInPlace(int id) {
        if (inPlace != 0) {
            //TODO setLeave
        }
        inPlace = id;
    }

//    public void requestUpdate(Context context) {
//        MyIntentService.startActionGetAccidents(context, true);
//    }

    private FrameLayout noAccidentsNotification() {
        FrameLayout fl = new FrameLayout(MyApp.getCurrentActivity());
        TextView    tv = new TextView(MyApp.getCurrentActivity());
        tv.setText("Нет событий");
        tv.setTextColor(Color.RED);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(Color.GRAY);
        fl.setBackgroundColor(Color.GRAY);
        fl.addView(tv, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return fl;
    }

//    public void requestUpdate() {
//        new AccidentsRequest(new AccidentsRequestCallback(), true);
//    }

    public void parseJSON(JSONObject json) {
        if (!json.has("list")) return;
        try {
            JSONArray list = json.getJSONArray("list");
            Log.d("START PARSE POINTS", String.valueOf((new Date()).getTime()));
            for (int i = 0; i < list.length(); i++) {
                Accident accident = new Accident(list.getJSONObject(i));
                if (accident.isError()) continue;
                if (containsKey(accident.getId())) {
                    get(accident.getId()).update(list.getJSONObject(i));
                } else {
                    put(accident.getId(), accident);
                }
            }
            Log.d("END PARSE POINTS", String.valueOf((new Date()).getTime()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setLeave(int currentInplace) {
        //TODO SetLeave
    }

    private class AccidentsRequestCallback implements AsyncTaskCompleteListener {

        public void onTaskComplete(JSONObject result) {
            if (!result.has("error")) parseJSON(result);
            //((MyActivity) MyApp.getCurrentActivity()).redraw();
            //MyApp.getMap().placeAccidents();
        }
    }
}
