package motocitizen.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import motocitizen.Activity.AccidentDetailsActivity;
import motocitizen.MyApp;
import motocitizen.accident.Volunteer;
import motocitizen.content.VolunteerStatus;
import motocitizen.draw.Rows;
import motocitizen.main.R;
import motocitizen.network.AsyncTaskCompleteListener;
import motocitizen.network.requests.AccidentsRequest;
import motocitizen.network.requests.CancelOnWayRequest;
import motocitizen.network.requests.OnWayRequest;
import motocitizen.utils.Preferences;

public class DetailVolunteersFragment extends AccidentDetailsFragments {

    private static final int DIALOG_ONWAY_CONFIRM        = 1;
    private static final int DIALOG_ACC_NOT_ACTUAL       = 2;
    private static final int DIALOG_CANCEL_ONWAY_CONFIRM = 3;

    private ImageButton onwayButton;
    private ImageButton onwayCancelButton;
    private ImageButton onwayDisabledButton;
    private View        onwayContent;

    // TODO: Rename and change types and number of parameters
    public static DetailVolunteersFragment newInstance(int accID, String userName) {
        DetailVolunteersFragment fragment = new DetailVolunteersFragment();
        Bundle                   args     = new Bundle();
        args.putInt(ACCIDENT_ID, accID);
        args.putString(USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailVolunteersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewMain = inflater.inflate(R.layout.fragment_detail_volunteers, container, false);

        onwayButton = (ImageButton) viewMain.findViewById(R.id.onway_button);
        onwayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ONWAY_CONFIRM);
            }
        });

        onwayCancelButton = (ImageButton) viewMain.findViewById(R.id.onway_cancel_button);
        onwayCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_CANCEL_ONWAY_CONFIRM);
            }
        });

        onwayDisabledButton = (ImageButton) viewMain.findViewById(R.id.onway_disabled_button);
        onwayDisabledButton.setEnabled(false);
        onwayContent = viewMain.findViewById(R.id.acc_onway_table);
        //inplaceContent = viewMain.findViewById(R.id.acc_inplace_table);
        View toMap = viewMain.findViewById(R.id.details_to_map_button);
        toMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AccidentDetailsActivity) getActivity()).jumpToMap();
            }
        });
        update();
        return viewMain;
    }

    private void update() {
        motocitizen.accident.Accident accident = ((AccidentDetailsActivity) getActivity()).getCurrentPoint();

        if (accident == null) {
            showDialog(DIALOG_ACC_NOT_ACTUAL);
            return;
        }

        setupAccess();
        ViewGroup vg_onway = (ViewGroup) onwayContent;
        vg_onway.removeAllViews();
        for (int i : accident.getVolunteers().keySet()) {
            Volunteer current = accident.getVolunteer(i);
            if (current.getStatus() == VolunteerStatus.LEAVE) continue;
            vg_onway.addView(Rows.getVolunteerRow(vg_onway, current));
        }

    }

    private void setupAccess() {
        motocitizen.accident.Accident accident = ((AccidentDetailsActivity) getActivity()).getCurrentPoint();
        int                           id       = accident.getId();
        boolean                       active   = accident.isActive() && MyApp.isAuthorized();
        onwayButton.setVisibility(id != Preferences.getOnWay() && id != MyApp.getContent().getInplaceId() && active ? View.VISIBLE : View.GONE);
        onwayCancelButton.setVisibility(id == Preferences.getOnWay() && id != MyApp.getContent().getInplaceId() && active ? View.VISIBLE : View.GONE);
        onwayDisabledButton.setVisibility(id == MyApp.getContent().getInplaceId() && active ? View.VISIBLE : View.GONE);
    }

    private void showDialog(int type) {
        mStackLevel++;
        FragmentTransaction ft   = getActivity().getFragmentManager().beginTransaction();
        Fragment            prev = getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        ft.commit();
        Activity act = getActivity();

        switch (type) {
            case DIALOG_ONWAY_CONFIRM:
                DialogFragment onwayConfirm = ConfirmDialog.newInstance(act.getString(R.string.title_dialog_onway_confirm), act.getString(android.R.string.yes), act.getString(android.R.string.no));
                onwayConfirm.setTargetFragment(this, type);
                onwayConfirm.show(getFragmentManager().beginTransaction(), "dialog");
                break;

            case DIALOG_CANCEL_ONWAY_CONFIRM:
                DialogFragment cancelOnwayConfirm = ConfirmDialog.newInstance(act.getString(R.string.title_dialog_cancel_onway_confirm), act.getString(android.R.string.yes), act.getString(android.R.string.no));
                cancelOnwayConfirm.setTargetFragment(this, type);
                cancelOnwayConfirm.show(getFragmentManager().beginTransaction(), "dialog");
                break;

            case DIALOG_ACC_NOT_ACTUAL:
                DialogFragment dialogFrag = ConfirmDialog.newInstance(act.getString(R.string.title_dialog_acc_not_actual), act.getString(android.R.string.ok), "");
                dialogFrag.setTargetFragment(this, type);
                dialogFrag.show(getFragmentManager().beginTransaction(), "dialog");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case DIALOG_ONWAY_CONFIRM:
                if (resultCode == Activity.RESULT_OK) {
                    sendOnway();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // After Cancel code.
                }
                break;
            case DIALOG_CANCEL_ONWAY_CONFIRM:
                if (resultCode == Activity.RESULT_OK) {
                    sendCancelOnway();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // After Cancel code.
                }
                break;
            case DIALOG_ACC_NOT_ACTUAL:
                getActivity().finish();
                break;
        }
    }

    private void sendOnway() {
        //AccidentsGeneral.setOnWay(accidentID);
        Preferences.setOnWay(accidentID);
        new OnWayRequest(new OnWayCallback(), accidentID);
    }

    private void message(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    private class OnWayCallback implements AsyncTaskCompleteListener {
        @Override
        public void onTaskComplete(JSONObject result) {
            if (result.has("error")) {
                try {
                    message(result.getString("error"));
                } catch (JSONException e) {
                    message("Неизвестная ошибка " + result.toString());
                }
            } else {
                new AccidentsRequest(new UpdateAccidentsCallback());
            }
        }
    }

    private class UpdateAccidentsCallback implements AsyncTaskCompleteListener {
        @Override
        public void onTaskComplete(JSONObject result) {
            MyApp.getContent().update();
            ((AccidentDetailsActivity) getActivity()).update();
            update();
        }
    }

    private void sendCancelOnway() {
        Preferences.setOnWay(0);
        new CancelOnWayRequest(new OnWayCallback(), accidentID);
    }
}
