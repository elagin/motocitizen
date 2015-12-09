package motocitizen.network;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import motocitizen.MyIntentService;

/**
 * Created by elagin on 03.12.15.
 */
public class RequestErrors {
    final static public String VALID_RESULT   = "RESULT";
    final static public String INVALID_RESULT = "ERROR";

    final static public String VALID_VK_RESULT = "response";


    final static public String PREREQUISITES   = "PREREQUISITES";
    final static public String NO_USER         = "NO USER";
    final static public String ALREADY_IN_ROLE = "ALREADY IN ROLE";
    final static public String NO_RIGHTS       = "NO RIGHTS";
    final static public String UNKNOWN_ERROR   = "UNKNOWN ERROR";
    final static public String TIMEOUT         = "TIMEOUT";

    public static String getError(JSONObject response) {
        if (response.has(VALID_RESULT)) return "OK";
        if (!response.has(INVALID_RESULT)) return "Ошибка соединения " + response.toString();
        try {
            JSONObject errorJson = response.getJSONObject(INVALID_RESULT);
            String text = errorJson.getString("text");
            String object = errorJson.getString("object");
            switch (text) {
                case PREREQUISITES:
                    return "Ошибка в параметрах запроса " + object;
                case NO_USER:
                    return "Пользователь отсутствует";
                case ALREADY_IN_ROLE:
                    return "Роль уже назначена";
                case NO_RIGHTS:
                    return "Недостаточно прав";
                case TIMEOUT:
                    return "Создать новую точку можно будет через " + object + " минут";
                case UNKNOWN_ERROR:
                    return "Неизвестная ошибка";
            }
        } catch (JSONException ignored) {

        }
        return "Неизвестная ошибка " + response.toString();
    }

    public static boolean isError(String action, JSONObject response) {
        Boolean isError = true;
        switch (action) {
            case MyIntentService.ACTION_ACCIDENTS:
                if (response.has("list")) {
                    try {
                        JSONObject json = response.getJSONArray("list").getJSONObject(0);
                        if (!json.has("error")) {
//                            String error = json.getString("error");
//                            if (error.equals("no_new")) {
//                                res = "Нет новых сообщений";
//                            }
                            isError = false;
                        } else {
//                            res = "Список обновлен";
                        }
                      } catch (JSONException ignored) {
                    }
                }
                break;
        }
        return isError;
    }

    public static boolean isVkError(JSONObject response) {
        return !response.has(RequestErrors.VALID_VK_RESULT);
    }

    public static void showError(Context context, JSONObject response) {
        Toast.makeText(context, getError(response), Toast.LENGTH_LONG).show();
    }
}