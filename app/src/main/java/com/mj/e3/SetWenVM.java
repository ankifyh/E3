package com.mj.e3;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class SetWenVM extends AndroidViewModel {
    public SetWenVM(@NonNull Application application) {
        super(application);
    }

    //返回首页,将会被其它页面的返回按键调用
    public void toHomePage(@NonNull View v) {
        @NonNull NavController navController = Navigation.findNavController(v);
        navController.navigate(R.id.homeFragment);
    }
}
