package com.mj.e3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProviders;

import com.mj.e3.databinding.ActivityMainBinding;

import java.util.Locale;

public  class MainActivity extends AppCompatActivity {

    static public ActivityMainBinding binding;
    HomeVM homeVm;
    static TextToSpeech textToSpeech;
    static public SharedPreferences sharedPreferences;
    static public SharedPreferences.Editor editor;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //实现沉浸模式,先判断是不是安卓5.0的
        if (Build.VERSION.SDK_INT >= 21) {

            //使填充状态栏
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);

        }

        //给导航栏设置个颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.getWindow().setNavigationBarColor(getColor(R.color.导航栏颜色));
        }

        //使状态栏为透明,颜色为反转
        setStatusColor(this,true,true,R.color.绝对透明);

        //制造存储数据的两个关键东东!
        sharedPreferences =this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        homeVm = ViewModelProviders.of(this, new SavedStateViewModelFactory(getApplication(), this)).get(HomeVM.class);
        homeVm.context = this;
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setData(homeVm);
        binding.setLifecycleOwner(this);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result != TextToSpeech.LANG_COUNTRY_AVAILABLE
                        && result != TextToSpeech.LANG_AVAILABLE) {
                    Toast.makeText(MainActivity.this, "TTS暂时不支持这种语音的朗读！",
                            Toast.LENGTH_SHORT).show();
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

    //设置状态栏反色效果,在上面的Activity里进行初始化
    public static void setStatusColor(Activity activity, boolean isTranslate, boolean isDarkText, @ColorRes int bgColor) {
        //如果系统为6.0及以上，就可以使用Android自带的方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); //可有可无
            decorView.setSystemUiVisibility((isTranslate ? View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN : 0) | (isDarkText ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0));
            window.setStatusBarColor(isTranslate ? Color.TRANSPARENT : ContextCompat.getColor(activity, bgColor)); //Android5.0就可以用
        }
    }
}
