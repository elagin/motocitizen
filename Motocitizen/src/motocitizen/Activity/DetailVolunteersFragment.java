package motocitizen.Activity;

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

import motocitizen.app.general.Accident;
import motocitizen.app.general.AccidentVolunteer;
import motocitizen.app.general.AccidentsGeneral;
import motocitizen.main.R;
import motocitizen.network.requests.AccidentsRequest;
import motocitizen.network.requests.AsyncTaskCompleteListener;
import motocitizen.network.requests.CancelOnWayRequest;
import motocitizen.network.requests.OnWayRequest;

public class DetailVolunteersFragment extends AccidentDetailsFragments {

    public static final int DIALOG_ONWAY_CONFIRM        = 1;
    public static final int DIALOG_ACC_NOT_ACTUAL       = 2;
    public static final int DIALOG_CANCEL_ONWAY_CONFIRM = 3;

    private ImageButton onwayButton;
    private ImageButton onwayCancelButton;
    private ImageButton onwayDisabledButton;
    private View        toMap;
    private View        onwayContent;
    //private View        inplaceContent;

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
        toMap = viewMain.findViewById(R.id.details_to_map_button);
        toMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AccidentDetailsActivity) getActivity()).jumpToMap();
            }
        });
        update();
        return viewMain;
    }

    protected void update() {
        Accident accident = ((AccidentDetailsActivity) getActivity()).getCurrentPoint();

        if (accident != null) {
            setupAccess();

            ViewGroup vg_onway = (ViewGroup) onwayContent;
            vg_onway.removeAllViews();
            for (int i : accident.getSortedVolunteersKeys()) {
                AccidentVolunteer current = accident.volunteers.get(i);
                /*
                switch (current.getStatus()) {
                    case ONWAY:
                        vg_onway.addView(getDelimiterRow(getActivity(), "В пути"));
                        break;
                    case INPLACE:
                        vg_onway.addView(getDelimiterRow(getActivity(), "На месте"));
                        break;
                    case LEAVE:
                        vg_onway.addView(getDelimiterRow(getActivity(), "Были"));
                        break;
                }
                */
                vg_onway.addView(current.createRow(getActivity()));
            }
        } else {
            showDialog(DIALOG_ACC_NOT_ACTUAL);
        }
    }

    public void notifyDataSetChanged() {
        update();
//  ListAdapter
//        adapter.notifyDataSetChanged();
    }

    private void setupAccess() {
        Accident accident = ((AccidentDetailsActivity) getActivity()).getCurrentPoint();
        if (accident.getId() == prefs.getOnWay()) {
            onwayButton.setVisibility(View.GONE);
            onwayCancelButton.setVisibility(View.VISIBLE);
            onwayDisabledButton.setVisibility(View.GONE);

        } else if (accident.getId() == AccidentsGeneral.getInplaceID() || !AccidentsGeneral.auth.isAuthorized() || !accident.isActive()) {
            onwayButton.setVisibility(View.GONE);
            onwayCancelButton.setVisibility(View.GONE);
            onwayDisabledButton.setVisibility(View.VISIBLE);

        } else {
            onwayButton.setVisibility(View.VISIBLE);
            onwayCancelButton.setVisibility(View.GONE);
            onwayDisabledButton.setVisibility(View.GONE);
        }
    }

    void showDialog(int type) {
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
                DialogFragment onwayConfirm = ConfirmDialog.newInstance(
                        act.getString(R.string.title_dialog_onway_confirm),
                        act.getString(android.R.string.yes),
                        act.getString(android.R.string.no));
                onwayConfirm.setTargetFragment(this, type);
                onwayConfirm.show(getFragmentManager().beginTransaction(), "dialog");
                break;

            case DIALOG_CANCEL_ONWAY_CONFIRM:
                DialogFragment cancelOnwayConfirm = ConfirmDialog.newInstance(
                        act.getString(R.string.title_dialog_cancel_onway_confirm),
                        act.getString(android.R.string.yes),
                        act.getString(android.R.string.no));
                cancelOnwayConfirm.setTargetFragment(this, type);
                cancelOnwayConfirm.show(getFragmentManager().beginTransaction(), "dialog");
                break;

            case DIALOG_ACC_NOT_ACTUAL:
                DialogFragment dialogFrag = ConfirmDialog.newInstance(
                        act.getString(R.string.title_dialod_acc_not_actual),
                        act.getString(android.R.string.ok),
                        "");
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
        prefs.setOnWay(accidentID);
        new OnWayRequest(new OnWayCallback(), this.getActivity(), accidentID);
    }

    private class OnWayCallback implements AsyncTaskCompleteListener {
        @Override
        public void onTaskComplete(JSONObject result) {
            if (result.has("error")) {
                try {
                    Toast.makeText(getActivity(), result.getString("error"), Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    Toast.makeText(getActivity(), "Неизвестная ошибка " + result.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                new AccidentsRequest(getActivity(), new UpdateAccidentsCallback());
            }
        }
    }

    private class UpdateAccidentsCallback implements AsyncTaskCompleteListener {
        @Override
        public void onTaskComplete(JSONObject result) {
            try {
                AccidentsGeneral.points.update(result.getJSONArray("list"));
                ((AccidentDetailsActivity) getActivity()).update();
                update();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCancelOnway() {
        prefs.setOnWay(0);
        new CancelOnWayRequest(new OnWayCallback(), this.getActivity(), accidentID);
    }
}
