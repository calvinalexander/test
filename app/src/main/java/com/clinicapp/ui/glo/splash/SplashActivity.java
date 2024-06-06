package com.clinicapp.ui.glo.splash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.clinicapp.databinding.ActivitySplashBinding;
import com.clinicapp.ui.auth.LoginActivity;
import com.clinicapp.ui.glo.home.GloMainActivity;
import com.clinicapp.ui.glo.login.GloLoginActivity;
import com.clinicapp.ui.home.MainActivity;
import com.clinicapp.utilities.Utils;


public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding views;
    private SplashViewModel viewModel;
    boolean isLogin = false;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Utils.removeStatusBar(SplashActivity.this);
        views = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());
        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        isLogin = viewModel.checkAuthState();
    }

    public void onClickGroTrack(View v) {
        Intent intent;
        if (isLogin) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
    }

    public void onClickGloTrack(View v) {
        Intent intent;
        if (isLogin) {
            intent = new Intent(this, GloMainActivity.class);
        } else {
            intent = new Intent(this, GloLoginActivity.class);
        }
        startActivity(intent);
    }

}
