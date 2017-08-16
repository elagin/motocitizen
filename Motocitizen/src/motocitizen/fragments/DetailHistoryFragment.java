package motocitizen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import motocitizen.activity.AccidentDetailsActivity;
import motocitizen.content.accident.Accident;
import motocitizen.main.R;
import motocitizen.rows.RowFactory;

public class DetailHistoryFragment extends AccidentDetailsFragments {

    private LinearLayout logContent;

    public static DetailHistoryFragment newInstance(int accID, String userName) {
        DetailHistoryFragment fragment = new DetailHistoryFragment();
        Bundle                args     = new Bundle();
        args.putInt(ACCIDENT_ID, accID);
        args.putString(USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    public DetailHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewMain = inflater.inflate(R.layout.fragment_detail_history, container, false);
        logContent = (LinearLayout) viewMain.findViewById(R.id.details_log_content);

        update();
        return viewMain;
    }

    private void update() {
        Accident accident = ((AccidentDetailsActivity) getActivity()).getCurrentPoint();

        logContent.removeAllViews();
        for (int i : accident.getHistory().keySet()) {
            logContent.addView(RowFactory.INSTANCE.make(getActivity(), accident.getHistory().get(i)));
        }
    }
}
