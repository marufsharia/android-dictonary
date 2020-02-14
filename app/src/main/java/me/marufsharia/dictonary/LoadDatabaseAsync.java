package me.marufsharia.dictonary;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class LoadDatabaseAsync extends AsyncTask<Void, Void, Boolean> {


    private Context context;
    private AlertDialog alertDialog;
    private DatabaseHelper databaseHelper;

    public LoadDatabaseAsync(Context context) {

        this.context = context;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        AlertDialog.Builder d = new AlertDialog.Builder(context, R.style.MyDialogTheme);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.alert_dialog_database_copying, null);
        d.setTitle("Loading Database");
        d.setView(dialogView);
        alertDialog = d.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        String DB_NAME = "eng_dictionary.db";
        databaseHelper = new DatabaseHelper(context);


        try {
            // databaseHelper.createDatabase();

            String DB_PATH = context.getDatabasePath(DB_NAME).getPath();
            // String DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/"+DB_NAME;

            try {
                // Open your local db as the input stream
                InputStream myInput = context.getAssets().open(DB_NAME);


                // Path to the just created empty db

                // Open the empty db as the output stream
                OutputStream myOutput = new FileOutputStream(DB_PATH);

                // transfer bytes from the input file to the output file
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }

                // Close the streams
                myOutput.flush();
                myOutput.close();
                myInput.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            throw new Error("Database was not Created");
        }
        databaseHelper.close();
        return null;

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        alertDialog.dismiss();
        MainActivity.openDatabase();
    }


}
