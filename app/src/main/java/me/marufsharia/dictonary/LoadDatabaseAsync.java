package me.marufsharia.dictonary;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;


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

        databaseHelper = new DatabaseHelper(context);

        try {
            databaseHelper.createDatabase();
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
