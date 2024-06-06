package com.clinicapp.ui.glo.fragments;

import android.Manifest;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.clinicapp.R;
import com.clinicapp.databinding.FragmentGloPatientsResultBinding;
import com.clinicapp.databinding.FragmentPatientsResultBinding;
import com.clinicapp.models.FaceAnalysisResponse;
import com.clinicapp.models.Patient;
import com.clinicapp.ui.glo.viewmodel.GloPositionViewModel;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseFragment;
import com.clinicapp.utilities.Utils;


public class GloPatientsResultFragment extends BaseFragment {
    private FragmentGloPatientsResultBinding views;
    private MainViewModel mainViewModel;
    private Patient patient;
    private GloPositionViewModel viewModel;


    public static GloPatientsResultFragment getInstance(Patient patient){
        final GloPatientsResultFragment fragment = new GloPatientsResultFragment();
        final Bundle bundle = new Bundle();

        bundle.putParcelable(DATA,patient);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        patient = getArguments().getParcelable(DATA);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        views = FragmentGloPatientsResultBinding.inflate(getLayoutInflater(),container,false);

        if(patient == null){
            Navigation.findNavController(views.textView).navigateUp();
        }

        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        viewModel = new ViewModelProvider(this).get(GloPositionViewModel.class);
        initViews();
    }

    private void initViews() {
        views.textView.setText(patient.getName());
        RequestOptions requestOptions=new RequestOptions().dontTransform().placeholder(R.drawable.search_avtr);

        Glide.with(this)
                .load(patient.getImage())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).apply(requestOptions)
                .into(views.imageView3);

        views.selectPatient.setOnClickListener(this::onClickPatient);

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
    private void swapButtonAndLoader(boolean isLoading) {
        views.progressBar.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
        views.selectPatient.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }
    private void onClickPatient(View view) {
        swapButtonAndLoader(true);
        mainViewModel.setSelectedPatient(patient);
        final boolean canProceed = viewModel.setFacePositions(false,
                false, true);
        viewModel.setFaceCloseupPositions();
        if (canProceed) {
            viewModel.createAnalysisID(patient.getId());
        } else {
            Utils.notify(getContext(), "Please select at least one position.");
        }
    }

    private void handleApiResponse(FaceAnalysisResponse result) {
        mainViewModel.removeFaceDataParts();
        Long analysisId = result.getFaceAnalysisID();
        mainViewModel.setFaceAnalysisID(analysisId);
        startCameraFragment();
    }


    private void startCameraFragment() {
        if (Utils.checkCameraPermission(getContext())) {
            mainViewModel.removeFaceDataParts();
            mainViewModel.setSelectedFacePositions(viewModel.getSelectedFacePositions());
            mainViewModel.setSelectedCloseupFacePositions(viewModel.getSelectedCloseupFacePositions());
            GloFragment returnToGloHomeFragment = new GloFragment();
            Bundle bundle = new Bundle();
            bundle.putString("FROM", "global");
            bundle.putString("IS_FROM_START","true");
            returnToGloHomeFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();
        } else {
            swapButtonAndLoader(false);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

}