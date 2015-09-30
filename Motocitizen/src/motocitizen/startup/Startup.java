package motocitizen.startup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONObject;

import motocitizen.Activity.AboutActivity;
import motocitizen.Activity.CreateAccActivity;
import motocitizen.Activity.SettingsActivity;
import motocitizen.MyApp;
import motocitizen.content.Content;
import motocitizen.gcm.GCMBroadcastReceiver;
import motocitizen.gcm.NewAccidentReceived;
import motocitizen.geolocation.MyLocationManager;
import motocitizen.main.R;
import motocitizen.maps.MyMapManager;
import motocitizen.maps.google.MyGoogleMapManager;
import motocitizen.maps.osm.MyOSMMapManager;
import motocitizen.network.requests.AsyncTaskCompleteListener;
import motocitizen.network.requests.RegionsRequest;
import motocitizen.utils.Const;

public class Startup extends ActionBarActivity implements View.OnClickListener {

    public static  Context                            context;
    public static  MyMapManager                       map;
    public static  boolean                            fromDetails;
    public static  Menu                               mMenu;
    public static  Integer                            currentGeneral;
    private static ActionBar                          actionBar;
    private static AlertDialog                        changeLogDlg;
    private        ImageButton                        createAccButton;
    private        RadioGroup                         mainTabsGroup;
    private        View                               accListView;
    private        View                               mapContainer;
    static         ProgressBar                        progressBar;
    public static  boolean                            inTransaction;
    private final  RadioGroup.OnCheckedChangeListener mainTabsListener;

    static {
        changeLogDlg = null;
        inTransaction = false;
    }

    {
        mainTabsListener = new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                currentGeneral = group.getCheckedRadioButtonId();
                fromDetails = false;
                accListView.setVisibility(View.VISIBLE);
                mapContainer.setVisibility(View.VISIBLE);
                switch (currentGeneral) {
                    case R.id.tab_map_button:
                        accListView.animate().translationX(-Const.getWidth(context) * 2);
                        mapContainer.animate().translationX(0);
                        break;
                    case R.id.tab_accidents_button:
                    default:
                        accListView.animate().translationX(0);
                        mapContainer.animate().translationX(Const.getWidth(context) * 2);
                        break;
                }
            }
        };
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
        map.placeUser(context);
    }

    public static boolean isChangeLogDlgShowing() {
        return (changeLogDlg != null && changeLogDlg.isShowing());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.main);
        context = this;
        actionBar = getSupportActionBar();
        new Preferences(this);
        Preferences.setDoNotDisturb(false);
        new Const();

        checkUpdate();

        ImageButton dialButton = (ImageButton) findViewById(R.id.dial_button);
        dialButton.setOnClickListener(this);

        createAccButton = (ImageButton) findViewById(R.id.mc_add_point_button);
        createAccButton.setOnClickListener(this);

        mainTabsGroup = (RadioGroup) findViewById(R.id.main_tabs_group);
        mainTabsGroup.setOnCheckedChangeListener(mainTabsListener);

        accListView = findViewById(R.id.mc_acc_list);
        mapContainer = findViewById(R.id.map_container);
        mapContainer.setTranslationX(Const.getWidth(context));

        accListView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return true;
            }
        });

        new Content(this);

        createMap(MyMapManager.GOOGLE);
        new GCMBroadcastReceiver();
        if(((MyApp) getApplicationContext()).getRegions().isEmpty())
            new RegionsRequest(new RegionsCallback(), this);
    }

    private void checkUpdate() {
        PackageManager manager = this.getPackageManager();
        String         version;
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = getString(R.string.unknown_code_version);
        }
        if (!Preferences.getCurrentVersion().equals(version)) {
            changeLogDlg = ChangeLog.getDialog(this, true);
            changeLogDlg.show();
        }
        Preferences.setCurrentVersion(version);
    }

    public static void createMap(String name) {
        if (map != null && map.getName().equals(name)) return;
        switch (name) {
            case MyMapManager.OSM:
                map = new MyOSMMapManager(context);
                break;
            case MyMapManager.GOOGLE:
            default:
                map = new MyGoogleMapManager(context);
        }

        Location location = MyLocationManager.getLocation();
        map.jumpToPoint(location);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyLocationManager.sleep();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLocationManager.wakeup();
        Intent intent = getIntent();
        String id     = intent.getStringExtra("id");
        int    toMap  = intent.getIntExtra("toMap", 0);
        context = this;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        createAccButton.setVisibility(Content.auth.getRole().isStandart() ? View.VISIBLE : View.INVISIBLE);
        Content.redraw(this);
        getAccidents();

        if (toMap != 0) {
            intent.removeExtra("toMap");
            mainTabsGroup.check(R.id.tab_map_button);
            fromDetails = intent.getBooleanExtra("fromDetails", false);
        } else if (id != null) {
            intent.removeExtra("id");
            Content.refresh(this);
            Content.toDetails(this, Integer.parseInt(id));
            NewAccidentReceived.clearAll(this);
        }
    }

    private void getAccidents() {
        if (Startup.isOnline(context)) {
            startRefreshAnimation();
            Content.update(this, new AccidentsRequestCallback(this));
        } else {
            message(getString(R.string.inet_not_available));
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm      = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo         netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void message(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyUp(int keycode, @NonNull KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_BACK:
                /*
                if (fromDetails) {
                    AccidentsGeneral.toDetails(this);
                }
                */
                Content.redraw(this);
                return true;
        }
        return super.onKeyUp(keycode, e);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.small_settings_menu, menu);
        mMenu = menu;
        MenuItem itemMenuNotDisturb = mMenu.findItem(R.id.do_not_disturb);
        MenuItem refreshItem        = mMenu.findItem(R.id.action_refresh);
        if (inTransaction) refreshItem.setVisible(false);
        itemMenuNotDisturb.setIcon(Preferences.getDoNotDisturb() ? R.drawable.ic_lock_ringer_off_alpha : R.drawable.ic_lock_ringer_on_alpha);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.small_menu_refresh:
                // Do animation start
                getAccidents();
                return true;
            case R.id.small_menu_settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                this.startActivity(intentSettings);
                return true;
            case R.id.small_menu_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                context.startActivity(intentAbout);
                return true;
            case R.id.small_menu_exit:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                context.startActivity(intent);
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
                return true;
            case R.id.action_refresh:
                getAccidents();
                return true;
            case R.id.do_not_disturb:
                item.setIcon(Preferences.getDoNotDisturb() ? R.drawable.ic_lock_ringer_on_alpha : R.drawable.ic_lock_ringer_off_alpha);
                Preferences.setDoNotDisturb(!Preferences.getDoNotDisturb());
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.dial_button:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+78007751734"));
                this.startActivity(intent);
                break;
            case R.id.mc_add_point_button:
                startActivity(new Intent(this, CreateAccActivity.class));
                break;
            default:
                Log.e("Startup", "Unknown button pressed");
                break;
        }
    }

    private static class AccidentsRequestCallback implements AsyncTaskCompleteListener {
        private Context context;

        public AccidentsRequestCallback(final Context context) {
            this.context = context;
        }

        public void onTaskComplete(JSONObject result) {
            stopRefreshAnimation();
            if (!result.has("error")) Content.parseJSON(result);
            Content.redraw(context);
            Startup.map.placeAccidents(context);
        }
    }

    public static void startRefreshAnimation() {
        progressBar.setVisibility(View.VISIBLE);
        inTransaction = true;
        if (mMenu == null) return;
        MenuItem refreshItem = mMenu.findItem(R.id.action_refresh);
        refreshItem.setVisible(false);
    }

    public static void stopRefreshAnimation() {
        progressBar.setVisibility(View.INVISIBLE);
        inTransaction = false;
        if (mMenu == null) return;
        MenuItem refreshItem = mMenu.findItem(R.id.action_refresh);
        refreshItem.setVisible(true);
    }

    private class RegionsCallback implements AsyncTaskCompleteListener {
        @Override
        public void onTaskComplete(JSONObject result) {
            if (!result.has("error")) {
                ((MyApp) getApplicationContext()).setRegions(result);
            }
        }
    }
}
