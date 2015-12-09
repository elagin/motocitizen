package motocitizen;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import motocitizen.network.RequestErrors;
import motocitizen.network.requests.AccidentsRequest;
import motocitizen.network.requests.OnWayRequest;

/**
 * Created by elagin on 03.12.15.
 */
public class MyIntentService extends IntentService {

    public static final String APP_PATH = "motocitizen";

    public static final String RESULT_CODE = "result_code";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_OPERATION_TYPE = APP_PATH + ".OPERATION_TYPE";

    // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS = APP_PATH + ".STATUS";

    // Defines the key for the log "extra" in an Intent
    public static final String EXTENDED_STATUS_LOG = APP_PATH + ".LOG";

    public static final String RESULT = "RESULT";

    public final static int RESULT_SUCCSESS = 0;
    public final static int RESULT_ERROR = 1;

    public static final String ACTION_AUTH = APP_PATH + ".action.Auth";
    public static final String ACTION_ACCIDENTS = APP_PATH + ".action.Accidents";
    public static final String ACTION_ON_WAY = APP_PATH + ".action.OnWay";

    private static final String SILENT = "silent";
    private static final String ON_WAY_ID = "id";

    private MyApp myApp = null;
    private final BroadcastNotifier mBroadcaster = new BroadcastNotifier(this);

    public MyIntentService() {
        super("MyIntentService");
    }

    public void onCreate() {
        super.onCreate();
        myApp = (MyApp) getApplicationContext();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void returnError(JSONObject response, String action) {
        switch (action) {
            case ACTION_ACCIDENTS:
//                if (!response.has("list")) return "Ошибка соединения " + response.toString();
//                try {
//                    JSONObject json = response.getJSONArray("list").getJSONObject(0);
//                    if (json.has("error")) {
//                        String error = json.getString("error");
//                        if (error.equals("no_new")) {
//                            return "Нет новых сообщений";
//                        }
//                    } else {
//                        return "Список обновлен";
//                    }
//                } catch (JSONException ignored) {
//
//                }
//                return "Неизвестная ошибка " + response.toString();
                mBroadcaster.broadcastIntentWithState(action, RESULT_ERROR, RequestErrors.getError(response));
                break;
            case ACTION_ON_WAY:
//        if (!response.has("result")) return "Ошибка соединения " + response.toString();
//        try {
//            String result = response.getString("result");
//            switch (result) {
//                case "OK":
//                    return "Статус изменен";
//                case "ERROR PREREQUISITES":
//                    return "Неизвестная ошибка " + response.toString();
//            }
//        } catch (JSONException ignored) {
//
//        }
//        return "Неизвестная ошибка " + response.toString();
                break;
        }
        //mBroadcaster.broadcastIntentWithState(action, RESULT_ERROR, RequestErrors.getError(response));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ACTION_ACCIDENTS:
                JSONObject pointList = handleActionAccidents(intent);
                if (RequestErrors.isError(action, pointList)) {
                    returnError(pointList, action);
                } else {
                    MyApp.getContent().parseJSON(pointList);
                }
                mBroadcaster.broadcastIntentWithState(action, RESULT_SUCCSESS, "");
                break;
            case ACTION_ON_WAY:
                JSONObject onWay = handleActionOnWay(intent);
                mBroadcaster.broadcastIntentWithState(action, RESULT_SUCCSESS, "");
                if (RequestErrors.isError(action, onWay)) {
                    returnError(onWay, action);
                } else {
                    //MyApp.getContent().parseJSON(pointList);
                }
                MyIntentService.startActionGetAccidents(this, true);
                break;
            default:
                mBroadcaster.broadcastIntentWithState(action, RESULT_SUCCSESS, "");
                break;
        }
    }

    private static Intent newIntent(Context context, String action) {
        Intent res = new Intent(context, MyIntentService.class);
        res.setAction(action);
        return res;
    }

    public static void startActionGetAccidents(Context context, boolean isSilent) {
        Intent intent = newIntent(context, ACTION_ACCIDENTS);
        intent.putExtra(SILENT, isSilent);
        context.startService(intent);
    }

    private JSONObject handleActionAccidents(Intent intent) {
        boolean isSilent = intent.getBooleanExtra(SILENT, true);
        return new AccidentsRequest(isSilent).request();
    }

    public static void startActionOnWay(Context context, int id) {
        Intent intent = newIntent(context, ACTION_ON_WAY);
        intent.putExtra(ON_WAY_ID, id);
        context.startService(intent);
    }

    private JSONObject handleActionOnWay(Intent intent) {
        int id = intent.getIntExtra(ON_WAY_ID, 0);
        return new OnWayRequest(id).request();
    }
}
