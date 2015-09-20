package motocitizen.network.requests;

import android.content.Context;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import motocitizen.MyApp;
import motocitizen.geolocation.MyLocationManager;
import motocitizen.startup.Preferences;

@SuppressWarnings("unchecked")
public class AccidentsRequest extends HTTPClient {
    private boolean silent;

    public AccidentsRequest(Context context, AsyncTaskCompleteListener listener, boolean silent) {
        this.silent = silent;
        this.context = context;
        this.listener = listener;
        post = new HashMap<>();
        Location location = MyLocationManager.getLocation();
        myApp = (MyApp) context.getApplicationContext();
        String      user  = Preferences.getLogin();
        if (!user.equals("")) {
            post.put("user", user);
        }
        post.put("distance", String.valueOf(Preferences.getVisibleDistance()));
        post.put("lat", String.valueOf(location.getLatitude()));
        post.put("lon", String.valueOf(location.getLongitude()));
        //post.put("hint", context.getString(R.string.request_get_incidents));
        /*
        post.put("calledMethod", Methods.GET_LIST.toCode());
        */
        post.put("m", Methods.GET_LIST.toCode());
        execute(post);
    }

    public AccidentsRequest(Context context, AsyncTaskCompleteListener listener) {
        new AccidentsRequest(context, listener, false);
    }

    @Override
    public boolean error(JSONObject response) {
        if (silent) return false;
        if (!response.has("list")) return true;
        try {
            JSONObject error = response.getJSONArray("list").getJSONObject(0);
            if (error.has("error")) return true;
        } catch (JSONException e) {
            return true;
        }
        return false;
    }

    @Override
    public String getError(JSONObject response) {
        if (!response.has("list")) return "Ошибка соединения " + response.toString();
        try {
            JSONObject json = response.getJSONArray("list").getJSONObject(0);
            if (json.has("error")) {
                String error = json.getString("error");
                if (error.equals("no_new")) {
                    return "Нет новых сообщений";
                }
            } else {
                return "Список обновлен";
            }
        } catch (JSONException ignored) {

        }
        return "Неизвестная ошибка " + response.toString();
    }
}
