package motocitizen.network.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class RegionsRequest extends HTTPClient {
    public RegionsRequest(AsyncTaskCompleteListener listener, Context context) {
        this.listener = listener;
        this.context = context;
        post = new HashMap<>();
        post.put("m", Methods.REGIONS.toCode());
        execute(post);
    }

    @Override
    public boolean error(JSONObject response) {
        return !response.has("result");
    }

    @Override
    public String getError(JSONObject response) {
        if (!response.has("result"))
            return "Ошибка соединения " + response.toString();
        try {
            String result = response.getString("result");
            switch (result) {
                case "OK":
                    return "Статус изменен";
                case "ERROR PREREQUISITES":
                    return "Неизвестная ошибка " + response.toString();
            }
        } catch (JSONException ignored) {

        }
        return "Неизвестная ошибка " + response.toString();
    }
}