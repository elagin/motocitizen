package motocitizen.network.requests;

import org.json.JSONException;
import org.json.JSONObject;

import motocitizen.content.Content;
import motocitizen.network.AsyncTaskCompleteListener;
import motocitizen.network.HTTPClient;
import motocitizen.network.Methods;
import motocitizen.user.User;
import motocitizen.utils.Preferences;

public class BanRequest extends HTTPClient {
    @SuppressWarnings("unchecked")
    public BanRequest(AsyncTaskCompleteListener listener, int id) {
        this.listener = listener;
        int user_id = Content.getInstance().get(id).getOwnerId();
        post.put("login", Preferences.getInstance().getLogin());
        post.put("passhash", User.getInstance().makePassHash());
        post.put("id", String.valueOf(id));
        post.put("user_id", String.valueOf(user_id));
        post.put("calledMethod", Methods.BAN.toCode());
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
            String result = response.getString("ban");
            switch (result) {
                case "OK":
                    return "Статус изменен";
                case "ERROR PREREQUISITES":
                    return "Неизвестная ошибка " + response.toString();
                case "NO USER":
                    return "Пользователь не зарегистрирован";
                case "AUTH ERROR":
                case "NO RIGHTS":
                    return "Недостаточно прав";
            }
        } catch (JSONException ignored) {}
        return "Неизвестная ошибка " + response.toString();
    }
}