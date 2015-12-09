package motocitizen.Activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import motocitizen.MyApp;
import motocitizen.MyIntentService;
import motocitizen.fragments.MainScreenFragment;
import motocitizen.main.R;
import motocitizen.utils.ChangeLog;
import motocitizen.utils.Const;

public class MainScreenActivity extends ActionBarActivity {

    private static ActionBar actionBar;
    private MainScreenFragment mainScreenFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(Const.BROADCAST_ACTION);

        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        // Instantiates a new ResponseStateReceiver
        ResponseStateReceiver mDownloadStateReceiver = new ResponseStateReceiver();

        // Registers the ResponseStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver, statusIntentFilter);

        setContentView(R.layout.main_screen_activity);
        MyApp.setCurrentActivity(this);

        actionBar = getSupportActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApp.setCurrentActivity(this);
        mainScreenFragment = new MainScreenFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, mainScreenFragment).commit();

        MyApp.getLocationManager().wakeup();
        if (ChangeLog.isNewVersion()) {
            AlertDialog changeLogDlg = ChangeLog.getDialog();
            changeLogDlg.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.getLocationManager().sleep();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public static void updateStatusBar(String address) {
        String subTitle = "";
        //Делим примерно пополам, учитывая пробел или запятую
        int commaPos = address.lastIndexOf(",", address.length() / 2);
        int spacePos = address.lastIndexOf(" ", address.length() / 2);

        if (commaPos != -1 || spacePos != -1) {
            subTitle = address.substring(Math.max(commaPos, spacePos) + 1);
            address = address.substring(0, Math.max(commaPos, spacePos));
        }

        actionBar.setTitle(address);
        if (!subTitle.isEmpty()) actionBar.setSubtitle(subTitle);
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
                        if (!mainScreenFragment.isVisible())
                            return;
                        mainScreenFragment.stopRefreshAnimation();
                        mainScreenFragment.redraw();
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
