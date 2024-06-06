package com.clinicapp.ui.glo.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.clinicapp.databinding.FragmentGloBinding;
import com.clinicapp.models.CameraAutoCaptureState;
import com.clinicapp.models.CameraMeasurementResponse;
import com.clinicapp.models.CameraPositions;
import com.clinicapp.models.FaceCameraCaptureState;
import com.clinicapp.providers.AsyncResponse;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseViewModel;
import com.clinicapp.utilities.GloCameraUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;

public class GloCameraViewModel extends BaseViewModel {

    private int currentPosition;
    private List<CameraPositions> positions;
    private MutableLiveData<FaceCameraCaptureState> captureStateLiveData;
    private MutableLiveData<CameraAutoCaptureState> autoCaptureStateLiveData;
    private MutableLiveData<AsyncResponse<Boolean, Exception>> captureCompleteLiveData;
    private MutableLiveData<String> endProcess;
    private MutableLiveData<String> textGuide;
    private MutableLiveData<CameraMeasurementResponse> camData;
    //    List<Part> alImg = new ArrayList<>();
//    List<Part> altypes = new ArrayList<>();
//    List<Part> alPositions = new ArrayList<>();
//    List<Part> alAreas = new ArrayList<>();
//    List<Long> alAnalysisID = new ArrayList<>();
    MutableLiveData<String> isNextButtonPress;
    MainViewModel mainViewModel;
    Activity activity;
    FragmentGloBinding views;

    public GloCameraViewModel(@NonNull Application application) {
        super(application);
        captureCompleteLiveData = new MutableLiveData<>(AsyncResponse.notStarted());

    }


    public void init(Activity activity, @NonNull List<CameraPositions> positions, MainViewModel mainViewModel, FragmentGloBinding views) {
        this.activity = activity;
        this.positions = positions;
        this.views = views;
        this.mainViewModel = mainViewModel;
        currentPosition = 0;
        final FaceCameraCaptureState initialState = new FaceCameraCaptureState(positions.get(0), false, null, "");
        final CameraAutoCaptureState initialAutoCaptureState = new CameraAutoCaptureState("", null);
        captureStateLiveData = new MutableLiveData<>(initialState);
        autoCaptureStateLiveData = new MutableLiveData<>(initialAutoCaptureState);
        camData = new MutableLiveData<>(null);
        endProcess = new MutableLiveData<>("");
        textGuide = new MutableLiveData<>("");
        isNextButtonPress = new MutableLiveData<>("");
    }

    public LiveData<String> getNextButtonState() {
        return isNextButtonPress;
    }

    public LiveData<String> getEndProcessData() {
        return endProcess;
    }

    public LiveData<String> getTextGuideLiveData() {
        return textGuide;
    }

    public void updateTextGuide(String newTextGuide) {
        textGuide.setValue(newTextGuide);
    }

    public void updateAutoCaptureState(CameraAutoCaptureState state) {
        final FaceCameraCaptureState captureState = captureStateLiveData.getValue();
        if (captureState.isPreview()) {
            return;
        }
        autoCaptureStateLiveData.setValue(state);
        if (state.getTextGuide().equals("OK")) {
            GloCameraUtils.Companion.getGetCameraProvider().unbindAll();
            updateState(positions.get(currentPosition), true, state.getPath());
        }
    }

    public LiveData<CameraAutoCaptureState> getAutoCaptureState() {
        return autoCaptureStateLiveData;
    }

    public LiveData<CameraMeasurementResponse> getCamMeasureLiveData() {
        return camData;
    }

    public LiveData<FaceCameraCaptureState> getCaptureStateLiveData() {
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
        final FaceCameraCaptureState state = new FaceCameraCaptureState(position, isPreview, path, isNextButtonPress.getValue());
        captureStateLiveData.setValue(state);
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void onStartShoot() {
        isNextButtonPress.setValue("");
        captureCompleteLiveData.setValue(AsyncResponse.notStarted());
        currentPosition = currentPosition + 1;
        if (positions.size() > currentPosition) {
            updateState(positions.get(currentPosition), false, null);
        } else {
            captureCompleteLiveData.setValue(AsyncResponse.success(true));
        }
    }

    public void onNext(long analysisID) {
        final FaceCameraCaptureState state = captureStateLiveData.getValue();
        File file = new File(state.getPath().replace("file://", ""));

        final CameraPositions position = positions.get(currentPosition);

        String posText = "";
        if (position.getFacePosition(position.position).contains("left")) {
            posText = "left";
        } else if (position.getFacePosition(position.position).contains("right")) {
            posText = "right";
        } else if (position.getFacePosition(position.position).contains("front")) {
            posText = "front";
        }

        if (position.position < 5) {
            file = resizeImageWidth(file, 500);
            RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
            Part imagePart = Part.createFormData("image", file.getName(), imageBody);
            Part captureType = Part.createFormData("type", position.position > 5 ? "closeup" : "global");
            Part facePosition = Part.createFormData("position", posText);
            Part xAxis = Part.createFormData("x_axis", "0.0");
            Part yAxis = Part.createFormData("y_axis", "0.0");
            mainViewModel.addImage(file.getAbsolutePath());
//            saveFaceGlobalImage(captureType, facePosition, imagePart, xAxis, yAxis, analysisID);
        } else {
            file = resizeImageHeight(file, 3000);
            RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
            Part imagePart = Part.createFormData("image", file.getName(), imageBody);
            Part captureType = Part.createFormData("type", position.position > 5 ? "closeup" : "global");
            Part facePosition = Part.createFormData("position", "front");
            Part faceArea = Part.createFormData("area", position.getFaceAreaPosition(position.position));
            Part xAxis = Part.createFormData("x_axis", "0.0");
            Part yAxis = Part.createFormData("y_axis", "0.0");
            Part brownSpot = Part.createFormData("is_brown_spot", views.cbBrownspot.isChecked() ? "1" : "0");
            Part redSpot = Part.createFormData("is_red_spot", views.cbRedspot.isChecked() ? "1" : "0");
            Part pores = Part.createFormData("is_pores", views.cbPores.isChecked() ? "1" : "0");
            mainViewModel.addImage(file.getAbsolutePath());
            saveFaceLocalImage(captureType, facePosition, faceArea, imagePart, xAxis, yAxis, brownSpot, redSpot, pores, analysisID);
        }
    }

    public void onNextLocal(long analysisID, String pos, String x, String y, File file) {
        final FaceCameraCaptureState state = captureStateLiveData.getValue();
//        File file = new File(state.getPath().replace("file://", ""));

        final CameraPositions position = positions.get(currentPosition);

        RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
        Part imagePart = Part.createFormData("image", file.getName(), imageBody);
        Part captureType = Part.createFormData("type", "closeup");
        Part facePosition = Part.createFormData("position", pos);
        Part faceArea = Part.createFormData("area", "");
        Part xAxis = Part.createFormData("x_axis", x);
        Part yAxis = Part.createFormData("y_axis", y);
        Part brownSpot = Part.createFormData("is_brown_spot", views.cbBrownspot.isChecked() ? "1" : "0");
        Part redSpot = Part.createFormData("is_red_spot", views.cbRedspot.isChecked() ? "1" : "0");
        Part pores = Part.createFormData("is_pores", views.cbPores.isChecked() ? "1" : "0");
        mainViewModel.addImage(file.getAbsolutePath());
        saveFaceLocalImage(captureType, facePosition, faceArea, imagePart, xAxis, yAxis, brownSpot, redSpot, pores, analysisID);

    }

    public void onEndProcess(long analysisID) {
        final FaceCameraCaptureState state = captureStateLiveData.getValue();
        File file = new File(state.getPath().replace("file://", ""));

        final CameraPositions position = positions.get(currentPosition);
        String posText = "";
        if (position.getFacePosition(position.position).contains("left")) {
            posText = "left";
        } else if (position.getFacePosition(position.position).contains("right")) {
            posText = "right";
        } else if (position.getFacePosition(position.position).contains("front")) {
            posText = "front";
        }

        if (position.position < 5) {
            file = resizeImageWidth(file, 500);
            RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
            Part imagePart = Part.createFormData("image", file.getName(), imageBody);
            Part captureType = Part.createFormData("type", position.position > 5 ? "closeup" : "global");
            Part facePosition = Part.createFormData("position", posText);
            Part xAxis = Part.createFormData("x_axis", "0.0");
            Part yAxis = Part.createFormData("y_axis", "0.0");
            mainViewModel.addImage(file.getAbsolutePath());
//            saveFaceGlobalImage(captureType, facePosition, imagePart, xAxis, yAxis, analysisID);
        } else {
            file = resizeImageHeight(file, 3000);
            RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
            Part imagePart = Part.createFormData("image", file.getName(), imageBody);
            Part captureType = Part.createFormData("type", position.position > 5 ? "closeup" : "global");
            Part facePosition = Part.createFormData("position", "front");
            Part faceArea = Part.createFormData("area", position.getFaceAreaPosition(position.position));
            Part xAxis = Part.createFormData("x_axis", "0.0");
            Part yAxis = Part.createFormData("y_axis", "0.0");
            Part brownSpot = Part.createFormData("is_brown_spot", views.cbBrownspot.isChecked() ? "1" : "0");
            Part redSpot = Part.createFormData("is_red_spot", views.cbRedspot.isChecked() ? "1" : "0");
            Part pores = Part.createFormData("is_pores", views.cbPores.isChecked() ? "1" : "0");
            mainViewModel.addImage(file.getAbsolutePath());
            saveFaceLocalImage(captureType, facePosition, faceArea, imagePart, xAxis, yAxis, brownSpot, redSpot, pores, analysisID);
        }
        endProcess.setValue("ProcessDone");
    }

    public void saveFaceGlobalImage(Part type, Part position, Part image,Part xAxis, Part yAxis, long analysisID) {
        mainViewModel.addFaceDataParts(image, type, position, null, xAxis, yAxis, null, null, null, "Global", analysisID);
        currentPosition = currentPosition + 1;
        if (currentPosition >= positions.size()) {
            captureCompleteLiveData.setValue(AsyncResponse.success(true));
        } else {
            captureCompleteLiveData.setValue(AsyncResponse.notStarted());
        }
    }

    public void saveFaceLocalImage(Part type, Part position, Part area, Part image, Part xAxis, Part yAxis,
                                   Part brownSpot, Part redSpot, Part pores, long analysisID) {
        mainViewModel.addFaceDataParts(image, type, position, area, xAxis, yAxis, brownSpot, redSpot, pores, "Closeup", analysisID);
        CameraPositions cposition = positions.get(currentPosition);
        isNextButtonPress.setValue(cposition.title);
        currentPosition = currentPosition + 1;
        if (currentPosition >= positions.size()) {
            captureCompleteLiveData.setValue(AsyncResponse.success(true));
        } else {
            captureCompleteLiveData.setValue(AsyncResponse.notStarted());
            updateState(positions.get(currentPosition), false, null);
        }
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
                f = bitmapToFile(bitmap);
            }
        } else {
            f = bitmapToFile(bitmap);
        }
        return f;
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
                f = bitmapToFile(bitmap);
            }

        } else {
            f = bitmapToFile(bitmap);
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
            Long tsLong = System.currentTimeMillis() / 1000;
            file = new File(path + File.separator + "GroTack" + tsLong + ".png");
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

}