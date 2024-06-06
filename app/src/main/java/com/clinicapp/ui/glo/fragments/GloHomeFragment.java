package com.clinicapp.ui.glo.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.clinicapp.R;
import com.clinicapp.databinding.FragmentGloHomeBinding;
import com.clinicapp.databinding.FragmentHomeBinding;
import com.clinicapp.providers.Repository;
import com.clinicapp.ui.glo.splash.SplashActivity;
import com.clinicapp.ui.home.SearchPatientsFragment;
import com.clinicapp.ui.launch.LaunchActivity;

public class GloHomeFragment extends Fragment {
private FragmentGloHomeBinding views;
    private Repository repository;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        views = FragmentGloHomeBinding.inflate(getLayoutInflater(),container,false);
        return  views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        repository = Repository.getInstance(getActivity().getApplication());

        views.newPatient.setOnClickListener(v ->
                {
                  //  Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_newPatientFragment);
                    GloNewPatientFragment returnToGloHomeFragment = new GloNewPatientFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();
                }
        );
        views.existingPatient.setOnClickListener(v ->
                {
               //     Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_searchPatientFragment);
                    GloSearchPatientsFragment returnToGloHomeFragment = new GloSearchPatientsFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();
                }
        );
        views.tvLogout.setOnClickListener(view ->
                {
                    repository.prefs.setToken("");

                    startActivity(new Intent(getActivity(), SplashActivity.class));
                    getActivity().finish();

                }
        );
        views.logo.setOnClickListener(view ->{
            startActivity(new Intent(getActivity(), SplashActivity.class));
            getActivity().finish();
        });
    }
}