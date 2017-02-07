package motocitizen.network.requests;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import motocitizen.network.AsyncTaskCompleteListener;
import motocitizen.network.HTTPClient;
import motocitizen.network.Methods;
import motocitizen.user.User;
import motocitizen.utils.Preferences;

public class AccidentChangeStateRequest extends HTTPClient {

    public AccidentChangeStateRequest(AsyncTaskCompleteListener listener, int id, String state) {
        this.listener = listener;
        post.put("login", Preferences.getInstance().getLogin());
        post.put("passhash", User.getInstance().makePassHash());
        post.put("state", state);
        post.put("id", String.valueOf(id));
        post.put("m", Methods.CHANGE_STATE.toCode());
        execute(post);
    }

    @Override
    public boolean error(JSONObject response) {
        try {
            if (response.getString("result").equals("OK")) return false;
        } catch (JSONException | NullPointerException ignored) {}
        return true;
    }

    @Override
    public String getError(JSONObject response) {
        if (!response.has("result")) return "Ошибка соединения " + response.toString();
        try {
            String result = response.getString("result");
            switch (result) {
                case "OK":
                    return "Статус изменен успешно";
                case "ERROR":
                    return "Вы не авторизированы";
                case "NO RIGHTS":
                case "READONLY":
                    return "Недостаточно прав";
            }
        } catch (JSONException ignored) {

        }
        return "Неизвестная ошибка " + response.toString();
    }
}
