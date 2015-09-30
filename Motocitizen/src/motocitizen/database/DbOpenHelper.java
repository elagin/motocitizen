package motocitizen.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper {

    /* constants */
    private static final int    VERSION  = 4;
    private static final String DATABASE = "motodtp";
    /* end constants */

    public DbOpenHelper(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSchema(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createSchema(db);
    }

    private void createSchema(SQLiteDatabase db){
        try {
            String createTableMessages  = "CREATE TABLE IF NOT EXISTS messages (acc_id int, msg_id int)";
            String createTableFavorites = "CREATE TABLE IF NOT EXISTS favorites (acc_id int)";
            String createTableRegions = "CREATE TABLE IF NOT EXISTS regions (id string, name string, lat double, lon double)";
            db.execSQL(createTableMessages);
            db.execSQL(createTableFavorites);
            db.execSQL(createTableRegions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
