package motocitizen.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import motocitizen.content.Region;

public class Regions {
    public static List<Region> getRegions(Context context) {
        List<Region> result = new ArrayList<>();
        DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, lat, lon FROM regions", new String[]{});
        while (cursor.moveToNext()) {
            Region region = new Region();
            region.id = cursor.getString(0);
            region.name = cursor.getString(1);
            region.lat = cursor.getDouble(2);
            region.lon = cursor.getDouble(3);
            result.add(region);
        }
        cursor.close();
        db.close();
        return result;
    }

    public static void setRegions(Context context, List<Region> regions) {
        DbOpenHelper dbOpenHelper = new DbOpenHelper(context);
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        db.delete("regions", null, null);

        for (int i = 0; i < regions.size(); i++) {
            Region region = regions.get(i);
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", region.id);
            contentValues.put("name", region.name);
            contentValues.put("lat", region.lat);
            contentValues.put("lon", region.lon);
            db.insert("regions", null, contentValues);
        }
        db.close();
    }
}
