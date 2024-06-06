package com.clinicapp.ui.home;

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
import com.clinicapp.databinding.FragmentHomeBinding;
import com.clinicapp.providers.Repository;
import com.clinicapp.ui.glo.splash.SplashActivity;
import com.clinicapp.ui.launch.LaunchActivity;

public class HomeFragment extends Fragment {
private FragmentHomeBinding views;
    private Repository repository;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        views = FragmentHomeBinding.inflate(getLayoutInflater(),container,false);
        return  views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        repository = Repository.getInstance(getActivity().getApplication());

        views.newPatient.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_newPatientFragment));
        views.existingPatient.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_searchPatientFragment));
        views.tvLogout.setOnClickListener(view ->
                {
                    repository.prefs.setToken("");
                    startActivity(new Intent(getActivity(), LaunchActivity.class));
                    getActivity().finish();

                }
        );
        views.logo.setOnClickListener(view ->{
            startActivity(new Intent(getActivity(), SplashActivity.class));
            getActivity().finish();
        });
    }
}