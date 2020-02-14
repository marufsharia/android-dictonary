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

public class FragmentAntonyms extends Fragment {
    private TextToSpeech tts;
    private String antonyms;
    public Context context;

    public FragmentAntonyms() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_antonyms, container, false);//Inflate Layout

         context = getActivity();
        TextView text = view.findViewById(R.id.textview_a);
        ImageButton imageButton = view.findViewById(R.id.btnSpeak_a);
         antonyms = ((WordMeaningActivity) context).antonyms;

        if (antonyms != null && !antonyms.equals("NA")) {

            antonyms = antonyms.replaceAll(",", ",\n");
            text.setText(antonyms);
        }
        else{
            text.setText("No antonyms found");
        }
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (antonyms != null && !antonyms.equals("NA")) {
                    textToSpeech(context, antonyms);
                }

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
                        tts.stop();
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }
                } else {
                    Log.e("error", "Initialization Failed!");
                }
            }
        });
    }
}