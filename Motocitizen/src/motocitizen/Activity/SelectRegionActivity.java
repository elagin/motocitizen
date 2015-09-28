package motocitizen.Activity;

import android.content.Context;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
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

import java.util.ArrayList;
import java.util.List;

import motocitizen.main.R;
import motocitizen.startup.Preferences;
import motocitizen.utils.Const;
import motocitizen.utils.MyUtils;

public class SelectRegionActivity extends ActionBarActivity {

    public class Region {
        public String name;
        public String id;
        public double lon;
        public double lat;
    }

    private static List<Region> notifications;
    private static int currentId;
    private static Region currentRegion;
    private static ViewGroup vg;
    private static String currentTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_region);

        notifications = new ArrayList<>();

        Region region = new Region();
        region.name = "Moscow";
        region.id = "77";
        notifications.add(region);

        Region region2 = new Region();
        region2.name = "Spb";
        region2.id = "99";
        notifications.add(region2);

        vg = (ViewGroup) findViewById(R.id.region_select_table);

        Button selectSoundConfirmButton = (Button) findViewById(R.id.select_region_save_button);
        selectSoundConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.setDefaultRegion(currentRegion.id);
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
        drawList(this);
    }

    private void drawList(Context context) {
        for (int i = 0; i < notifications.size(); i++) {
            inflateRow(context, vg, i);
        }
    }

    private void inflateRow(final Context context, ViewGroup viewGroup, int currentPosition) {
        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TableRow tr = (TableRow) li.inflate(R.layout.region_row, viewGroup, false);
        //tr.setId(MyUtils.newId());
        tr.setId(currentPosition);
        ((TextView) tr.findViewById(R.id.region_name)).setText(notifications.get(currentPosition).name);
        ((TextView) tr.findViewById(R.id.region_id)).setText(notifications.get(currentPosition).id);
        //notifications.put(tr.getId(), rm.getRingtoneUri(currentPosition));
        tr.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currentId != 0) {
                    vg.findViewById(currentId).setBackgroundColor(Const.getDefaultBGColor(context));
                }
                currentId = v.getId();
                vg.findViewById(currentId).setBackgroundColor(Color.GRAY);
                currentRegion = notifications.get(v.getId());
                int a = 0;
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
