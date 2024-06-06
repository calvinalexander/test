package com.clinicapp.ui.glo.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.clinicapp.R;
import com.clinicapp.databinding.FragmentReturnGloHomeBinding;
import com.clinicapp.ui.glo.viewmodel.GloFinishViewModel;
import com.clinicapp.ui.home.HomeFragment;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseFragment;

import org.jetbrains.annotations.NotNull;

public class ReturnToGloHomeFragment extends BaseFragment {
    private FragmentReturnGloHomeBinding views;
    private GloFinishViewModel viewModel;
    private MainViewModel mainViewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        views = FragmentReturnGloHomeBinding.inflate(getLayoutInflater(), container, false);

        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(GloFinishViewModel.class);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        viewModel.init(getArguments(), mainViewModel);

//        if(getArguments().getBoolean("isEnd")){
        if (true) {
            views.progressCircular.setVisibility(View.VISIBLE);
            views.tvUpload.setVisibility(View.VISIBLE);
            views.btHome.setVisibility(View.GONE);
            viewModel.uploadMultipleImage(0);
//            views.btHome.setVisibility(View.VISIBLE);
        } else {
            if (!getArguments().getBoolean("isPortrait")) {
                //   views.headPositions.setVisibility(View.GONE);
                //  views.btHome.setText(R.string.finished);
            }
        }
//            views.progressCircular.setVisibility(View.VISIBLE);
//            views.tvUpload.setVisibility(View.VISIBLE);
//            views.btHome.setVisibility(View.GONE);
//            viewModel.uploadMultipleImage(0);
//           // views.btHome.setVisibility(View.VISIBLE);
//        }
//        else{
//            if (!getArguments().getBoolean("isPortrait")) {
//                //   views.headPositions.setVisibility(View.GONE);
//                //  views.btHome.setText(R.string.finished);
//            }
//        }
        initViews();

    }

    private void initViews() {
//        views.btHome.setOnClickListener(view -> {
//            views.progressCircular.setVisibility(View.VISIBLE);
//            views.tvUpload.setVisibility(View.VISIBLE);
//           // views.headPositions.setVisibility(View.GONE);
//            views.btHome.setVisibility(View.GONE);
////            viewModel.uploadMultipleImage(0);
////            Intent intent = new Intent(getActivity(), GloMainActivity.class);
////            startActivity(intent);
//
//        });
//        views.btHome.setOnClickListener(view -> {
//            views.progressCircular.setVisibility(View.VISIBLE);
//            views.tvUpload.setVisibility(View.VISIBLE);
//            //views.headPositions.setVisibility(View.GONE);
//            views.btHome.setVisibility(View.GONE);
//            viewModel.uploadMultipleImage(0);
//        });
        viewModel.getUploadStatusLiveData().observe(getViewLifecycleOwner(), this::handleUploadResponse);
//        views.headPositions.setOnClickListener(view -> Navigation.findNavController(view)
//                .navigate(R.id.action_returnToHomeScreen_to_selectHairPosition, getArguments()));

       views.btHome.setOnClickListener(view -> Navigation.findNavController(view)
                .navigate(R.id.action_returnToHomeScreen_to_homeFragment));

    }

    private void handleUploadResponse(String response) {
        if (response.equals("UploadDone")) {
            mainViewModel.removeFaceDataParts();
          //  views.btHome.performClick();
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.nav_host_fragment, new GloHomeFragment());
            ft.commit();

//            Navigation.findNavController(views.getRoot())
//                .navigate(R.id.action_returnToHomeScreen_to_homeFragment);

//            views.btHome.setVisibility(View.VISIBLE);
//            Navigation.findNavController(views.getRoot())
//                    .navigate(R.id.action_returnToHomeScreen_to_homeFragment);
        }
    }

}