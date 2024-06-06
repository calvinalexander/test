package com.clinicapp.ui.glo.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.clinicapp.R;
import com.clinicapp.databinding.FragmentFinishBinding;
import com.clinicapp.ui.glo.home.GloMainActivity;
import com.clinicapp.ui.glo.viewmodel.GloFinishViewModel;
import com.clinicapp.ui.home.viewmodels.MainViewModel;

import org.jetbrains.annotations.NotNull;

public class FinishFragment extends Fragment {

    private FragmentFinishBinding views;
    private GloFinishViewModel viewModel;
    private MainViewModel mainViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        views = FragmentFinishBinding.inflate(getLayoutInflater(), container, false);
        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(GloFinishViewModel.class);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        viewModel.init(getArguments(), mainViewModel);
        initViews();

    }

    private void initViews() {
        if (getArguments().getString("FROM").equals("global")) {
            views.btContinue.setVisibility(View.VISIBLE);
            views.btHome.setVisibility(View.VISIBLE);
        } else {
            views.btContinue.setVisibility(View.GONE);
            views.btHome.setVisibility(View.VISIBLE);
        }

        views.btContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GloFragment returnToGloHomeFragment = new GloFragment();
                Bundle bundle = new Bundle();
                bundle.putString("FROM", "closeup");
                bundle.putString("IS_FROM_START","false");
                returnToGloHomeFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();
            }
        });

        views.btHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                views.tvUpload.setVisibility(View.VISIBLE);
                views.btHome.setVisibility(View.GONE);
                views.btContinue.setVisibility(View.GONE);
                views.progressCircular.setVisibility(View.VISIBLE);
                viewModel.uploadMultipleImage(0);
            }
        });
        viewModel.getUploadStatusLiveData().observe(getViewLifecycleOwner(), this::handleUploadResponse);
    }

    private void handleUploadResponse(String response) {
        if (response.equals("UploadDone")) {
            mainViewModel.removeFaceDataParts();
            Intent mStartActivity = new Intent(getActivity(), GloMainActivity.class);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(getContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
            System.exit(0);

//            getActivity().finish();
//            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            FragmentTransaction ft = getFragmentManager().beginTransaction();
//            ft.replace(R.id.nav_host_fragment, new GloHomeFragment());
//            ft.commit();
        }
    }

}