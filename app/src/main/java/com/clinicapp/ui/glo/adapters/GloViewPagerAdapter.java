package com.clinicapp.ui.glo.adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.clinicapp.models.Patient;
import com.clinicapp.ui.glo.fragments.GloPatientsResultFragment;
import com.clinicapp.ui.home.PatientsResultFragment;

import java.util.List;


public class GloViewPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter{

    private List<Patient> patients;

    public GloViewPagerAdapter(@NonNull Fragment fragment, List<Patient> patients) {
        super(fragment);
        this.patients = patients;
    }



    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return GloPatientsResultFragment.getInstance(patients.get(position));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }
}
