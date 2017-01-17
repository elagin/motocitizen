package motocitizen.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import motocitizen.activity.AboutActivity;
import motocitizen.activity.CreateAccActivity;
import motocitizen.activity.MyFragmentInterface;
import motocitizen.activity.SettingsActivity;
import motocitizen.MyApp;
import motocitizen.content.Content;
import motocitizen.draw.Rows;
import motocitizen.main.R;
import motocitizen.maps.MyMapManager;
import motocitizen.maps.google.MyGoogleMapManager;
import motocitizen.network.AsyncTaskCompleteListener;
import motocitizen.user.Auth;
import motocitizen.utils.BounceScrollView;
import motocitizen.utils.Const;
import motocitizen.utils.OverScrollListenerInterface;
import motocitizen.utils.Preferences;

public class MainScreenFragment extends Fragment implements MyFragmentInterface {
    private        ViewGroup    mapContainer;
    private        ImageButton  createAccButton;
    private        ImageButton  toAccListButton;
    private        ImageButton  toMapButton;
    private        View         accListView;
    private static ProgressBar  progressBar;
    public static  boolean      inTransaction;
    private static MenuItem     refreshItem;
    private static MyMapManager map;

    private enum Screen {
        LIST,
        MAP
    }

    private static Screen currentScreen = Screen.LIST;

    protected boolean fromDetails;

    static {
        inTransaction = false;
    }

    public MainScreenFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.main_screen_fragment, container, false);
    }

    private void bindButtons() {
        mapContainer = (ViewGroup) getActivity().findViewById(R.id.google_map);
        createAccButton = (ImageButton) getActivity().findViewById(R.id.add_point_button);
        toAccListButton = (ImageButton) getActivity().findViewById(R.id.list_button);
        toMapButton = (ImageButton) getActivity().findViewById(R.id.map_button);
        getActivity().findViewById(R.id.dial_button).setOnClickListener(new DialOnClickListener());
        createAccButton.setOnClickListener(new CreateOnClickListener());
        toAccListButton.setOnClickListener(new TabsOnClickListener());
        toMapButton.setOnClickListener(new TabsOnClickListener());
    }

    @Override
    public void onResume() {
        super.onResume();
        accListView = getActivity().findViewById(R.id.acc_list);
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        ((BounceScrollView) getActivity().findViewById(R.id.accListRefresh)).setOverScrollListener(new OnOverScrollUpdateListener());

        bindButtons();

        map = new MyGoogleMapManager(getActivity());

        setPermissions();

        switch (currentScreen) {
            case LIST:
                goToAccList();
                break;
            case MAP:
                goToMap();
                break;
        }

        redraw();
        getAccidents();
    }

    private class DialOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            //TODO Сделать забор телефона из преференсов
            intent.setData(Uri.parse("tel:+" + Const.PHONE));
            getActivity().startActivity(intent);
        }
    }

    private class CreateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(getActivity(), CreateAccActivity.class));
        }
    }

    @Override
    public void setPermissions() {
        createAccButton.setVisibility(Auth.getInstance().getRole().isStandard() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void redraw() {
        ViewGroup view = (ViewGroup) getActivity().findViewById(R.id.accListContent);

        if (view == null) return;
        view.removeAllViews();

        //TODO YesterdayRow ???
        //TODO Нет событий

        Content points = Content.getInstance();
        for (int id : points.reverseSortedKeySet()) {
            if (points.get(id).isInvisible()) continue;
            view.addView(Rows.getAccidentRow(getActivity(), view, points.get(id)));
        }
        map.placeAccidents(getActivity());
    }

//    private void setUpRightFragment(Intent intent) {
//
//        switch (currentScreen) {
//            case LIST:
//                goToAccList();
//                break;
//            case MAP:
//                goToMap();
//                break;
//        }
//
//        String id    = intent.getStringExtra("id");
//        int    toMap = intent.getIntExtra("toMap", 0);
//
//        if (toMap != 0) {
//            map.zoom(16);
//            map.animateToPoint(Content.getInstance().get(toMap).getLocation());
//            fromDetails = intent.getBooleanExtra("fromDetails", false);
//            goToMap(toMap);
//        } else if (id != null) {
//            intent.removeExtra("id");
//
//            NewAccidentReceived.removeFromTray(Content.getInstance().get(Integer.parseInt(id)).getLocation().hashCode());
//            MyApp.toDetails(Integer.parseInt(id));
//            //NewAccidentReceived.clearAll();
//        } else {
//            goToAccList();
//        }
//    }

    private void getAccidents() {
        if (MyApp.isOnline(getContext())) {
            startRefreshAnimation();
            Content.getInstance().requestUpdate(new AccidentsRequestCallback());
        } else {
            Toast.makeText(getActivity(), getString(R.string.inet_not_available), Toast.LENGTH_LONG).show();
        }
    }

    private static void setRefreshAnimation(boolean status) {
        progressBar.setVisibility(status ? View.VISIBLE : View.INVISIBLE);
        inTransaction = status;
        //TODO костыль
        if (refreshItem != null) refreshItem.setVisible(!status);
    }

    public static void stopRefreshAnimation() {
        setRefreshAnimation(false);
    }

    public static void startRefreshAnimation() {
        setRefreshAnimation(true);
    }

    private class AccidentsRequestCallback implements AsyncTaskCompleteListener {

        public void onTaskComplete(JSONObject result) {
            if (!result.has("error")) Content.getInstance().parseJSON(result);
            if (!isVisible()) return;
            stopRefreshAnimation();
            redraw();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.small_settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        refreshItem = menu.findItem(R.id.action_refresh);
        if (inTransaction) refreshItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.small_menu_refresh:
                getAccidents();
                return true;
            case R.id.small_menu_settings:
                Intent intentSettings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.small_menu_about:
                Intent intentAbout = new Intent(getActivity(), AboutActivity.class);
                startActivity(intentAbout);
                return true;
            case R.id.small_menu_exit:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
                return true;
            case R.id.action_refresh:
                getAccidents();
                return true;
            case R.id.do_not_disturb:
                item.setIcon(Preferences.getInstance().getDoNotDisturb() ? R.drawable.ic_lock_ringer_on_alpha : R.drawable.ic_lock_ringer_off_alpha);
                Preferences.getInstance().setDoNotDisturb(!Preferences.getInstance().getDoNotDisturb());
                return true;
        }
        return false;
    }

    private class OnOverScrollUpdateListener implements OverScrollListenerInterface {

        @Override
        public void onOverScroll() {
            if (inTransaction) return;
            if (MyApp.isOnline(getActivity())) {
                startRefreshAnimation();
                Content.getInstance().requestUpdate(new AccidentsRequestCallback());
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.inet_not_available), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void goToAccList() {
        Log.d("SCREEN TO LIST", String.valueOf(Const.getWidth(getActivity()) * 2));
        accListView.animate().translationX(0);
        mapContainer.animate().translationX(Const.getWidth(getActivity()) * 2);
        toMapButton.setAlpha(0.3f);
        toAccListButton.setAlpha(1f);
        currentScreen = Screen.LIST;
    }

    private void goToMap(int id) {
        map.jumpToPoint(Content.getInstance().get(id).getLocation());
        goToMap();
    }

    private void goToMap() {
        accListView.animate().translationX(-Const.getWidth(getActivity()) * 2);
        Log.d("SCREEN TO ACC", String.valueOf(-Const.getWidth(getActivity()) * 2));
        mapContainer.animate().translationX(0);
        toMapButton.setAlpha(1f);
        toAccListButton.setAlpha(0.3f);
        currentScreen = Screen.MAP;
    }

    private class TabsOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case (R.id.list_button):
                    goToAccList();
                    break;
                case (R.id.map_button):
                    goToMap();
                    break;
            }
        }
    }
}
