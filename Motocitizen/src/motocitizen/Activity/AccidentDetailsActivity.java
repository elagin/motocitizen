package motocitizen.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import motocitizen.accident.Accident;
import motocitizen.app.general.popups.AccidentListPopup;
import motocitizen.app.general.user.Role;
import motocitizen.content.AccidentStatus;
import motocitizen.content.Content;
import motocitizen.draw.Strings;
import motocitizen.main.R;
import motocitizen.network.requests.AccidentChangeStateRequest;
import motocitizen.network.requests.AsyncTaskCompleteListener;
import motocitizen.startup.Preferences;
import motocitizen.startup.Startup;
import motocitizen.utils.Const;
import motocitizen.utils.MyUtils;

import static motocitizen.content.AccidentStatus.ACTIVE;
import static motocitizen.content.AccidentStatus.ENDED;
import static motocitizen.content.AccidentStatus.HIDDEN;

public class AccidentDetailsActivity extends ActionBarActivity implements AccidentDetailsFragments.OnFragmentInteractionListener {

    private Context context;

    private static final int SMS_MENU_MIN_ID = 100;
    private static final int SMS_MENU_MAX_ID = 200;

    private static final int CALL_MENU_MIN_ID = 400;
    private static final int CALL_MENU_MAX_ID = 500;

    /*
    Инцидент с которым работаем
     */
    private int      accidentID;
    private Accident currentPoint;

    private AccidentStatus accNewState;

    private DetailVolunteersFragment detailVolunteersFragment;
    private DetailMessagesFragment   detailMessagesFragment;
    private DetailHistoryFragment    detailHistoryFragment;

    private TextView generalType;
    private TextView generalStatus;
    private TextView generalTime;
    private TextView generalOwner;
    private TextView generalAddress;
    private TextView generalDescription;
    private View     generalLayout;

    private Menu mMenu;

    private Preferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_details);
        context = this;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        accidentID = b.getInt("accidentID");
        currentPoint = Content.get(accidentID);

        //NewAccidentReceived.removeNotification(accidentID);

        String userName = Preferences.getLogin();

        detailVolunteersFragment = DetailVolunteersFragment.newInstance(accidentID, userName);
        detailMessagesFragment = DetailMessagesFragment.newInstance(accidentID, userName);
        detailHistoryFragment = DetailHistoryFragment.newInstance(accidentID, userName);

        /*
        * Описание группы закладок внутри деталей происшествия
        */
        RadioGroup mcDetTabsGroup = (RadioGroup) findViewById(R.id.mc_det_tabs_group);
        mcDetTabsGroup.setOnCheckedChangeListener(accDetTabsListener);

        generalLayout = findViewById(R.id.mc_acc_details_general);
        generalLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupWindow popupWindow;
                popupWindow = (new AccidentListPopup(context, currentPoint.getId(), false)).getPopupWindow();
                int viewLocation[] = new int[2];
                v.getLocationOnScreen(viewLocation);
                popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, viewLocation[0], viewLocation[1]);
                return true;
            }
        });

        generalType = (TextView) findViewById(R.id.acc_details_general_type);
        generalStatus = (TextView) findViewById(R.id.acc_details_general_status);
        generalTime = (TextView) findViewById(R.id.acc_details_general_time);
        generalOwner = (TextView) findViewById(R.id.acc_details_general_owner);
        generalAddress = (TextView) findViewById(R.id.acc_details_general_address);
        generalDescription = (TextView) findViewById(R.id.acc_details_general_description);
//        ((ScrollView) findViewById(R.id.mc_det_messages_scroll)).fullScroll(View.FOCUS_UP);

        getFragmentManager().beginTransaction().replace(R.id.mc_det_tab_content, detailVolunteersFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO Вероятно теперь ни когда null не будет.
        if (prefs == null) prefs = new Preferences(this);
        update();
    }

    void update() {
        currentPoint = Content.getPoint(accidentID);

        ActionBar actionBar = getSupportActionBar();
        //TODO Разобраться с nullPointerException и убрать костыль
        if (currentPoint == null || actionBar == null) return;
        actionBar.setTitle(currentPoint.getType().toString() + ". " + currentPoint.getDistanceString());

        generalType.setText(currentPoint.getType().toString() + ". " + currentPoint.getMedicine().toString());
        generalStatus.setText(currentPoint.getStatus().toString());
        generalTime.setText(Const.TIME_FORMAT.format(currentPoint.getTime()));
        generalOwner.setText(currentPoint.getOwner());
        generalAddress.setText("(" + currentPoint.getDistanceString() + ") " + currentPoint.getAddress());
        generalDescription.setText(currentPoint.getDescription());

        menuReconstruction();

        Startup.map.zoom(16);
        Startup.map.jumpToPoint(currentPoint.getLocation());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accident_details, menu);
        mMenu = menu;
        menuReconstruction();

        List<String> contactNumbers = MyUtils.getPhonesFromText(currentPoint.getDescription());
        if (!contactNumbers.isEmpty()) {
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
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void menuReconstruction() {
        if (mMenu != null) {
            Accident currentPoint = Content.getPoint(accidentID);
            MenuItem finish = mMenu.findItem(R.id.menu_acc_finish);
            MenuItem hide = mMenu.findItem(R.id.menu_acc_hide);
            finish.setVisible(Role.isModerator());
            hide.setVisible(Role.isModerator());
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, Strings.getAccidentTextToCopy(currentPoint));
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
            context.startActivity(intent);
        } else if (item.getItemId() >= CALL_MENU_MIN_ID && item.getItemId() < CALL_MENU_MAX_ID) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            String callPrefix = getString(R.string.make_call);
            String number = (String) item.getTitle();
            //if (number.indexOf(callPrefix) != -1)
            if (number.contains(callPrefix))
                number = number.substring(callPrefix.length(), number.length());
            intent.setData(Uri.parse("tel:" + number));
            context.startActivity(intent);
        }
        return false;
    }

    private void sendFinishRequest() {
        if (Content.getPoint(accidentID).getStatus() == ENDED) {
            //TODO Суперкостыль
            accNewState = ACTIVE;
            new AccidentChangeStateRequest(new AccidentChangeCallback(), this, accidentID, ACTIVE.toString());
        } else {
            //TODO Суперкостыль
            accNewState = ENDED;
            new AccidentChangeStateRequest(new AccidentChangeCallback(), this, accidentID, ENDED.toString());
        }
    }

    private void sendHideRequest() {
        if (Content.getPoint(accidentID).getStatus() == ENDED) {
            //TODO Суперкостыль
            accNewState = ACTIVE;
            new AccidentChangeStateRequest(new AccidentChangeCallback(), this, accidentID, ACTIVE.toString());
        } else {
            //TODO Суперкостыль
            accNewState = ENDED;
            new AccidentChangeStateRequest(new AccidentChangeCallback(), this, accidentID, HIDDEN.toString());
        }
    }

    private void message(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
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

            if (id == R.id.mc_det_tab_messages) {
                getFragmentManager().beginTransaction().replace(R.id.mc_det_tab_content, detailMessagesFragment).commit();
            } else if (id == R.id.mc_det_tab_history) {
                getFragmentManager().beginTransaction().replace(R.id.mc_det_tab_content, detailHistoryFragment).commit();
            } else if (id == R.id.mc_det_tab_people) {
                getFragmentManager().beginTransaction().replace(R.id.mc_det_tab_content, detailVolunteersFragment).commit();
            }
        }
    };

    void jumpToMap() {
        Intent intent = new Intent(this, Startup.class);
        intent.putExtra("toMap", currentPoint.getId());
        intent.putExtra("fromDetails", true);
        this.startActivity(intent);
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

//    MyPreferences getPref() {
//        if (prefs == null)
//            prefs = new MyPreferences(this);
//        return prefs;
//    }

    public Accident getCurrentPoint() {
        return currentPoint;
    }
}
