package com.mj.e3;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.mj.e3.databinding.FragmentSetWenBinding;

import java.util.Objects;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.mj.e3.HomeFragment.restart;
import static com.mj.e3.MainActivity.editor;
import static com.mj.e3.MainActivity.sharedPreferences;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetWenFragment extends Fragment {
    private FragmentSetWenBinding fragmentSetWenBinding;

    public SetWenFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SetWenVM setWenVM = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SetWenVM.class);
        fragmentSetWenBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_set_wen, container, false);
        fragmentSetWenBinding.setData(setWenVM);
        fragmentSetWenBinding.setLifecycleOwner(getActivity());//委托activity来管理生命周期
        return fragmentSetWenBinding.getRoot();//返回根节点
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentSetWenBinding.btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(getString(R.string.used_text_key),fragmentSetWenBinding.editBox.getText().toString());
                Log.d(TAG, "onClick: "+fragmentSetWenBinding.editBox.getText().toString());
                editor.commit();
                Log.d(TAG, "onClick: 提交后"+sharedPreferences.getString(getString(R.string.used_text_key), "耿耿"));

                //顺便刷新一下练习页的使用文章,并且置0索引
                restart();
            }
        });
    }
}
