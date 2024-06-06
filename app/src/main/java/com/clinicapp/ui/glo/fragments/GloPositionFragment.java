package com.clinicapp.ui.glo.fragments;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.clinicapp.R;
import com.clinicapp.databinding.FragmentGloPositionBinding;
import com.clinicapp.models.FaceAnalysisResponse;
import com.clinicapp.models.Patient;
import com.clinicapp.ui.glo.viewmodel.GloPositionViewModel;
import com.clinicapp.ui.home.HomeFragment;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.Utils;

public class GloPositionFragment extends Fragment {

    private FragmentGloPositionBinding views;
    private Patient patient;
    private MainViewModel mainViewModel;
    private GloPositionViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        views = FragmentGloPositionBinding.inflate(getLayoutInflater(), container, false);
        return views.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GloPositionViewModel.class);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        patient = mainViewModel.getSelectedPatient();
        initViews();
        initListeners();
    }

    private void initViews() {
        patient = mainViewModel.getSelectedPatient();
        views.patientName.setText(patient.getName());

        if (patient.getGender() != null && patient.getGender().contains("F")) {
            views.maleRightSideImg.setVisibility(View.GONE);
            views.maleLeftSideImg.setVisibility(View.GONE);
            views.maleFrontSideImg.setVisibility(View.GONE);

            views.femaleRightSideImg.setVisibility(View.VISIBLE);
            views.femaleLeftSideImg.setVisibility(View.VISIBLE);
            views.femaleFrontSideImg.setVisibility(View.VISIBLE);
        }
    }

    private void initListeners() {
        views.selectAllPositions.setOnCheckedChangeListener(this::onCheckAllChanged);
        views.shoot.setOnClickListener(this::onClickShoot);

        viewModel.getApiLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result.isError()) {
                Utils.notify(getActivity(), result.message);
            } else if (result.isSuccess() && result.isFresh()) {
                //Use pop as we want to act on the result only when it's fresh.
                final FaceAnalysisResponse analysisResponse = result.pop();
                handleApiResponse(analysisResponse);
            }
        });
    }

    private void onCheckAllChanged(CompoundButton compoundButton, boolean b) {
        boolean allPositions = compoundButton.isChecked();
        views.leftSide.setChecked(allPositions);
        views.rightSide.setChecked(allPositions);
        views.frontSide.setChecked(allPositions);
        views.selectAllPositions.setChecked(allPositions);
    }

    private void onClickShoot(View view) {
        final boolean canProceed = viewModel.setFacePositions(views.rightSide.isChecked(),
                views.leftSide.isChecked(), views.frontSide.isChecked());
        if (canProceed) {
            viewModel.createAnalysisID(patient.getId());
        } else {
            Utils.notify(getContext(), "Please select at least one position.");
        }
    }

    private void startCameraFragment() {
        if (Utils.checkCameraPermission(getContext())) {
            mainViewModel.removeFaceDataParts();
            Bundle bundle = new Bundle();
            bundle.putBoolean("SHOW_ZOOM", false);
            bundle.putBoolean("FRONTAL", views.frontSide.isChecked());
            bundle.putBoolean("RIGHT_SIDE", views.rightSide.isChecked());
            bundle.putBoolean("LEFT_SIDE", views.leftSide.isChecked());
            mainViewModel.setSelectedFacePositions(viewModel.getSelectedFacePositions());
//            Navigation.findNavController(views.getRoot())
//                    .navigate(R.id., bundle);
            GloFragment returnToGloHomeFragment = new GloFragment();
            returnToGloHomeFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();

        } else {
            //Request Permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (Utils.isInternetAvailable(getContext())) {
                //Start Camera after allowing the permission
                startCameraFragment();
            } else {
                Utils.notify(getContext(), "Unable to access Camera");
                getFragmentManager().beginTransaction().replace(R.id.main_layout, new HomeFragment()).commit();
            }

        }
    }

    private void handleApiResponse(FaceAnalysisResponse result) {
        mainViewModel.removeFaceDataParts();
        Long analysisId = result.getFaceAnalysisID();
        mainViewModel.setFaceAnalysisID(analysisId);
        startCameraFragment();
    }
}