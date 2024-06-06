package com.clinicapp.ui.camera.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;

import com.clinicapp.R;
import com.clinicapp.models.CameraCaptureState;
import com.clinicapp.models.CameraMeasurementRequest;
import com.clinicapp.models.CameraMeasurementResponse;
import com.clinicapp.models.CameraPositions;
import com.clinicapp.models.Patient;
import com.clinicapp.models.UploadImageResponse;
import com.clinicapp.providers.AsyncResponse;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseViewModel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinishViewModel extends BaseViewModel {

    Bundle data;
    MainViewModel mainViewModel;
    private MutableLiveData<String> uploadResponse;

    public FinishViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(@NonNull Bundle data, MainViewModel mainViewModel) {
        this.data = data;
        this.mainViewModel = mainViewModel;
        uploadResponse = new MutableLiveData<>("");
        Log.e("=======>", mainViewModel.getImagesList().size() + "");
    }

    public LiveData<String> getUploadStatusLiveData() {
        return uploadResponse;
    }

    public void uploadMultipleImage(int pos) {
        if (mainViewModel.getAlSubType().size() > 0) {
            repository.api.uploadImage(mainViewModel.getAlMainType().get(pos), mainViewModel.getAlSubType().get(pos), mainViewModel.getAlImg().get(pos), mainViewModel.getAlAnalysisID().get(pos))
                    .enqueue(new Callback<UploadImageResponse>() {
                        @Override
                        public void onResponse(Call<UploadImageResponse> call, Response<UploadImageResponse> response) {
                            if (!response.isSuccessful()) {
                                onFailure(call, new Exception("Failed to upload image. Please check your connection."));
                            } else {

                                UploadImageResponse imageResponse = response.body();
                                Log.e("=======>", imageResponse.toString() + "");
                                if (pos < mainViewModel.getAlMainType().size() - 1) {
                                    uploadMultipleImage(pos + 1);
                                } else {
                                    uploadResponse.setValue("UploadDone");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<UploadImageResponse> call, Throwable t) {
                            uploadResponse.setValue("UploadFailed");
                        }
                    });
        }
        else{
            uploadResponse.setValue("UploadDone");
        }

    }

}