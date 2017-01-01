package motocitizen.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class Favorites {
    public static List<Integer> getFavorites() {
        List<Integer>  result = new ArrayList<>();
        SQLiteDatabase db     = DbOpenHelper.getInstance().getReadableDatabase();
        Cursor         cursor = db.rawQuery("SELECT acc_id FROM favorites", new String[]{});
        while (cursor.moveToNext()) {
            result.add(cursor.getInt(0));
        }
        cursor.close();
        db.close();
        return result;
    }

    public static void setFavorite(int accidentId) {
        SQLiteDatabase db            = DbOpenHelper.getInstance().getWritableDatabase();
        ContentValues  contentValues = new ContentValues();
        contentValues.put("acc_id", accidentId);
        db.insert("messages", null, contentValues);
        db.close();
    }
}
