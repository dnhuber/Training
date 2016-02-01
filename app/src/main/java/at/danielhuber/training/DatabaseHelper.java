package at.danielhuber.training;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by danielhuber on 17.12.15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME= "history.db";
    public static final String TABLE_NAME= "history_table";
    public static final String Col1= "ID";
    public static final String Col2= "PUSHUP";
    public static final String Col3= "PULLUP";
    public static final String Col4= "SQUAT";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT, PUSHUP INTEGER, " +
                "PULLUP INTEGER, SQUAT INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF  EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(Integer PUSHUP, Integer PULLUP, Integer SQUAT){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(Col2,PUSHUP);
        contentValues.put(Col3,PULLUP);
        contentValues.put(Col4, SQUAT);
        long result= db.insert(TABLE_NAME,null,contentValues);
        if(result==-1){
            return false;
        }
        else{
            return true;
        }
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+ TABLE_NAME ,null);
        return res;
    }
}

