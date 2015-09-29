package motocitizen.Activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.List;

import motocitizen.MyApp;
import motocitizen.content.Region;
import motocitizen.main.R;
import motocitizen.network.requests.AsyncTaskCompleteListener;
import motocitizen.network.requests.RegionsRequest;
import motocitizen.startup.Preferences;
import motocitizen.utils.Const;

public class SelectRegionActivity extends ActionBarActivity {
    private static int currentId;
    private static ViewGroup vg;
    private List<Region> regions;
    private MyApp myApp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_region);

        myApp = (MyApp) getApplicationContext();
        regions = myApp.getRegions();

        vg = (ViewGroup) findViewById(R.id.region_select_table);

        Button selectSoundConfirmButton = (Button) findViewById(R.id.select_region_save_button);
        selectSoundConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.setDefaultRegion(Integer.toString(currentId));
                finish();
            }
        });

        Button selectSoundCancelButton = (Button) findViewById(R.id.select_region_cancel_button);
        selectSoundCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawList();
    }

    public void drawList() {
        if(vg.getChildCount() > 0)
            vg.removeAllViews();
        for (int i = 0; i < regions.size(); i++) {
            inflateRow(this, vg, i);
        }
    }

    private void inflateRow(final Context context, ViewGroup viewGroup, int currentPosition) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TableRow tr = (TableRow) li.inflate(R.layout.region_row, viewGroup, false);
        Region region = regions.get(currentPosition);
        tr.setId(Integer.parseInt(region.id));
        ((TextView) tr.findViewById(R.id.region_name)).setText(region.name + " " + region.id);
        if(region.id.equals(currentId))
            vg.findViewById(currentId).setBackgroundColor(Color.GRAY);

//        if(region.id == currentRegion.id)
//            vg.findViewById(currentId).setBackgroundColor(Color.GRAY);

        tr.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentId != 0) {
                    vg.findViewById(currentId).setBackgroundColor(Const.getDefaultBGColor(context));
                }
                currentId = v.getId();
                vg.findViewById(currentId).setBackgroundColor(Color.GRAY);
            }
        });
        viewGroup.addView(tr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_region, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            new RegionsRequest(new RegionsCallback(), this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RegionsCallback implements AsyncTaskCompleteListener {
        @Override
        public void onTaskComplete(JSONObject result) {
            if (!result.has("error")) {
                myApp.setRegions(result);
                drawList();
            }
        }
    }
}
