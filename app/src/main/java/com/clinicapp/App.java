package com.clinicapp;

import android.app.Application;
import android.content.SharedPreferences;


public class App extends Application {

    public SharedPreferences preferences;

    public SharedPreferences getSharedPrefs(){
        if(preferences == null){
            preferences = getSharedPreferences( getPackageName() + "_preferences", MODE_PRIVATE);
        }
        return preferences;
    }
}
