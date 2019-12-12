package com.mj.e3;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.mj.e3.databinding.FragmentSetBinding;

import java.util.Objects;

import static com.mj.e3.HomeFragment.StingIcon;
import static com.mj.e3.MainActivity.editor;
import static com.mj.e3.MainActivity.sharedPreferences;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetFragment extends Fragment {
    @SuppressLint("StaticFieldLeak")
    private static FragmentSetBinding binding;

    public SetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        SetVM setVM = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(SetVM.class);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_set, container, false);
        binding.setData(setVM);
        binding.setLifecycleOwner(getActivity());//

        binding.editText.setText(sharedPreferences.getString(getString(R.string.emojiOfShowBox), "\uD83D\uDC64"));//加载内存里的emoji
        return binding.getRoot();
    }
    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editor.putString(getString(R.string.emojiOfShowBox),s.toString());
                editor.commit();

                //顺便刷新下当下的emoji
                StingIcon = sharedPreferences.getString(getString(R.string.emojiOfShowBox),"\uD83D\uDC8A");
                CustomToast.INSTANCE.showToast(requireActivity(), StingIcon);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

}
