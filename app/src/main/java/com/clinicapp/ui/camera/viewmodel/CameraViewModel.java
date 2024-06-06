package com.clinicapp.ui.camera.viewmodel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;

import com.clinicapp.R;
import com.clinicapp.models.CameraAutoCaptureState;
import com.clinicapp.models.CameraCaptureState;
import com.clinicapp.models.CameraMeasurementRequest;
import com.clinicapp.models.CameraMeasurementResponse;
import com.clinicapp.models.CameraPositions;
import com.clinicapp.models.Patient;
import com.clinicapp.models.UploadImageResponse;
import com.clinicapp.providers.AsyncResponse;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseViewModel;
import com.clinicapp.utilities.CameraUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class  CameraViewModel extends BaseViewModel {

    private int currentPosition;
    private List<CameraPositions> positions;
    private MutableLiveData<CameraCaptureState> captureStateLiveData;
    private MutableLiveData<CameraAutoCaptureState> autoCaptureStateLiveData;
    private MutableLiveData<AsyncResponse<Boolean, Exception>> captureCompleteLiveData;
    private MutableLiveData<String> endProcess;
    private MutableLiveData<String> textGuide;
    private MutableLiveData<CameraMeasurementResponse> camData;
    List<Part> alImg = new ArrayList<>();
    List<Part> alMainType = new ArrayList<>();
    List<Part> alSubType = new ArrayList<>();
    List<Long> alAnalysisID = new ArrayList<>();
    MainViewModel mainViewModel;
    Activity activity;

    public CameraViewModel(@NonNull Application application) {
        super(application);
        captureCompleteLiveData = new MutableLiveData<>(AsyncResponse.notStarted());


    }


    public void init(Activity activity, @NonNull List<CameraPositions> positions, MainViewModel mainViewModel) {
    this.activity = activity;
        this.positions = positions;
        this.mainViewModel = mainViewModel;
        this.currentPosition = 0;
        final CameraCaptureState initialState = new CameraCaptureState(positions.get(0), false, null);
        final CameraAutoCaptureState initialAutoCaptureState = new CameraAutoCaptureState("",null);
        captureStateLiveData = new MutableLiveData<>(initialState);
        autoCaptureStateLiveData = new MutableLiveData<>(initialAutoCaptureState);
        camData = new MutableLiveData<>(null);
        endProcess = new MutableLiveData<>("");
        textGuide = new MutableLiveData<>("");
    }


    public LiveData<String> getEndProcessData() {
        return endProcess;
    }

    public LiveData<String> getTextGuideLiveData() {
        return textGuide;
    }

    public void updateTextGuide(String newTextGuide){
        textGuide.setValue(newTextGuide);
    }

    public void updateAutoCaptureState(CameraAutoCaptureState state){
        final CameraCaptureState captureState = captureStateLiveData.getValue();
        if(captureState.isPreview()){
            return;
        }
        autoCaptureStateLiveData.setValue(state);
        if (state.getTextGuide().equals("OK")){
            CameraUtils.Companion.getGetCameraProvider().unbindAll();
            updateState(positions.get(currentPosition), true,state.getPath());
        }
    }

    public LiveData<CameraAutoCaptureState> getAutoCaptureState(){
        return autoCaptureStateLiveData;
    }

    public LiveData<CameraMeasurementResponse> getCamMeasureLiveData() {
        return camData;
    }

    public LiveData<CameraCaptureState> getCaptureStateLiveData() {
        return captureStateLiveData;
    }

    public LiveData<AsyncResponse<Boolean, Exception>> getCaptureCompleteLiveData() {
        return captureCompleteLiveData;
    }

    public void onImageCaptured(String path) {
        updateState(positions.get(currentPosition), true, path);
    }

    public void retake() {
        updateState(positions.get(currentPosition), false, null);
    }

    private void updateState(CameraPositions position, boolean isPreview, String path) {
        final CameraCaptureState state = new CameraCaptureState(position, isPreview, path);
        captureStateLiveData.setValue(state);
    }

    public void onNext(long analysisID) {
        final CameraCaptureState state = captureStateLiveData.getValue();
        File file = new File(state.getPath().replace("file://", ""));

        final CameraPositions position = positions.get(currentPosition);
        Log.d("check_file", String.valueOf(file.equals(null)));
        Log.d("check_file", String.valueOf(file.exists()));
        Log.d("check_file", file.toString());


        if (position.position < 5) {
            file = resizeImage(file, 500);
        } else {
            file = resizeImageHeight(file, 3000);
        }
//        Log.d("check_file", String.valueOf(file.equals(null)));
//        Log.d("check_file", String.valueOf(file.exists()));
//        Log.d("check_file", file.toString());

        String subTypeString = position.getText().toLowerCase();
        if (subTypeString.contains("zoomed")) {
            subTypeString = subTypeString.replace(" (zoomed)", "");
        }

        RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
        Part imagePart = Part.createFormData("images[]", file.getName(), imageBody);
        Part mainType = Part.createFormData("mainType", position.getShootType());
        Part subType = Part.createFormData("subType", subTypeString);
        //imagesPath.add(file.getAbsolutePath());
        mainViewModel.addImage(file.getAbsolutePath());
        //  uploadImage(mainType, subType, imagePart, analysisID);
        saveUploadImage(mainType, subType, imagePart, analysisID);
    }

    public void onNext2(long analysisID, File file) {
        final CameraPositions position = positions.get(currentPosition);
        Log.d("check_file", String.valueOf(file.equals(null)));
        Log.d("check_file", String.valueOf(file.exists()));
        Log.d("check_file", file.toString());


//        if (position.position < 5) {
//            file = resizeImage(file, 500);
//        } else {
            file = resizeImageHeight(file, 3000);
//        }

        String subTypeString = position.getText().toLowerCase();
        if (subTypeString.contains("zoomed")) {
            subTypeString = subTypeString.replace(" (zoomed)", "");
        }

        RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
        Part imagePart = Part.createFormData("images[]", file.getName(), imageBody);
        Part mainType = Part.createFormData("mainType", position.getShootType());
        Part subType = Part.createFormData("subType", subTypeString);
        //imagesPath.add(file.getAbsolutePath());
        mainViewModel.addImage(file.getAbsolutePath());
        //  uploadImage(mainType, subType, imagePart, analysisID);

        alImg.add(imagePart);
        alMainType.add(mainType);
        alSubType.add(subType);
        alAnalysisID.add(analysisID);
        mainViewModel.addDataParts(imagePart, mainType, subType, analysisID);
    }

    public void onEndProcess(long analysisID) {
        final CameraCaptureState state = captureStateLiveData.getValue();
        File file = new File(state.getPath().replace("file://", ""));

        final CameraPositions position = positions.get(currentPosition);

        if (position.position < 5) {
            file = resizeImage(file, 500);
        } else {
            file = resizeImageHeight(file, 3000);
        }

        String subTypeString = position.getText().toLowerCase();
        if (subTypeString.contains("zoomed")) {
            subTypeString = subTypeString.replace(" (zoomed)", "");
        }

        RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
        Part imagePart = Part.createFormData("images[]", file.getName(), imageBody);
        Part mainType = Part.createFormData("mainType", position.getShootType());
        Part subType = Part.createFormData("subType", subTypeString);
        //imagesPath.add(file.getAbsolutePath());
        mainViewModel.addImage(file.getAbsolutePath());
        alImg.add(imagePart);
        alMainType.add(mainType);
        alSubType.add(subType);
        alAnalysisID.add(analysisID);
        mainViewModel.addDataParts(imagePart, mainType, subType, analysisID);
        endProcess.setValue("ProcessDone");
    }

    private void saveUploadImage(Part mainType, Part subType, Part image, long analysisID) {
        alImg.add(image);
        alMainType.add(mainType);
        alSubType.add(subType);
        alAnalysisID.add(analysisID);
        mainViewModel.addDataParts(image, mainType, subType, analysisID);

        currentPosition++;
        if (currentPosition >= positions.size()) {
            try {
                compressImagesToZip();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // uploadMultipleImage(0);
            captureCompleteLiveData.setValue(AsyncResponse.success(true));
        } else {
            captureCompleteLiveData.setValue(AsyncResponse.notStarted());
            updateState(positions.get(currentPosition), false, null);
        }
    }

    private void uploadMultipleImage(int pos) {

        captureCompleteLiveData.setValue(AsyncResponse.loading());
        repository.api.uploadImage(alMainType.get(pos), alSubType.get(pos), alImg.get(pos), alAnalysisID.get(pos))
                .enqueue(new Callback<UploadImageResponse>() {
                    @Override
                    public void onResponse(Call<UploadImageResponse> call, Response<UploadImageResponse> response) {
                        if (!response.isSuccessful()) {
                            onFailure(call, new Exception("Failed to upload image. Please check your connection."));
                        } else {
                            UploadImageResponse imageResponse = response.body();
                            if (pos < alImg.size() - 1) {
                                uploadMultipleImage(pos + 1);
                            } else {
                                captureCompleteLiveData.setValue(AsyncResponse.success(true));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadImageResponse> call, Throwable t) {
                        captureCompleteLiveData.setValue(AsyncResponse.error(t.getMessage()));
                    }
                });
    }

    private void uploadImage(Part mainType, Part subType, Part image, long analysisID) {
        captureCompleteLiveData.setValue(AsyncResponse.loading());
        repository.api.uploadImage(mainType, subType, image, analysisID)
                .enqueue(new Callback<UploadImageResponse>() {
                    @Override
                    public void onResponse(Call<UploadImageResponse> call, Response<UploadImageResponse> response) {

                        if (!response.isSuccessful()) {
                            onFailure(call, new Exception("Failed to upload image. Please check your connection."));
                        } else {
                            UploadImageResponse imageResponse = response.body();
                            currentPosition++;
                            if (currentPosition >= positions.size()) {
                                captureCompleteLiveData.setValue(AsyncResponse.success(true));
                            } else {
                                captureCompleteLiveData.setValue(AsyncResponse.notStarted());
                                updateState(positions.get(currentPosition), false, null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadImageResponse> call, Throwable t) {
                        captureCompleteLiveData.setValue(AsyncResponse.error(t.getMessage()));
                    }
                });
    }

    public void saveCameraMeasurement(Patient patient, int height) {
        try {
            final CameraMeasurementRequest request = new CameraMeasurementRequest(height);
            repository.api.setCameraMeasurement(patient.getId(), request)
                    .enqueue(new Callback<CameraMeasurementResponse>() {
                        @Override
                        public void onResponse(Call<CameraMeasurementResponse> call, Response<CameraMeasurementResponse> response) {

                            if (!response.isSuccessful()) {
                                onFailure(call, new Exception("Failed to upload image. Please check your connection."));
                            } else {
                                CameraMeasurementResponse imageResponse = response.body();
                                Log.e("-----SAVE", imageResponse.getData().getPatient_id() + "");
                            }
                        }

                        @Override
                        public void onFailure(Call<CameraMeasurementResponse> call, Throwable t) {
                            captureCompleteLiveData.setValue(AsyncResponse.error(t.getMessage()));
                        }
                    });
        } catch (Exception e) {

        }
    }

    public void getCameraMeasurement(Patient patient) {
        repository.api.getCameraMeasurement(patient.getId())
                .enqueue(new Callback<CameraMeasurementResponse>() {
                    @Override
                    public void onResponse(Call<CameraMeasurementResponse> call, Response<CameraMeasurementResponse> response) {

                        if (!response.isSuccessful()) {
                            onFailure(call, new Exception("Failed to load camera measurement"));
                        } else {
                            CameraMeasurementResponse imageResponse = response.body();
                            if (imageResponse.getStatus()) {
                                camData.setValue(imageResponse);
                            }
                            Log.e("-----GET", imageResponse.getData().getHeight() + "");
                        }
                    }

                    @Override
                    public void onFailure(Call<CameraMeasurementResponse> call, Throwable t) {
                        camData.setValue(null);
                        //captureCompleteLiveData.setValue(AsyncResponse.error(t.getMessage()));
                    }
                });
    }

    private File resizeImage(File inputFile, int resizeWidth) {
        Bitmap bitmap = null;
        File f = null;
        bitmap = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
        bitmap = manageOrentation(inputFile, bitmap);
        f = bitmapToFile(bitmap);
        return f;
    }


    private File resizeImageWidth(File inputFile, int resizeWidth) {
        Bitmap bitmap = null;
        Bitmap resizedBitmap = null;
        File f = null;
        bitmap = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
        bitmap = manageOrentation(inputFile, bitmap);

        int origWidth = bitmap.getWidth();
        int origHeight = bitmap.getHeight();
        Log.d("orig_width", String.valueOf(origWidth));
        Log.d("orig_height", String.valueOf(origHeight));

        if (origWidth > resizeWidth) {
            try {
                int destHeight = (resizeWidth * origHeight) / origWidth;// origHeight / (origWidth / resizeWidth);
                resizedBitmap = BITMAP_RESIZER(bitmap, resizeWidth, destHeight);
                f = bitmapToFile(resizedBitmap);

            } catch (Exception errVar) {
                errVar.printStackTrace();
            }
            return f;
        } else {
            return inputFile;
        }



    }

    private File resizeImageHeight(File inputFile, int resizeHeight) {
        Bitmap bitmap = null;
        Bitmap resizedBitmap = null;
        File f = null;
        bitmap = BitmapFactory.decodeFile(inputFile.getAbsolutePath());
        bitmap = manageOrentation(inputFile, bitmap);
        int origWidth = bitmap.getWidth();
        int origHeight = bitmap.getHeight();
        if (origHeight > resizeHeight) {
            try {
                int destWidth = (origWidth * resizeHeight) / origHeight;// origWidth / (origHeight / resizeHeight);
                resizedBitmap = BITMAP_RESIZER(bitmap, destWidth, resizeHeight);
                f = bitmapToFile(resizedBitmap);
            } catch (Exception errVar) {
                errVar.printStackTrace();
            }
        }
        return f;
    }

    private Bitmap manageOrentation(File file, Bitmap bitmap) {
        ExifInterface ei = null;
        Bitmap rotatedBitmap = null;
        try {
            ei = new ExifInterface(file);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public Bitmap BITMAP_RESIZER(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = 0;
        float middleY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(6));
        return scaledBitmap;

    }

    public File bitmapToFile(Bitmap bitmap) { // File name like "image.png"
        //create a file to write bitmap data
        File file = null;
        try {
            String path = getApplication().getFilesDir().toString();
            Long tsLong = System.currentTimeMillis()/1000;
            file = new File(path + File.separator + "GroTack" +tsLong + ".png");
            file.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos); // YOU can also save it in JPEG
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return file; // it will return null
        }
    }

    public void compressImagesToZip() throws IOException {

        FileHelper.zip( mainViewModel.getImagesList(),activity.getExternalCacheDir()+ "/" + getApplication().getApplicationContext().getResources().getString(R.string.imageFolder),"GroTrack.zip" );
//        BufferedInputStream origin = null;
//        String opd =activity.getExternalCacheDir()+ "/" + getApplication().getApplicationContext().getResources().getString(R.string.imageFolder) + "/GroTrack.zip";
//        Log.e("====>>", opd);
//        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(opd)));
//        out.setLevel(ZipOutputStream.DEFLATED);
//        try {
//            byte data[] = new byte[2048];
//            for (int i = 0; i < mainViewModel.getImagesList().size(); i++) {
//                FileInputStream fi = new FileInputStream(mainViewModel.getImagesList().get(i));
//                origin = new BufferedInputStream(fi, 2048);
//                try {
//                    ZipEntry entry = new ZipEntry(mainViewModel.getImagesList().get(i).substring(mainViewModel.getImagesList().get(i).lastIndexOf("/") + 1));
//entry.
//                    out.putNextEntry(entry);
//                    int count;
//                    while ((count = origin.read(data, 0, 2048)) != -1) {
//                        out.write(data, 0, count);
//                    }
//                } finally {
//                    origin.close();
//                }
//            }
//        } finally {
//            out.close();
//        }
    }
}