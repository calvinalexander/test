package com.clinicapp.ui.glo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.clinicapp.adapters.ViewPagerAdapter;
import com.clinicapp.databinding.FragmentGloSearchResultsBinding;
import com.clinicapp.databinding.FragmentSearchResultsBinding;
import com.clinicapp.ui.glo.adapters.GloViewPagerAdapter;
import com.clinicapp.ui.home.viewmodels.MainViewModel;

public class GloSearchResultsContainerFragment extends Fragment {

    private FragmentGloSearchResultsBinding views;
    private MainViewModel mainViewModel;
    private GloViewPagerAdapter viewPagerAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        views = FragmentGloSearchResultsBinding.inflate(getLayoutInflater(), container, false);

        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        initViews();
    }

    private void initViews() {
        viewPagerAdapter = new GloViewPagerAdapter(this, mainViewModel.getSearchResults());
        views.pager.setAdapter(viewPagerAdapter);
    }
}