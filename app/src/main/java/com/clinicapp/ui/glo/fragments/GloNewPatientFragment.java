package com.clinicapp.ui.glo.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.clinicapp.R;
import com.clinicapp.databinding.FragmentGloNewPatientBinding;
import com.clinicapp.models.AddPatientResponse;
import com.clinicapp.models.FaceAnalysisResponse;
import com.clinicapp.ui.glo.viewmodel.GloPositionViewModel;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.ui.home.viewmodels.NewPatientViewModel;
import com.clinicapp.utilities.BaseFragment;
import com.clinicapp.utilities.DateTextWatcher;
import com.clinicapp.utilities.PostTextWatcher;
import com.clinicapp.utilities.Utils;

import java.util.Calendar;

import kotlin.Pair;

public class GloNewPatientFragment extends BaseFragment {
    private FragmentGloNewPatientBinding views;
    private NewPatientViewModel viewModel;
    private MainViewModel mainViewModel;
    private static final String TAG = "NewPatientFragment";
    private GloPositionViewModel gloViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        views = FragmentGloNewPatientBinding.inflate(getLayoutInflater(), container, false);
        views.form.txtTitle.setText(R.string.create_new_patient);
        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NewPatientViewModel.class);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        gloViewModel = new ViewModelProvider(this).get(GloPositionViewModel.class);
        init();
    }

    private void init() {
        new PostTextWatcher(views.form.firstname, this::handleFirstNameChange);
        new PostTextWatcher(views.form.lastname, this::handleLastNameChange);
        new PostTextWatcher(views.form.phone, this::handlePhoneChange);
        new DateTextWatcher(views.form.birthdate, this::handleDateChange);

        views.form.gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                final String gender = views.form.radioMale.isChecked() ? "M" : "F";
                handleGenderChange(views.form.gender.getCheckedRadioButtonId() == -1 ? "" : gender);
            }
        });

        views.form.dob.setOnClickListener(this::showDatePicker);

        views.addPatient.setOnClickListener(this::addPatient);

        viewModel.getApiLiveData()
                .observe(getViewLifecycleOwner(), result -> {
                    setViewState(!result.isLoading(), views.form.firstname, views.form.lastname,
                            views.form.gender, views.form.dob, views.form.phone);
                    if (result.isError()) {
                        handleApiError(result.value, result.message);
                    } else if (result.isSuccess() && result.value.getStatus()) {
                        handleSuccessApiResponse();
                    }
                });

        gloViewModel.getApiLiveData().observe(getViewLifecycleOwner(), result -> {
            if (result.isError()) {
                swapButtonAndLoader(false);

                Utils.notify(getActivity(), result.message);
            } else if (result.isSuccess() && result.isFresh()) {
                //Use pop as we want to act on the result only when it's fresh.
                final FaceAnalysisResponse analysisResponse = result.pop();
                handleApiResponse(analysisResponse);
            }
        });

    }

    private void handleSuccessApiResponse() {
        mainViewModel.onPatientAdded(viewModel.getPatient());
        mainViewModel.setSelectedPatient(viewModel.getPatient());
        final boolean canProceed = gloViewModel.setFacePositions(false,
                false, true);
        if (canProceed) {
            gloViewModel.createAnalysisID(viewModel.getPatient().getId());
        } else {
            swapButtonAndLoader(false);
            Utils.notify(getContext(), "Please select at least one position.");
        }
//        GloPositionFragment returnToGloHomeFragment = new GloPositionFragment();
//        getActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();
//
//        Navigation.findNavController(views.getRoot())
//                .navigate(R.id.action_newPatientFragment_to_selectShootPosition);
    }

    private void handleApiResponse(FaceAnalysisResponse result) {
        mainViewModel.removeFaceDataParts();
        Long analysisId = result.getFaceAnalysisID();
        mainViewModel.setFaceAnalysisID(analysisId);
        gloViewModel.setFaceCloseupPositions();
        startCameraFragment();
    }

    private void startCameraFragment() {
        if (Utils.checkCameraPermission(getContext())) {
            mainViewModel.removeFaceDataParts();
            mainViewModel.setSelectedFacePositions(gloViewModel.getSelectedFacePositions());
            mainViewModel.setSelectedCloseupFacePositions(gloViewModel.getSelectedCloseupFacePositions());
            GloFragment returnToGloHomeFragment = new GloFragment();
            Bundle bundle = new Bundle();
            bundle.putString("FROM", "global");
            bundle.putString("IS_FROM_START","true");
            returnToGloHomeFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();
        } else {
            swapButtonAndLoader(false);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void handleApiError(AddPatientResponse response, String message) {
        swapButtonAndLoader(false);
        if (!response.showFieldError()) {
            Utils.notify(getActivity(), message);
        } else {
            showError(views.form.firstnameErr, response.getFirstNameError());
            showError(views.form.lastnameErr, response.getLastNameError());
            showError(views.form.phoneErr, response.getPhoneError());
            showError(views.form.genderErr, response.getGenderError());
            showError(views.form.dobErr, response.getDobError());
        }
    }


    private void swapButtonAndLoader(boolean isLoading) {
        views.progressBar.setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
        views.addPatient.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }


    private boolean handleFirstNameChange(String value) {
        final Pair<Boolean, String> valResult = viewModel.validateFirstName(value);
        return handleValidation(views.form.firstnameErr, valResult);
    }

    private boolean handleLastNameChange(String s) {
        final Pair<Boolean, String> valResult = viewModel.validateLastName(s);
        return handleValidation(views.form.lastnameErr, valResult);
    }

    private boolean handlePhoneChange(String s) {
        final Pair<Boolean, String> valResult = viewModel.validatePhone(s);
        return handleValidation(views.form.phoneErr, valResult);
    }

    private boolean handleDateChange(String value) {
        final Pair<Boolean, String> valResult = viewModel.validateDoB(value);
        return handleValidation(views.form.dobErr, valResult);
    }

    private boolean handleGenderChange(String value) {
        final Pair<Boolean, String> valResult = viewModel.validateGender(value);
        return handleValidation(views.form.genderErr, valResult);
    }

    private void showDatePicker(View view) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        new DatePickerDialog(getActivity(), (datePicker, year1, month1, day) -> {
            month1++;
            String dt = day < 10 ? "0" + day : "" + day;
            String monthOfYear = month1 < 10 ? "0" + month1 : month1 + "";
            views.form.birthdate.setText(monthOfYear + "/" + dt + "/" + year1);
        }, year, month, date).show();

        handleDateChange(views.form.birthdate.getText().toString());
    }

    private void addPatient(View view) {
        final String firstName = views.form.firstname.getText().toString();
        final String lastName = views.form.lastname.getText().toString();
        final String gender = views.form.radioMale.isChecked() ? "M" : "F";
        final String dateOfBirth = views.form.birthdate.getText().toString();

        final String phone = views.form.phone.getText().toString();

        final boolean isValidFirstName = handleFirstNameChange(firstName);
        final boolean isValidLastName = handleLastNameChange(lastName);
        final boolean isValidGender = handleGenderChange(views.form.gender.getCheckedRadioButtonId() == -1 ? "" : gender);
        final boolean isValidDob = handleDateChange(dateOfBirth);

        if (phone.length() > 0) {
            final boolean isValidPhone = handlePhoneChange(phone);
            if (isValidFirstName && isValidLastName && isValidGender && isValidPhone && isValidDob) {
                String[] arr = dateOfBirth.split("/");
                final String newDOB = arr[2] + "-" + arr[0] + "-" + arr[1];
                swapButtonAndLoader(true);
                viewModel.addPatient(firstName, lastName, newDOB, gender, phone);
            }
        } else {
            if (isValidFirstName && isValidLastName && isValidGender && isValidDob) {
                String[] arr = dateOfBirth.split("/");
                final String newDOB = arr[2] + "-" + arr[0] + "-" + arr[1];
                swapButtonAndLoader(true);
                viewModel.addPatient(firstName, lastName, newDOB, gender, phone);
            }
        }

    }


}