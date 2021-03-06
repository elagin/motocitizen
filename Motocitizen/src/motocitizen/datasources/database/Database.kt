package motocitizen.datasources.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import motocitizen.MyApp

object Database {
    private const val VERSION = 3
    private const val DATABASE = "motodtp"

    val db: SQLiteOpenHelper by lazy { Helper(MyApp.context) }

    class Helper(context: Context) : SQLiteOpenHelper(context, DATABASE, null, VERSION) {
        override fun onCreate(db: SQLiteDatabase) = createSchema(db)

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = createSchema(db)

        private fun createSchema(db: SQLiteDatabase) {
            val createTableMessages = "CREATE TABLE IF NOT EXISTS messages (acc_id int, msg_id int)"
            val createTableFavorites = "CREATE TABLE IF NOT EXISTS favorites (acc_id int)"
            db.execSQL(createTableMessages)
            db.execSQL(createTableFavorites)
        }
    }
}
