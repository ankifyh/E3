package com.mj.e3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProviders;

import com.mj.e3.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static public ActivityMainBinding binding;
    HomeVM homeVm;
    static TextToSpeech textToSpeech;
    static public SharedPreferences sharedPreferences;
    static public SharedPreferences.Editor editor;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //制造存储数据的两个关键东东!
        sharedPreferences =this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        homeVm = ViewModelProviders.of(this, new SavedStateViewModelFactory(getApplication(), this)).get(HomeVM.class);
        homeVm.context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setData(homeVm);
        binding.setLifecycleOwner(this);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                            && result != TextToSpeech.LANG_AVAILABLE) {
                        Toast.makeText(MainActivity.this, "TTS暂时不支持这种语音的朗读！",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    static public void play(String str,int queueMode) {
        if (!TextUtils.isEmpty(str)) {
            // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            textToSpeech.setPitch(0.5f);
            // 设置语速
            textToSpeech.setSpeechRate(1.0f);
            //播放语音
            textToSpeech.speak(str, queueMode, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
