package com.clinicapp.ui.glo.viewmodel;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.clinicapp.models.CameraPositions;
import com.clinicapp.models.UploadFaceImageResponse;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseViewModel;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GloFinishViewModel extends BaseViewModel {

    Bundle data;
    MainViewModel mainViewModel;
    ArrayList<CameraPositions> positions;
    int count = 0;
    private MutableLiveData<String> uploadResponse;

    public GloFinishViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(@NonNull Bundle data, MainViewModel mainViewModel) {
        this.data = data;
        this.mainViewModel = mainViewModel;
        positions = mainViewModel.getSelectedFacePositions();
//        if (data.getBoolean("FRONTAL")) {
//            count += 3;
//        }
//        if (data.getBoolean("RIGHT_SIDE")) {
//            count += 2;
//        }
//        if (data.getBoolean("LEFT_SIDE")) {
//            count += 2;
//        }
//        count = 7;

        uploadResponse = new MutableLiveData<>("");
    }

    public LiveData<String> getUploadStatusLiveData() {
        return uploadResponse;
    }

    public void uploadMultipleImage(int pos) {
        if (mainViewModel.getFaceTypes().size() > 0) {
            if (pos < mainViewModel.getFaceTypes().size()) {
                if (mainViewModel.getFaceTypes().get(pos).equalsIgnoreCase("Closeup")) {
                    repository.api.uploadFaceCloseupImages(mainViewModel.getTypeListFace().get(pos),
                                    mainViewModel.getPositionListFace().get(pos),
                                    mainViewModel.getAreaListFace().get(pos),
                                    mainViewModel.getImagesListFace().get(pos),
                                    mainViewModel.getxAxisListFace().get(pos),
                                    mainViewModel.getyAxisListFace().get(pos),
                                    mainViewModel.getBrownList().get(pos),
                                    mainViewModel.getRedList().get(pos),
                                    mainViewModel.getPoresList().get(pos),
                                    mainViewModel.getFaceAnalysisID())
                            .enqueue(new Callback<UploadFaceImageResponse>() {
                                @Override
                                public void onResponse(Call<UploadFaceImageResponse> call, Response<UploadFaceImageResponse> response) {
                                    if (!response.isSuccessful()) {
                                        onFailure(call, new Exception("Failed to upload image. Please check your connection."));
                                    } else {

                                        UploadFaceImageResponse imageResponse = response.body();
//                                    Log.e("=======>", imageResponse.toString() + "");
                                        Log.e("=======>", "success " + pos);
                                        if (pos < mainViewModel.getFaceTypes().size()) {
                                            uploadMultipleImage(pos + 1);
                                        } else {
                                            uploadResponse.setValue("UploadDone");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<UploadFaceImageResponse> call, Throwable t) {
                                    uploadResponse.setValue("UploadFailed");
                                }
                            });
                } else {
                    repository.api.uploadFaceGlobalImages(mainViewModel.getTypeListFace().get(pos),
                                    mainViewModel.getPositionListFace().get(pos),
                                    mainViewModel.getImagesListFace().get(pos),
                                    mainViewModel.getxAxisListFace().get(pos),
                                    mainViewModel.getyAxisListFace().get(pos),
                                    mainViewModel.getFaceAnalysisID())
                            .enqueue(new Callback<UploadFaceImageResponse>() {
                                @Override
                                public void onResponse(Call<UploadFaceImageResponse> call, Response<UploadFaceImageResponse> response) {
                                    if (!response.isSuccessful()) {
                                        onFailure(call, new Exception("Failed to upload image. Please check your connection."));
                                    } else {

                                        UploadFaceImageResponse imageResponse = response.body();
//                                    Log.e("=======>", imageResponse.toString() + "");
                                        Log.e("=======>", "success " + pos);
                                        if (pos < mainViewModel.getFaceTypes().size()) {
                                            uploadMultipleImage(pos + 1);
                                        } else {
                                            uploadResponse.setValue("UploadDone");
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<UploadFaceImageResponse> call, Throwable t) {
                                    uploadResponse.setValue("UploadFailed");
                                }
                            });
                }
            } else {
                uploadResponse.setValue("UploadDone");
            }
        } else {
            uploadResponse.setValue("UploadDone");
        }
    }

}