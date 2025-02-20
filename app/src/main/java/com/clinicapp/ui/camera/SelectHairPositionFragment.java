package com.clinicapp.ui.camera;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.clinicapp.R;
import com.clinicapp.databinding.FragmentSelectHairPositionBinding;
import com.clinicapp.models.Patient;
import com.clinicapp.ui.camera.viewmodel.PositionViewModel;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseFragment;
import com.clinicapp.utilities.Utils;

public class SelectHairPositionFragment extends BaseFragment {
    private PositionViewModel viewModel;
    private MainViewModel mainViewModel;
    private FragmentSelectHairPositionBinding views;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        views = FragmentSelectHairPositionBinding.inflate(getLayoutInflater(),container,false);
        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PositionViewModel.class);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        initViews();
        initListeners();
    }

    private void initViews() {
        final Patient patient = mainViewModel.getSelectedPatient();

        views.patientName.setText(patient.getName());

        int maleVisibility = patient.isFemale() ? View.GONE:View.VISIBLE;
        int femaleVisibility = patient.isFemale() ? View.VISIBLE:View.GONE;

        views.maleCrown.setVisibility(maleVisibility);
        views.maleRightImg.setVisibility(maleVisibility);
        views.maleLeftImg.setVisibility(maleVisibility);
        views.maleBackImg.setVisibility(maleVisibility);

        views.femaleCrown.setVisibility(femaleVisibility);
        views.femaleRightImg.setVisibility(femaleVisibility);
        views.femaleBackImg.setVisibility(femaleVisibility);
        views.femaleLeftImg.setVisibility(femaleVisibility);
    }



    private void initListeners() {
        views.selectAllPositions.setOnCheckedChangeListener(this::handleAllButtonChecked);
        views.shoot.setOnClickListener(this::onClickShoot);
    }

    private void handleAllButtonChecked(CompoundButton compoundButton, boolean isChecked) {
        final Patient patient = mainViewModel.getSelectedPatient();
        boolean allPositions=compoundButton.isChecked();
        if(patient.isFemale()){
            views.femaleCrownTop.setChecked(allPositions);
            views.femaleCrownMiddle.setChecked(allPositions);
            views.femaleCrownBottom.setChecked(allPositions);
            views.femaleHairRight.setChecked(allPositions);
            views.femaleHairLeft.setChecked(allPositions);
            views.femaleHairBack.setChecked(allPositions);
        }else{
            views.crownTop.setChecked(allPositions);
            views.crownBottom.setChecked(allPositions);
            views.crownMiddle.setChecked(allPositions);
            views.maleHairBack.setChecked(allPositions);
            views.maleHairLeft.setChecked(allPositions);
            views.maleHairRight.setChecked(allPositions);
        }
    }

    private void onClickShoot(View view) {
        boolean crownTop = views.femaleCrownTop.isChecked() || views.crownTop.isChecked();
        boolean crownMiddle = views.femaleCrownMiddle.isChecked() || views.crownMiddle.isChecked();
        boolean crownBottom = views.femaleCrownBottom.isChecked() || views.crownBottom.isChecked();
        boolean hairRight = views.femaleHairRight.isChecked() || views.maleHairRight.isChecked();
        boolean hairLeft = views.femaleHairLeft.isChecked() || views.maleHairLeft.isChecked();
        boolean hairBack = views.femaleHairBack.isChecked() || views.maleHairBack.isChecked();

        boolean hasSelection = viewModel.setZoomPositions(crownTop, crownMiddle, crownBottom, hairRight, hairBack, hairLeft);
        if(hasSelection) {
            mainViewModel.setCameraPositions(viewModel.getSelectedPositions());
                    CameraFragment returnToCameraFragment = new CameraFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("FROM", "closeup");
                    returnToCameraFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, returnToCameraFragment).addToBackStack(null).commit();
//            Navigation.findNavController(view)
//                    .navigate(R.id.action_selectHairPosition_to_cameraFragment);
        }else {
            Utils.notify(getContext(),"Please select at least one position.");
        }
    }
}