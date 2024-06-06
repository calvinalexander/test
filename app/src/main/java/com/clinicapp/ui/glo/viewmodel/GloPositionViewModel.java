package com.clinicapp.ui.glo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.clinicapp.models.CameraPositions;
import com.clinicapp.models.FaceAnalysisRequest;
import com.clinicapp.models.FaceAnalysisResponse;
import com.clinicapp.providers.AsyncResponse;
import com.clinicapp.utilities.BaseViewModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GloPositionViewModel extends BaseViewModel {
    private ArrayList<CameraPositions> positions = new ArrayList<>();
    private ArrayList<CameraPositions> closeupPositions = new ArrayList<>();

    private MutableLiveData<AsyncResponse<FaceAnalysisResponse, Exception>> apiLiveData;

    public GloPositionViewModel(@NonNull Application application) {
        super(application);
    }

    public boolean setFacePositions(boolean hasRight, boolean hasLeft, boolean hasFront) {
        ArrayList<Integer> selectedPositions = new ArrayList<>();
        selectedPositions.add(CameraPositions.Positions.FRONTAL);
        selectedPositions.add(CameraPositions.Positions.RIGHT);
        selectedPositions.add(CameraPositions.Positions.LEFT);
        positions = CameraPositions.getCameraPositionArray(selectedPositions);
        return !selectedPositions.isEmpty();
    }

    public boolean setFaceCloseupPositions() {
        ArrayList<Integer> selectedPositions = new ArrayList<>();
        selectedPositions.add(CameraPositions.Positions.FACE_FRONT_MID_HEAD);
        selectedPositions.add(CameraPositions.Positions.FACE_FRONT_RIGHT_HEAD);
        selectedPositions.add(CameraPositions.Positions.FACE_FRONT_LEFT_HEAD);
        selectedPositions.add(CameraPositions.Positions.FACE_FRONT_UNDER_RIGHT_EYE);
        selectedPositions.add(CameraPositions.Positions.FACE_FRONT_UNDER_LEFT_EYE);
        selectedPositions.add(CameraPositions.Positions.FACE_RIGHT_UPPER_CHEEK);
        selectedPositions.add(CameraPositions.Positions.FACE_LEFT_UPPER_CHEEK);
        selectedPositions.add(CameraPositions.Positions.FACE_RIGHT_LOWER_CHEEK);
        selectedPositions.add(CameraPositions.Positions.FACE_LEFT_LOWER_CHEEK);
        selectedPositions.add(CameraPositions.Positions.FACE_CHIN);

        // Collections.sort(selectedPositions);
        closeupPositions = CameraPositions.getCameraPositionArray(selectedPositions);
        return !selectedPositions.isEmpty();
    }

    public ArrayList<CameraPositions> getSelectedFacePositions() {
        return positions;
    }
    public ArrayList<CameraPositions> getSelectedCloseupFacePositions() {
        return closeupPositions;
    }

    public void createAnalysisID(long patientId) {
        apiLiveData.postValue(AsyncResponse.loading());

        final FaceAnalysisRequest request = new FaceAnalysisRequest(patientId);

        repository.api.createFaceAnalysis(request)
                .enqueue(new Callback<FaceAnalysisResponse>() {
                    @Override
                    public void onResponse(Call<FaceAnalysisResponse> call, Response<FaceAnalysisResponse> response) {
                        if (response.isSuccessful()) {
                            FaceAnalysisResponse result = response.body();
                            apiLiveData.setValue(AsyncResponse.success(result));
                        } else {
                            onFailure(call, new Exception(response.message()));
                        }
                    }

                    @Override
                    public void onFailure(Call<FaceAnalysisResponse> call, Throwable t) {
                        apiLiveData.setValue(AsyncResponse.error(t.getMessage()));
                    }
                });
    }

    public LiveData<AsyncResponse<FaceAnalysisResponse, Exception>> getApiLiveData() {
        if (apiLiveData == null) apiLiveData = new MutableLiveData<>(AsyncResponse.notStarted());
        return apiLiveData;
    }
}