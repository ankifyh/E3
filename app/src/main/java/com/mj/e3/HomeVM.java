package com.mj.e3;

import android.app.Application;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import static com.mj.e3.HomeFragment.checkedWord;

public class HomeVM extends AndroidViewModel {
    Context context;
    static MutableLiveData<Integer> tipTimes = new MutableLiveData<>(1);
    static MutableLiveData<String> nextWord = new MutableLiveData<>();
    static MutableLiveData<Integer> litterIndex = new MutableLiveData<>(0);
    static String newText;

    public HomeVM(@NonNull Application application) {
        super(application);
    }



    //被首页的设文按钮调用,然后跳转到设文页面
    public void toSetWenPage(@NonNull View v) {
        NavController navController = Navigation.findNavController(v);
        navController.navigate(R.id.action_homeFragment_to_setWenFragment);
    }

    //跳去设置页
    public void toSetPage(@NonNull View v) {
        NavController navController = Navigation.findNavController(v);
        navController.navigate(R.id.action_homeFragment_to_setFragment);
    }
    public MutableLiveData<Integer> getTipTimes() {
        return tipTimes;
    }
    public MutableLiveData<Integer> getLitterIndex() {
        return litterIndex;
    }

    public MutableLiveData<String> getWord() {
        if (nextWord == null) {
            nextWord = new MutableLiveData<>();
            nextWord.setValue(checkedWord);
        }
        return nextWord;
    }

}
