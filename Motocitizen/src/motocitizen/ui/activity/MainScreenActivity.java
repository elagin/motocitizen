package motocitizen.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import motocitizen.MyApp;
import motocitizen.content.Content;
import motocitizen.content.accident.Accident;
import motocitizen.datasources.network.ApiResponse;
import motocitizen.datasources.preferences.Preferences;
import motocitizen.geo.geolocation.MyLocationManager;
import motocitizen.geo.maps.MainMapManager;
import motocitizen.main.R;
import motocitizen.permissions.Permissions;
import motocitizen.router.Router;
import motocitizen.ui.changelog.ChangeLog;
import motocitizen.ui.rows.accident.AccidentRowFactory;
import motocitizen.ui.rows.accident.Row;
import motocitizen.ui.views.BounceScrollView;
import motocitizen.user.User;
import motocitizen.utils.Utils;

public class MainScreenActivity extends AppCompatActivity {
    private static final byte LIST = 0;
    private static final byte MAP  = 1;

    private ViewGroup   mapContainer;
    private ImageButton createAccButton;
    private ImageButton toAccListButton;
    private ImageButton toMapButton;
    private View        accListView;
    private ProgressBar progressBar;
    private ViewGroup   listContent;

    private MenuItem       refreshItem;
    private MainMapManager map;
    private boolean inTransaction = false;
    private byte    currentScreen = LIST;
    private static ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_activity);
        actionBar = getSupportActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map == null) map = new MainMapManager(this);
        wakeUpLocationUpdate();
        showChangeLogIfUpdated();

        disableDialOnTablets();

        bindViews();
        setUpListeners();

        setPermissions();
        showCurrentFrame();
        redraw();
        getAccidents();
    }

    private void wakeUpLocationUpdate() {
        Permissions.INSTANCE.requestLocation(this,
                                             () -> {
                                                 MyLocationManager.INSTANCE.wakeup();
                                                 map.enableLocation();
                                                 return Unit.INSTANCE;
                                             },
                                             () -> Unit.INSTANCE);
    }

    private void disableDialOnTablets() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            this.findViewById(R.id.dial_button).setEnabled(false);
        }
    }

    private void showChangeLogIfUpdated() {
        if (!Preferences.INSTANCE.getNewVersion()) return;
        AlertDialog changeLogDlg = ChangeLog.INSTANCE.getDialog(this);
        changeLogDlg.show();
        Preferences.INSTANCE.setNewVersion(false);
    }

    private void setUpListeners() {
        createAccButton.setOnClickListener(v -> Router.INSTANCE.goTo(this, Router.Target.CREATE));
        toAccListButton.setOnClickListener(v -> showListFrame());
        toMapButton.setOnClickListener(v -> showMapFrame());
        this.findViewById(R.id.dial_button).setOnClickListener(v -> Router.INSTANCE.dial(this, getString(R.string.phone)));
        ((BounceScrollView) this.findViewById(R.id.accListRefresh)).setOverScrollListener(this::getAccidents);
    }

    private void bindViews() {
        accListView = this.findViewById(R.id.acc_list);
        progressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        mapContainer = (ViewGroup) this.findViewById(R.id.google_map);
        createAccButton = (ImageButton) this.findViewById(R.id.add_point_button);
        toAccListButton = (ImageButton) this.findViewById(R.id.list_button);
        toMapButton = (ImageButton) this.findViewById(R.id.map_button);
        listContent = (ViewGroup) this.findViewById(R.id.accListContent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Permissions.INSTANCE.requestLocation(this,
                                             () -> {MyLocationManager.INSTANCE.sleep(); return Unit.INSTANCE;},
                                             () -> Unit.INSTANCE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("toMap")) {
            //mainFragment.toMap(intent.getExtras().getInt("toMap", 0));
            toMap(intent.getExtras().getInt("toMap", 0));
            intent.removeExtra("toMap");
        }
        if (intent.hasExtra("toDetails")) {
            intent.removeExtra("toDetails");
        }
        setIntent(intent);
    }

    //todo exterminatus static
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

    public void setPermissions() {
        createAccButton.setVisibility(User.INSTANCE.isStandard() ? View.VISIBLE : View.INVISIBLE);
    }

    public void redraw() {
        List<Row> newList = new ArrayList();
        for (Accident accident : Content.INSTANCE.getVisibleReversed()) {
            newList.add(AccidentRowFactory.INSTANCE.make(this, accident));
        }
        listContent.removeAllViews();

        for (Row row : newList) {
            listContent.addView(row);
        }
        map.placeAccidents(this);
    }

    private Unit getAccidents() {
        if (inTransaction) return Unit.INSTANCE;
        if (MyApp.isOnline(this)) {
            startRefreshAnimation();
            Content.INSTANCE.requestUpdate(this::updateCompleteCallback);
        } else {
            Toast.makeText(this, getString(R.string.inet_not_available), Toast.LENGTH_LONG).show();
        }
        return Unit.INSTANCE;
    }

    private Unit updateCompleteCallback(ApiResponse stub) {
        runOnUiThread(() -> {
            stopRefreshAnimation();
            redraw();
        });
        return Unit.INSTANCE;
    }

    private void setRefreshAnimation(boolean status) {
        progressBar.setVisibility(status ? View.VISIBLE : View.INVISIBLE);
        inTransaction = status;
        //TODO костыль
        if (refreshItem != null) refreshItem.setVisible(!status);
    }

    private void stopRefreshAnimation() {
        setRefreshAnimation(false);
    }

    private void startRefreshAnimation() {
        setRefreshAnimation(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.small_settings_menu, menu);
        refreshItem = menu.findItem(R.id.action_refresh);
        if (inTransaction) refreshItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.small_menu_refresh:
                getAccidents();
                return true;
            case R.id.small_menu_settings:
                Router.INSTANCE.goTo(this, Router.Target.SETTINGS);
                return true;
            case R.id.small_menu_about:
                Router.INSTANCE.goTo(this, Router.Target.ABOUT);
                return true;
            case R.id.action_refresh:
                getAccidents();
                return true;
            case R.id.do_not_disturb:
                item.setIcon(Preferences.INSTANCE.getDoNotDisturb() ? R.drawable.ic_lock_ringer_on_alpha : R.drawable.ic_lock_ringer_off_alpha);
                Preferences.INSTANCE.setDoNotDisturb(!Preferences.INSTANCE.getDoNotDisturb());
                return true;
        }
        return false;
    }

    private void setFrame(byte target) {
        currentScreen = target;
        toAccListButton.setAlpha(target == LIST ? 1f : 0.3f);
        toMapButton.setAlpha(target == MAP ? 1f : 0.3f);
        accListView.animate().translationX(target == LIST ? 0 : -Utils.getWidth(this) * 2);
        mapContainer.animate().translationX(target == MAP ? 0 : Utils.getWidth(this) * 2);
    }

    private void showListFrame() {
        setFrame(LIST);
    }

    private void showMapFrame() {
        setFrame(MAP);
    }

    private void showCurrentFrame() {
        setFrame(currentScreen);
    }

    public void toMap(int id) {
        showMapFrame();
        map.jumpToPoint(Content.INSTANCE.accident(id).getCoordinates());
    }
}
