package com.clinicapp.ui.camera;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;


import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.clinicapp.R;
import com.clinicapp.databinding.FragmentPortraitImageSelectionBinding;
import com.clinicapp.models.HairAnalysisResponse;
import com.clinicapp.models.Patient;
import com.clinicapp.ui.camera.viewmodel.PositionViewModel;
import com.clinicapp.ui.home.HomeFragment;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseFragment;
import com.clinicapp.utilities.Utils;

public class PortraitPositionSelectionFragment extends BaseFragment {

    private FragmentPortraitImageSelectionBinding views;
    private Patient patient;
    private MainViewModel mainViewModel;
    private PositionViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        views = FragmentPortraitImageSelectionBinding.inflate(getLayoutInflater(),container,false);
        return views.getRoot();
    }

    @Override
    public void onViewCreated( View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PositionViewModel.class);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        patient=mainViewModel.getSelectedPatient();
        initViews();
        initListeners();
    }

    private void initViews(){
        patient = mainViewModel.getSelectedPatient();

        views.patientName.setText(patient.getName());

        if(patient.getGender()!=null&&patient.getGender().contains("F")){
            views.maleRightSideImg.setVisibility(View.GONE);
            views.maleLeftSideImg.setVisibility(View.GONE);
            views.maleTopSideImg.setVisibility(View.GONE);
            views.maleFrontSideImg.setVisibility(View.GONE);
            views.maleBackSideImg.setVisibility(View.GONE);


            views.femaleRightSideImg.setVisibility(View.VISIBLE);
            views.femaleLeftSideImg.setVisibility(View.VISIBLE);
            views.femaleTopSideImg.setVisibility(View.VISIBLE);
            views.femaleFrontSideImg.setVisibility(View.VISIBLE);
            views.femaleBackSideImg.setVisibility(View.VISIBLE);
        }
    }

    private void initListeners(){
        views.selectAllPositions.setOnCheckedChangeListener(this::onCheckAllChanged);
        views.skipAllPositions.setOnCheckedChangeListener(this::onCheckSkip);
        views.shoot.setOnClickListener(this::onClickShoot);

        viewModel.getApiLiveData().observe(getViewLifecycleOwner(),result->{
            if(result.isError()) {
                Utils.notify(getActivity(), result.message);
            } else if(result.isSuccess() && result.isFresh()){
                //Use pop as we want to act on the result only when it's fresh.
                final HairAnalysisResponse analysisResponse = result.pop();

                handleApiResponse(analysisResponse);
            }
        });
    }

    private void onCheckSkip(CompoundButton compoundButton, boolean b) {
        if (b) {
            views.selectAllPositions.setChecked(false);
            views.topSide.setChecked(false);
            views.backSide.setChecked(false);
            views.leftSide.setChecked(false);
            views.rightSide.setChecked(false);
            views.frontSide.setChecked(false);
        }
    }

    private void onCheckAllChanged(CompoundButton compoundButton, boolean b) {
        boolean allPositions = compoundButton.isChecked();
        if (b)
        {
            views.skipAllPositions.setChecked(false);
        }
        views.topSide.setChecked(allPositions);
        views.backSide.setChecked(allPositions);
        views.leftSide.setChecked(allPositions);
        views.rightSide.setChecked(allPositions);
        views.frontSide.setChecked(allPositions);
        views.selectAllPositions.setChecked(allPositions);
    }

    private void onClickShoot(View view) {
        final boolean canProceed = viewModel.setPositions(views.rightSide.isChecked(),
                views.leftSide.isChecked(),views.frontSide.isChecked(),
                views.backSide.isChecked(),views.topSide.isChecked())
                ||views.skipAllPositions.isChecked();


        if(canProceed) {
            viewModel.createAnalysisID(patient.getId());
        } else{
            Utils.notify(getContext(),"Please select at least one position.");
        }
    }

    private void startCameraFragment(){
        if (Utils.checkCameraPermission(getContext())) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("SHOW_ZOOM", false);
            mainViewModel.setCameraPositions(viewModel.getSelectedPositions());
            Navigation.findNavController(views.getRoot())
                    .navigate(R.id.action_selectShootPosition_to_cameraFragment, bundle);
        } else {
            //Request Permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        if(requestCode==0){
            if(Utils.isInternetAvailable(getContext())){
                //Start Camera after allowing the permission
                startCameraFragment();
            }else{
                Utils.notify(getContext(),"Unable to access Camera");
                getFragmentManager().beginTransaction().replace(R.id.main_layout,new HomeFragment()).commit();
            }

        }
    }

    private void handleApiResponse(HairAnalysisResponse result){
        Long analysisId=result.getHairAnalysisID();
        mainViewModel.setHairAnalysisID(analysisId);

        final boolean canSkip=views.skipAllPositions.isChecked();
        mainViewModel.removeDataParts();
        if(canSkip){
            Navigation.findNavController(views.getRoot()).navigate(R.id.action_selectShootPosition_to_selectHairPosition);
        }else{
            startCameraFragment();
        }
    }
}