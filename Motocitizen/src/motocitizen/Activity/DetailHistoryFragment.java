package motocitizen.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import motocitizen.app.general.AccidentHistory;
import motocitizen.app.general.AccidentsGeneral;
import motocitizen.main.R;

public class DetailHistoryFragment extends AccidentDetailsFragments {

    private OnFragmentInteractionListener mListener;

    private View mcDetLogContent;

    public static DetailHistoryFragment newInstance(int accID, String userName) {
        DetailHistoryFragment fragment = new DetailHistoryFragment();
        Bundle args = new Bundle();
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
        currentPoint = AccidentsGeneral.points.getPoint(accidentID);
        mcDetLogContent = viewMain.findViewById(R.id.mc_det_log_content);

        update();
        return viewMain;
    }

    protected void update() {
        ViewGroup logView = (ViewGroup) mcDetLogContent;
        logView.removeAllViews();
        logView.addView(AccidentHistory.createHeader(getActivity()));
        for (int i : currentPoint.getSortedHistoryKeys()) {
            logView.addView(currentPoint.history.get(i).createRow(getActivity(), userName));
        }
    }

    public void notifyDataSetChanged() {
        update();
    }
}
