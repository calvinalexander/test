package com.clinicapp.ui.glo.splash;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.clinicapp.models.LoginRequest;
import com.clinicapp.models.LoginResponse;
import com.clinicapp.providers.AsyncResponse;
import com.clinicapp.providers.Repository;
import com.clinicapp.ui.auth.LoginActivity;
import com.clinicapp.ui.glo.home.GloMainActivity;
import com.clinicapp.ui.glo.login.GloLoginActivity;
import com.clinicapp.ui.home.MainActivity;
import com.clinicapp.utilities.BaseViewModel;

import kotlin.Pair;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashViewModel extends BaseViewModel {

    private MutableLiveData<AsyncResponse<Class, Exception>> authStateLiveData;

    public SplashViewModel(@NonNull Application application) {
        super(application);
        authStateLiveData = new MutableLiveData<>(AsyncResponse.notStarted());
    }

    public LiveData<AsyncResponse<Class, Exception>> getAuthState() {
        return authStateLiveData;
    }

    public boolean checkAuthState() {
        return repository.prefs.isLoggedIn();
    }
}
