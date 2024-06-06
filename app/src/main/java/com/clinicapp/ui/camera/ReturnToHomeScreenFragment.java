package com.clinicapp.ui.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.clinicapp.R;
import com.clinicapp.databinding.FragmentReturnOrShootHairBinding;
import com.clinicapp.models.CameraMeasurementResponse;
import com.clinicapp.models.UploadImageResponse;
import com.clinicapp.providers.AsyncResponse;
import com.clinicapp.ui.camera.viewmodel.CameraViewModel;
import com.clinicapp.ui.camera.viewmodel.FinishViewModel;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReturnToHomeScreenFragment extends BaseFragment {
    private FragmentReturnOrShootHairBinding views;
    private FinishViewModel viewModel;
    private MainViewModel mainViewModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        views = FragmentReturnOrShootHairBinding.inflate(getLayoutInflater(), container, false);

        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(getActivity()).get(FinishViewModel.class);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        viewModel.init(getArguments(), mainViewModel);



        if(getArguments().getBoolean("isEnd")){
            views.progressCircular.setVisibility(View.VISIBLE);
            views.tvUpload.setVisibility(View.VISIBLE);
            views.headPositions.setVisibility(View.GONE);
            views.btHome.setVisibility(View.GONE);

            viewModel.uploadMultipleImage(0);
        }
        else{
            if (!getArguments().getBoolean("isPortrait")) {
                views.headPositions.setVisibility(View.GONE);
                //  views.btHome.setText(R.string.finished);
            }
        }
        initViews();

    }

    private void initViews() {
        views.btHome.setOnClickListener(view -> {
            views.progressCircular.setVisibility(View.VISIBLE);
            views.tvUpload.setVisibility(View.VISIBLE);
            views.headPositions.setVisibility(View.GONE);
            views.btHome.setVisibility(View.GONE);
            viewModel.uploadMultipleImage(0);
        });
        viewModel.getUploadStatusLiveData().observe(getViewLifecycleOwner(), this::handleUploadResponse);
        Bundle bundle = new Bundle();
        bundle.putString("FROM", "closeup");
        views.headPositions.setOnClickListener(view -> Navigation.findNavController(view)
                .navigate(R.id.action_returnToHomeScreen_to_selectHairPosition, getArguments()));

       /* views.btHome.setOnClickListener(view -> Navigation.findNavController(view)
                .navigate(R.id.action_returnToHomeScreen_to_homeFragment));*/

    }

    private void handleUploadResponse(String response) {
        if (response.equals("UploadDone"))
        {
            mainViewModel.removeDataParts();
            Navigation.findNavController(views.btHome)
                    .navigate(R.id.action_returnToHomeScreen_to_homeFragment);
        }
    }

}