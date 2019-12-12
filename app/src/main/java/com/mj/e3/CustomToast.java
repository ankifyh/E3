package com.mj.e3;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public enum  CustomToast {
    @SuppressLint("StaticFieldLeak") INSTANCE;// 实现单例
    private Toast mToast;
    private TextView mTvToast;

    //使用的本类的代码示例
    // CustomToast.INSTANCE.showToast(getContext(),"hello");

    //显示1
    public void showToast(Context ctx, String content) {
        if (mToast == null) {
            mToast = new Toast(ctx);
            mToast.setGravity(Gravity.CENTER, 0, 0);//设置toast显示的位置，这是居中
            mToast.setDuration(Toast.LENGTH_SHORT);//设置toast显示的时长
            @SuppressLint("InflateParams") View view = LayoutInflater.from(ctx).inflate(R.layout.custom_toast,null);//自定义样式，自定义布局文件
            mTvToast =view.findViewById(R.id.tvCustomToast);
            mToast.setView(view);//设置自定义的view
        }
        mTvToast.setText(content);//设置文本
        mToast.show();//展示toast
    }
}