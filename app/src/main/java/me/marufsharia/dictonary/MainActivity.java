package me.marufsharia.dictonary;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private String DB_PATH = null;
    private static String DB_NAME = "eng_dictionary.db";

    private static final String TAG = "MainActivity";
    SearchView search;
    static DatabaseHelper databaseHelper;
    static boolean databaseOpenned = false;

    SimpleCursorAdapter simpleCursorAdapter;

    ArrayList<History> historyList;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter historyAdapter;

    RelativeLayout emptyHistory;
    Cursor cursorHistory;

    ImageButton btnMic;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Version için destekleyici Toolbar eklenmesi işlemi.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnMic = findViewById(R.id.btnMic);
        search = findViewById(R.id.search_view);
        search.setIconifiedByDefault(false);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search.setIconified(false);
            }
        });

        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.speech_prompt));
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.speech_not_supported),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        databaseHelper = new DatabaseHelper(this);
        if (databaseHelper.checkDatabase()) {
            openDatabase();
        }
        else {
            LoadDatabaseAsync loadDatabaseAsync = new LoadDatabaseAsync(MainActivity.this);
            loadDatabaseAsync.execute();
        }

        final String[] from = new String[]{"en_word"};
        final int[] to = new int[]{R.id.suggestion_text};


        simpleCursorAdapter = new SimpleCursorAdapter(MainActivity.this,
                R.layout.suggestion_row, null, from, to, 0
        ){
            @Override
            public void changeCursor(Cursor cursor) {
                super.changeCursor(cursor);
            }
        };


        search.setSuggestionsAdapter(simpleCursorAdapter);
        search.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionClick(int position) {

                // Add clicked text to search box
                CursorAdapter ca = search.getSuggestionsAdapter();
                Cursor cursor = ca.getCursor();

                cursor.moveToPosition(position);
                String clicked_word =  cursor.getString(cursor.getColumnIndex("en_word"));
                search.setQuery(clicked_word,false);

                //search.setQuery("",false);

                search.clearFocus();
                search.setFocusable(false);

                Intent intent = new Intent(MainActivity.this, WordMeaningActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("en_word",clicked_word);
                intent.putExtras(bundle);
                startActivity(intent);

                return true;
            }

            @Override
            public boolean onSuggestionSelect(int position) {
                // Your code here
                return true;
            }
        });


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                String text =  search.getQuery().toString();
                try {
                Cursor c = databaseHelper.getMeaning(text);
                    if(c.getCount()<=0)
                    {
                        search.setQuery("",false);

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme);
                        builder.setTitle("Word Not Found");
                        builder.setMessage("Please search again");

                        String positiveText = getString(android.R.string.ok);
                        builder.setPositiveButton(positiveText,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // positive button logic
                                    }
                                });

                        String negativeText = getString(android.R.string.cancel);
                        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                search.setFocusable(true);
                                search.setIconified(false);
                                search.requestFocusFromTouch();
                            }
                        }).setNegativeButton(negativeText,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        search.clearFocus();
                                    }
                                });

                        AlertDialog dialog = builder.create();
                        // display dialog
                        dialog.show();

                    }

                    else
                    {
                        //search.setQuery("",false);
                        search.clearFocus();
                        search.setFocusable(false);

                        Intent intent = new Intent(MainActivity.this, WordMeaningActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("en_word",text);
                        intent.putExtras(bundle);
                        startActivity(intent);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                return false;
            }


            @Override
            public boolean onQueryTextChange(String s) {
                search.setIconifiedByDefault(false); //Give Suggestion list margins
                if(s.length()>0){
                    try {
                        Cursor cursorSuggestion=databaseHelper.getSuggestions(s);
                        if (cursorSuggestion.getCount() > 0) {
                            simpleCursorAdapter.changeCursor(cursorSuggestion);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }


                return false;
            }

        });


        emptyHistory = findViewById(R.id.empty_history);

        //recycler View
        recyclerView = findViewById(R.id.recycler_view_history);
        layoutManager = new LinearLayoutManager(MainActivity.this);

        recyclerView.setLayoutManager(layoutManager);

        fetch_history();


    }

    private void fetch_history()
    {
        historyList=new ArrayList<>();
        historyAdapter = new RecyclerViewAdapterHistory(this,historyList);
        recyclerView.setAdapter(historyAdapter);

        History h;

        if(databaseOpenned)
        {
            cursorHistory=databaseHelper.getHistory();
            if(cursorHistory.getCount() > 0){
                if (cursorHistory.moveToFirst()) {
                    do {
                        h= new History(cursorHistory.getString(cursorHistory.getColumnIndex("word")),cursorHistory.getString(cursorHistory.getColumnIndex("en_definition")));
                        historyList.add(h);
                    }
                    while (cursorHistory.moveToNext());
                }
                historyAdapter.notifyDataSetChanged();
            }
        }
        if (historyAdapter.getItemCount() == 0)
        {
            emptyHistory.setVisibility(View.VISIBLE);
        }
        else
        {
            emptyHistory.setVisibility(View.GONE);
        }
    }


    public static void openDatabase() {
        try {
            databaseHelper.close();
            databaseHelper.openDatabase();
            databaseOpenned = true;

        } catch (SQLException e) {

            e.printStackTrace();

        }

    }


    // İlgili menu xml dosyasını şişiriyoruz.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // İlgili menü seçeneklerinin seçilmesi durumunda gerçekleşecek işlemler.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;

        }

        if (id == R.id.action_exit) {
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        fetch_history();
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
           String DB_NAME = "eng_dictionary.db";
            String DB_PATH = "/data/data/" + getApplicationContext().getPackageName() + "/" + "databases/";
            try {
                in = assetManager.open(filename);
                //File outFile = new File(getExternalFilesDir(null), filename);
                //File outFile = new File(getExternalFilesDir(null), "/data/data/");
                String outFileName = DB_PATH + DB_NAME;
                 out = new FileOutputStream(outFileName);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    search.setQuery(result.get(0) ,true);
                    search.clearFocus();
                }
                break;
            }

        }

    }
}
