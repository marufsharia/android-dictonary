package me.marufsharia.dictonary.fragments;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import me.marufsharia.dictonary.R;
import me.marufsharia.dictonary.WordMeaningActivity;

public class FragmentsSynonyms extends Fragment {
    private TextToSpeech tts;
    private String synonyms;
    public Context context;

    public FragmentsSynonyms() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_synonyms, container, false);//Inflate Layout

        context = getActivity();

        TextView text = view.findViewById(R.id.textview_s);

        ImageButton imageButton = view.findViewById(R.id.btnSpeak_s);


        synonyms = ((WordMeaningActivity) context).synonyms;

        if (synonyms != null && !synonyms.equals("NA")) {
            synonyms = synonyms.replaceAll(",", ",\n");
            text.setText(synonyms);

        } else {
            text.setText("No synonyms found");
        }
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (synonyms != null && !synonyms.equals("NA"))
                    textToSpeech(context, synonyms);
            }
        });

        return view;
    }


    public void textToSpeech(Context context, final String text) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                } else {
                    Log.e("error", "Initialization Failed!");
                }
            }
        });
    }

}
