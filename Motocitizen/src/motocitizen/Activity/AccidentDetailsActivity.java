package motocitizen.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import motocitizen.MyApp;
import motocitizen.MyIntentService;
import motocitizen.accident.Accident;
import motocitizen.app.general.popups.AccidentListPopup;
import motocitizen.content.AccidentStatus;
import motocitizen.content.Medicine;
import motocitizen.fragments.DetailHistoryFragment;
import motocitizen.fragments.DetailMessagesFragment;
import motocitizen.fragments.DetailVolunteersFragment;
import motocitizen.main.R;
import motocitizen.network.AsyncTaskCompleteListener;
import motocitizen.network.requests.AccidentChangeStateRequest;
import motocitizen.utils.Const;
import motocitizen.utils.MyUtils;
import motocitizen.utils.Preferences;

import static motocitizen.content.AccidentStatus.ACTIVE;
import static motocitizen.content.AccidentStatus.ENDED;
import static motocitizen.content.AccidentStatus.HIDDEN;

public class AccidentDetailsActivity extends ActionBarActivity {

    /* constants */
    private static final int SMS_MENU_MIN_ID  = 100;
    private static final int SMS_MENU_MAX_ID  = 200;
    private static final int CALL_MENU_MIN_ID = 400;
    private static final int CALL_MENU_MAX_ID = 500;
    /* end constants */

    /*
    Инцидент с которым работаем
     */
    private int      accidentID;
    private Accident currentPoint;

    private AccidentStatus accNewState;

    private DetailVolunteersFragment detailVolunteersFragment;
    private DetailMessagesFragment   detailMessagesFragment;
    private DetailHistoryFragment    detailHistoryFragment;

    private TextView statusView;
    private TextView medicineView;
    private View     generalLayout;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp.setCurrentActivity(this);

        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(Const.BROADCAST_ACTION);
        // Instantiates a new ResponseStateReceiver
        ResponseStateReceiver mDownloadStateReceiver = new ResponseStateReceiver();
        // Registers the ResponseStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver, statusIntentFilter);

        setContentView(R.layout.activity_accident_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        accidentID = b.getInt("accidentID");
        currentPoint = MyApp.getContent().get(accidentID);

        //NewAccidentReceived.removeNotification(accidentID);

        String userName = Preferences.getLogin();

        detailVolunteersFragment = DetailVolunteersFragment.newInstance(accidentID, userName);
        detailMessagesFragment = DetailMessagesFragment.newInstance(accidentID, userName);
        detailHistoryFragment = DetailHistoryFragment.newInstance(accidentID, userName);

        /*
        * Описание группы закладок внутри деталей происшествия
        */
        RadioGroup mcDetTabsGroup = (RadioGroup) findViewById(R.id.details_tabs_group);
        mcDetTabsGroup.setOnCheckedChangeListener(accDetTabsListener);

        generalLayout = findViewById(R.id.acc_details_general);
        statusView = (TextView) findViewById(R.id.acc_details_general_status);
        medicineView = (TextView) findViewById(R.id.acc_details_medicine);

        getFragmentManager().beginTransaction().replace(R.id.details_tab_content, detailVolunteersFragment).commit();
        menuReconstruction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.setCurrentActivity(this);
        generalLayout.setOnLongClickListener(new DetailsLongClickListener());
        update();
    }

    public void update() {
        currentPoint = MyApp.getContent().get(accidentID);

        ActionBar actionBar = getSupportActionBar();
        //TODO Разобраться с nullPointerException и убрать костыль
        if (currentPoint == null || actionBar == null) return;
        actionBar.setTitle(currentPoint.getType().toString() + ". " + currentPoint.getDistanceString());

        statusView.setVisibility(currentPoint.getStatus() == ACTIVE ? View.GONE : View.VISIBLE);
        medicineView.setVisibility(currentPoint.getMedicine() == Medicine.UNKNOWN ? View.GONE : View.VISIBLE);

        ((TextView) findViewById(R.id.acc_details_general_type)).setText(currentPoint.getType().toString());
        medicineView.setText("(" + currentPoint.getMedicine().toString() + ")");
        statusView.setText(currentPoint.getStatus().toString());
        ((TextView) findViewById(R.id.acc_details_general_time)).setText(Const.TIME_FORMAT.format(currentPoint.getTime()));
        ((TextView) findViewById(R.id.acc_details_general_owner)).setText(currentPoint.getOwner());
        ((TextView) findViewById(R.id.acc_details_general_address)).setText(currentPoint.getAddress());
        ((TextView) findViewById(R.id.acc_details_general_distance)).setText(currentPoint.getDistanceString());
        ((TextView) findViewById(R.id.acc_details_general_description)).setText(currentPoint.getDescription());

        menuReconstruction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accident_details, menu);
        mMenu = menu;
        //TODO Костыль
        if (currentPoint == null) return super.onCreateOptionsMenu(menu);
        List<String> contactNumbers = MyUtils.getPhonesFromText(currentPoint.getDescription());
        if (contactNumbers.isEmpty()) return super.onCreateOptionsMenu(menu);
        if (contactNumbers.size() == 1) {
            mMenu.add(0, SMS_MENU_MIN_ID, 0, getString(R.string.send_sms) + contactNumbers.get(0));
            mMenu.add(0, CALL_MENU_MIN_ID, 0, getString(R.string.make_call) + contactNumbers.get(0));
        } else {
            SubMenu smsSub = mMenu.addSubMenu(getString(R.string.send_sms));
            SubMenu callSub = mMenu.addSubMenu(getString(R.string.make_call));
            for (int i = 0; i < contactNumbers.size(); i++) {
                smsSub.add(0, SMS_MENU_MIN_ID + i, 0, contactNumbers.get(i));
                callSub.add(0, CALL_MENU_MIN_ID + i, 0, contactNumbers.get(i));
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void menuReconstruction() {
        if (mMenu == null) return;
        currentPoint = MyApp.getContent().get(accidentID);
        MenuItem finish = mMenu.findItem(R.id.menu_acc_finish);
        MenuItem hide   = mMenu.findItem(R.id.menu_acc_hide);
        finish.setVisible(MyApp.getRole().isModerator());
        hide.setVisible(MyApp.getRole().isModerator());
        finish.setTitle(R.string.finish);
        hide.setTitle(R.string.hide);
        switch (currentPoint.getStatus()) {
            case ENDED:
                finish.setTitle(R.string.unfinish);
                break;
            case HIDDEN:
                hide.setTitle(R.string.show);
        }
    }

    private void showGeneralLayout(int state) {
        MenuItem menuItemActionHideInfo = mMenu.findItem(R.id.action_hide_info);
        MenuItem menuItemMenuHideInfo   = mMenu.findItem(R.id.menu_hide_info);
        if (state == View.INVISIBLE) {
            generalLayout.setVisibility(View.GONE);
            menuItemActionHideInfo.setIcon(R.drawable.ic_panel_down);
            menuItemMenuHideInfo.setTitle(getString(R.string.show_info_details));
        } else {
            generalLayout.setVisibility(View.VISIBLE);
            menuItemActionHideInfo.setIcon(R.drawable.ic_panel_up);
            menuItemMenuHideInfo.setTitle(getString(R.string.hide_info_details));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
            case R.id.action_to_map:
                jumpToMap();
                return true;
            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, AccidentListPopup.getAccidentTextToCopy(currentPoint));
                sendIntent.setType("text/plain");
                this.startActivity(sendIntent);
                return true;
            case R.id.action_hide_info:
            case R.id.menu_hide_info:
                if (generalLayout.getVisibility() == View.VISIBLE)
                    showGeneralLayout(View.INVISIBLE);
                else showGeneralLayout(View.VISIBLE);
                return true;
            case R.id.menu_acc_finish:
                sendFinishRequest();
                return true;
            case R.id.menu_acc_hide:
                sendHideRequest();
                return true;
        }
        if (item.getItemId() >= SMS_MENU_MIN_ID && item.getItemId() < SMS_MENU_MAX_ID) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String smsPrefix = getString(R.string.send_sms);
            String number = (String) item.getTitle();
            //if (number.indexOf(smsPrefix) != -1)
            if (number.contains(smsPrefix))
                number = number.substring(smsPrefix.length(), number.length());
            intent.setData(Uri.parse("sms:" + number));
            startActivity(intent);
        } else if (item.getItemId() >= CALL_MENU_MIN_ID && item.getItemId() < CALL_MENU_MAX_ID) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            String callPrefix = getString(R.string.make_call);
            String number = (String) item.getTitle();
            //if (number.indexOf(callPrefix) != -1)
            if (number.contains(callPrefix))
                number = number.substring(callPrefix.length(), number.length());
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
        return false;
    }

    private void sendFinishRequest() {
        if (MyApp.getContent().get(accidentID).getStatus() == ENDED) {
            //TODO Суперкостыль
            accNewState = ACTIVE;
            new AccidentChangeStateRequest(new AccidentChangeCallback(), accidentID, ACTIVE.toCode());
        } else {
            //TODO Суперкостыль
            accNewState = ENDED;
            new AccidentChangeStateRequest(new AccidentChangeCallback(), accidentID, ENDED.toCode());
        }
    }

    private void sendHideRequest() {
        if (MyApp.getContent().get(accidentID).getStatus() == ENDED) {
            //TODO Суперкостыль
            accNewState = ACTIVE;
            new AccidentChangeStateRequest(new AccidentChangeCallback(), accidentID, ACTIVE.toCode());
        } else {
            //TODO Суперкостыль
            accNewState = ENDED;
            new AccidentChangeStateRequest(new AccidentChangeCallback(), accidentID, HIDDEN.toCode());
        }
    }

    private void message(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private class AccidentChangeCallback implements AsyncTaskCompleteListener {
        @Override
        public void onTaskComplete(JSONObject result) {

            if (result.has("error")) {
                String error;
                try {
                    error = result.getString("error");
                } catch (JSONException e) {
                    e.printStackTrace();
                    error = "Неизвестная ошибка";
                }
                message(error);
            } else {
                //TODO Суперкостыль
                currentPoint.setStatus(accNewState);
                update();
            }
        }
    }

    private final RadioGroup.OnCheckedChangeListener accDetTabsListener = new RadioGroup.OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int id = group.getCheckedRadioButtonId();

            if (id == R.id.details_tab_messages) {
                getFragmentManager().beginTransaction().replace(R.id.details_tab_content, detailMessagesFragment).commit();
            } else if (id == R.id.details_tab_history) {
                getFragmentManager().beginTransaction().replace(R.id.details_tab_content, detailHistoryFragment).commit();
            } else if (id == R.id.details_tab_people) {
                getFragmentManager().beginTransaction().replace(R.id.details_tab_content, detailVolunteersFragment).commit();
            }
        }
    };

    public void jumpToMap() {
        Intent intent = new Intent(this, MainScreenActivity.class);
        intent.putExtra("toMap", currentPoint.getId());
        intent.putExtra("fromDetails", true);
        this.startActivity(intent);
        finish();
    }

    public Accident getCurrentPoint() {
        return currentPoint;
    }

    private class DetailsLongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            PopupWindow popupWindow;
            popupWindow = (new AccidentListPopup(currentPoint.getId())).getPopupWindow();
            int viewLocation[] = new int[2];
            v.getLocationOnScreen(viewLocation);
            popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, viewLocation[0], viewLocation[1]);
            return true;
        }
    }

    private class ResponseStateReceiver extends BroadcastReceiver {
        private ResponseStateReceiver() {
            // prevents instantiation by other packages.
        }

        /**
         * This method is called by the system when a broadcast Intent is matched by this class'
         * intent filters
         *
         * @param context An Android context
         * @param intent  The incoming broadcast Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra(MyIntentService.RESULT_CODE, 0);
            if (resultCode == MyIntentService.RESULT_SUCCSESS) {
                switch (intent.getStringExtra(MyIntentService.EXTENDED_OPERATION_TYPE)) {
                    case MyIntentService.ACTION_ACCIDENTS:
                        update();
                        detailMessagesFragment.update();
                        break;
                    default:
                        break;
                }
            } else if (resultCode == MyIntentService.RESULT_ERROR) {
                String error = intent.getStringExtra(MyIntentService.RESULT);
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "В onReceiveResult пришло не понятно что.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
