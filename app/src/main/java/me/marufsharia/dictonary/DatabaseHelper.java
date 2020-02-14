package me.marufsharia.dictonary;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private String DB_PATH = null;

    private static String DB_NAME = "eng_dictionary.db";

    private SQLiteDatabase myDatabase;

    private final Context context;


    public DatabaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.context = context;
        this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        Log.d("DB_PATH", DB_PATH);
        Log.d("test", "fConstructor call");
    }


    public void createDatabase() {

        boolean dbExist = checkDatabase();

        if (!dbExist) {

            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch (Exception e) {
                throw new Error("Error Copying Database");
            }

        }

    }


    public boolean checkDatabase() {
//        String myPath = DB_PATH + DB_NAME;
//        File file = new File(myPath);
//        if(file.exists()){
//            Log.d("test","file  exist");
//            return true;
//        }
//        else{
//            Log.d("test","file not exist");
//            return false;
//        }


        SQLiteDatabase checkDB;
        checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            Log.d("test", "file  exist");
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.d("test", "file not exist" + e);
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;


    }


    //Copy the database from assets
    public void copyDatabase()  {
//        try {
//            InputStream mInput = context.getAssets().open(DB_NAME);
//            String outFileName = DB_PATH + DB_NAME;
//            OutputStream mOutput = new FileOutputStream(outFileName);
//            byte[] mBuffer = new byte[1024];
//            int mLength;
//            while ((mLength = mInput.read(mBuffer)) > 0) {
//                mOutput.write(mBuffer, 0, mLength);
//            }
//            mOutput.flush();
//            mOutput.close();
//            mInput.close();
//        } catch (IOException mIOException) {
//            Log.i(TAG, "copyDataBase " + mIOException + "");
//
//        }
//        String DB_PATH = context.getDatabasePath(DB_NAME).getPath();
//
//        try {
//            // Open your local db as the input stream
//            InputStream myInput = context.getAssets().open(DB_NAME);
//
//
//            // Path to the just created empty db
//
//            // Open the empty db as the output stream
//            OutputStream myOutput = new FileOutputStream(DB_PATH);
//
//            // transfer bytes from the input file to the output file
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = myInput.read(buffer)) > 0) {
//                myOutput.write(buffer, 0, length);
//            }
//
//            // Close the streams
//            myOutput.flush();
//            myOutput.close();
//            myInput.close();
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }

    public void openDatabase() throws SQLException {
        String path = DB_PATH + DB_NAME;
        myDatabase = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE);


    }


    @Override
    public synchronized void close() {

        if (myDatabase != null) {
            myDatabase.close();
            Log.d("test","close function");
        }

        super.close();


    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        try {

            this.getReadableDatabase();
            context.deleteDatabase(DB_NAME);
            copyDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public Cursor getMeaning(String text)
    {
        Cursor c= myDatabase.rawQuery("SELECT en_definition,example,synonyms,antonyms FROM words WHERE en_word==UPPER('"+text+"')",null);
        return c;
    }

    public Cursor getSuggestions(String text)
    {
        //return myDatabase.query("words", new String[]{"_id", "en_word"}, "en_word=?", new String[]{text}, null, null, null, "10");
        return myDatabase.rawQuery("SELECT _id, en_word FROM words WHERE en_word LIKE '"+text+"%' LIMIT 40",
                null);
    }


    public void inserHistory(String text){

        myDatabase.execSQL("INSERT INTO history(word) VALUES (UPPER ('"+text+"'))");

    }

    public Cursor getHistory()
    {
        Cursor c= myDatabase.rawQuery("select distinct  word, en_definition from history h join words w on h.word==w.en_word order by h._id desc",null);
        return c;
    }


    public void  deleteHistory()
    {
        myDatabase.execSQL("DELETE  FROM history");
    }

}
