package motocitizen.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import motocitizen.Activity.AboutActivity;
import motocitizen.Activity.CreateAccActivity;
import motocitizen.Activity.MyFragment;
import motocitizen.Activity.SettingsActivity;
import motocitizen.MyApp;
import motocitizen.MyIntentService;
import motocitizen.content.Content;
import motocitizen.draw.AccidentRow;
import motocitizen.gcm.NewAccidentReceived;
import motocitizen.main.R;
import motocitizen.maps.MyMapManager;
import motocitizen.maps.google.MyGoogleMapManager;
import motocitizen.utils.BounceScrollView;
import motocitizen.utils.Const;
import motocitizen.utils.OverScrollListenerInterface;
import motocitizen.utils.Preferences;

public class MainScreenFragment extends MyFragment {
    private        ViewGroup    mapContainer;
    private        ImageButton  createAccButton;
    private        ImageButton  toAccListButton;
    private        ImageButton  toMapButton;
    private        View         accListView;
    private static ProgressBar  progressBar;
    public static  boolean      inTransaction;
    private static MenuItem     refreshItem;
    private static MyMapManager map;

    protected boolean fromDetails;

    static {
        inTransaction = false;
    }

    public MainScreenFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.main_screen_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapContainer = (ViewGroup) getActivity().findViewById(R.id.google_map);
        createAccButton = (ImageButton) getActivity().findViewById(R.id.add_point_button);
        toAccListButton = (ImageButton) getActivity().findViewById(R.id.list_button);
        toMapButton = (ImageButton) getActivity().findViewById(R.id.map_button);

        accListView = getActivity().findViewById(R.id.acc_list);

        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        getActivity().findViewById(R.id.dial_button).setOnClickListener(new DialOnClickListener());
        ((BounceScrollView) getActivity().findViewById(R.id.accListRefresh)).setOverScrollListener(new OnOverScrollUpdateListener());


        createAccButton.setOnClickListener(new CreateOnClickListener());
        toAccListButton.setOnClickListener(new TabsOnClickListener());
        toMapButton.setOnClickListener(new TabsOnClickListener());

        map = new MyGoogleMapManager(getActivity());

        setPermissions();
        setUpRightFragment(getActivity().getIntent());
        redraw();
        getAccidents();
    }

    private class DialOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            //TODO Сделать забор телефона из преференсов
            intent.setData(Uri.parse("tel:+" + Const.PHONE));
            MyApp.getCurrentActivity().startActivity(intent);
        }
    }

    private class CreateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MyApp.getCurrentActivity(), CreateAccActivity.class));
        }
    }

    @Override
    public void setPermissions() {
        createAccButton.setVisibility(MyApp.getRole().isStandart() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void redraw() {
        ViewGroup view = (ViewGroup) MyApp.getCurrentActivity().findViewById(R.id.accListContent);

        if (view == null) return;
        view.removeAllViews();

        //TODO YesterdayRow ???
        //TODO Нет событий

        Content points = MyApp.getContent();
        for (int id : points.reverseSortedKeySet()) {
            if (points.get(id).isInvisible()) continue;
            view.addView(AccidentRow.makeView(view, points.get(id)));
        }
        map.placeAccidents();
    }

    private void setUpRightFragment(Intent intent) {
        String id    = intent.getStringExtra("id");
        int    toMap = intent.getIntExtra("toMap", 0);

        if (toMap != 0) {
            map.zoom(16);
            map.animateToPoint(MyApp.getContent().get(toMap).getLocation());
            fromDetails = intent.getBooleanExtra("fromDetails", false);
            goToMap(toMap);
        } else if (id != null) {
            intent.removeExtra("id");

            MyApp.toDetails(Integer.parseInt(id));
            NewAccidentReceived.clearAll();
        } else {
            goToAccList();
        }
    }

    private void getAccidents() {
        if (MyApp.isOnline()) {
            startRefreshAnimation();
            MyIntentService.startActionGetAccidents(getActivity(), true);
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

//    private class AccidentsRequestCallback implements AsyncTaskCompleteListener {
//
//        public void onTaskComplete(JSONObject result) {
//            if (!result.has("error")) MyApp.getContent().parseJSON(result);
//            if (!isVisible()) return;
//            stopRefreshAnimation();
//            redraw();
//        }
//    }

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
                this.startActivity(intentSettings);
                return true;
            case R.id.small_menu_about:
                Intent intentAbout = new Intent(getActivity(), AboutActivity.class);
                this.startActivity(intentAbout);
                return true;
            case R.id.small_menu_exit:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                this.startActivity(intent);
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

    private class OnOverScrollUpdateListener implements OverScrollListenerInterface {

        @Override
        public void onOverScroll() {
            if (inTransaction) return;
            if (MyApp.isOnline()) {
                startRefreshAnimation();
                MyIntentService.startActionGetAccidents(getActivity(), true);
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.inet_not_available), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void goToAccList() {
        Intent intent = getActivity().getIntent();
        if (intent.hasExtra("toMap")) intent.removeExtra("toMap");
        accListView.animate().translationX(0);
        mapContainer.animate().translationX(Const.getWidth() * 2);
        toMapButton.setAlpha(0.3f);
        toAccListButton.setAlpha(1f);
    }

    private void goToMap(int id) {
        map.jumpToPoint(MyApp.getContent().get(id).getLocation());
        goToMap();
    }

    private void goToMap() {
        accListView.animate().translationX(-Const.getWidth() * 2);
        mapContainer.animate().translationX(0);
        toMapButton.setAlpha(1f);
        toAccListButton.setAlpha(0.3f);
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
