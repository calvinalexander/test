package com.clinicapp.ui.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.clinicapp.R;
import com.clinicapp.databinding.FragmentPatientsResultBinding;
import com.clinicapp.models.Patient;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseFragment;


public class PatientsResultFragment extends BaseFragment {
    private FragmentPatientsResultBinding views;
    private MainViewModel mainViewModel;

    private Patient patient;


    public static PatientsResultFragment getInstance(Patient patient){
        final PatientsResultFragment fragment = new PatientsResultFragment();
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
        views = FragmentPatientsResultBinding.inflate(getLayoutInflater(),container,false);

        if(patient == null){
            Navigation.findNavController(views.textView).navigateUp();
        }
        initViews();
        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
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
    }

    private void onClickPatient(View view) {
        mainViewModel.setSelectedPatient(patient);
        Navigation.findNavController(view)
                .navigate(R.id.action_resultScreen_to_selectShootPosition, getArguments());
    }
}