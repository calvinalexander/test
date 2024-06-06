package com.clinicapp.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.lifecycle.ViewModelProvider;

import com.clinicapp.databinding.ActivityMainBinding;
import com.clinicapp.ui.common.WifiDialogFragment;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseActivity;
import com.clinicapp.utilities.Utils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class MainActivity extends BaseActivity {
    private ActivityMainBinding views;
    private MainViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.removeStatusBar(MainActivity.this);
        views = ActivityMainBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        setContentView(views.getRoot());
        init();
        viewModel.removeOldImages();
    }

    private void init() {
        //int Price =  (Integer.parseInt("1 "))* 10;
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.log("Main Activity Open");
    }

    @Override
    public void updateBatteryPercent(int battery) {
        views.batteryLevel.setText(battery+"%");
    }
}