package com.clinicapp.ui.camera;

import static android.app.Activity.RESULT_OK;

import static com.clinicapp.ui.glo.fragments.GloFragment.rotateImage;
import static com.clinicapp.utilities.FaceMeshResultGlRenderer.NOSE_LANDMARK_INDEX;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.camera2.interop.Camera2Interop;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExtendableBuilder;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.utils.executor.CameraXExecutors;
import androidx.camera.core.impl.utils.futures.FutureCallback;
import androidx.camera.core.impl.utils.futures.Futures;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.clinicapp.R;
import com.clinicapp.databinding.FragmentCameraBinding;
import com.clinicapp.models.CameraAutoCaptureState;
import com.clinicapp.models.CameraCaptureState;
import com.clinicapp.models.CameraMeasurementResponse;
import com.clinicapp.models.CameraPositions;
import com.clinicapp.models.Patient;
import com.clinicapp.providers.AsyncResponse;
import com.clinicapp.ui.camera.viewmodel.CameraViewModel;
import com.clinicapp.ui.glo.fragments.GloFragment;
import com.clinicapp.ui.home.HomeFragment;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.BaseFragment;
import com.clinicapp.utilities.CameraUtils;
import com.clinicapp.utilities.FaceMeshResultGlRenderer;
import com.clinicapp.utilities.FaceMeshResultImageView;
import com.clinicapp.utilities.Utils;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.components.TextureFrameConsumer;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.framework.MediaPipeException;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutions.facemesh.FaceMesh;
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions;
import com.google.mediapipe.solutions.facemesh.FaceMeshResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLContext;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class CameraFragment extends BaseFragment {
    private FragmentCameraBinding views;
    private CameraViewModel viewModel;
    private MainViewModel mainViewModel;
    CameraPositions position;
    CameraMeasurementResponse camData = null;
    boolean isCaptured = false;

    private FaceMeshResultImageView imageView;
    MediaPlayer mp;
    private static final String TAG = "GloCameraFragment";
    String mainfilename = "";

    private enum InputType {
        UNKNOWN,
        IMAGE,
        CAMERA
    }

    private static final boolean RUN_ON_GPU = true;

    private static int POSITION = 0;
    public static final int LEFT_EYE_LANDMARK_INDEX = 359;
    public static final int RIGHT_EYE_LANDMARK_INDEX = 130;

    // {@link SurfaceTexture} where the camera-preview frames can be accessed.
    private SurfaceTexture previewFrameTexture;
    // {@link SurfaceView} that displays the camera-preview frames processed by a MediaPipe graph.
    private SurfaceView previewDisplayView;
    // Creates and manages an {@link EGLContext}.
    // Converts the GL_TEXTURE_EXTERNAL_OES texture from Android camera into a regular texture to be
    // consumed by {@link FrameProcessor} and the underlying MediaPipe graph.
    private ExternalTextureConverter converter;
    // ApplicationInfo for retrieving metadata defined in the manifest.
    private ApplicationInfo applicationInfo;
    // Handles camera access via the {@link CameraX} Jetpack support library.
    private CameraXPreviewHelper cameraHelper;
    // InputSource, determine input source
    private InputType inputType = InputType.UNKNOWN;
    // CameraInput, camera solution from mediapipe
    private CameraInput cameraInput;
    // FaceMesh
    private FaceMesh facemesh;
    // OpenGL Surface View
    private SolutionGlSurfaceView<FaceMeshResult> glSurfaceView;
    // frame layout for preview
    private FrameLayout frameLayout;
    private ImageView imgOverlay;
    private ImageView imgChin;
    private ImageView imgEyeBridge;
    double latestCoordinateX;
    double latestCoordinateY;
    private TextView status;
    private ImageCapture imageCapture;
    private boolean faceAlgined = false;
    private boolean faceReady = false;
    private ExecutorService cameraExecutor;
    private CameraSelector cameraSelector;
    private Preview previewCamera;
    private ActivityResultLauncher<Intent> imageGetter;

    private TextureFrameConsumer newFrameListener;
    private SurfaceTexture frameTexture;
    SharedPreferences sharedPref;
    LandmarkProto.NormalizedLandmark lipsLandmark;
    float x;
    float y;
    boolean photoTaken = false;
    boolean photoTaken2 = false;
    String from;

    Context mContext;
    Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        views = FragmentCameraBinding.inflate(getLayoutInflater(), container, false);
        frameLayout = views.previewDisplayLayout;
        status = views.txtPositionName;
        return views.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CameraViewModel.class);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);
        viewModel.init(getActivity(), mainViewModel.getSelectedPositions(), mainViewModel);
        from = getArguments().getString("FROM");
        if (from == null){
            from = "global";
        }

        if (from.equals("closeup")) {
            initViews();
            initListeners();
        }else{
//            views.viewMiddle.setVisibility(View.VISIBLE);
//            views.leftDot.setVisibility(View.VISIBLE);
//            views.rightDot.setVisibility(View.VISIBLE);
            views.imgOverlayFront2.setVisibility(View.VISIBLE);
            try {
                applicationInfo =
                        mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Cannot find application info: " + e);
            }

            cameraExecutor = Executors.newSingleThreadExecutor();
            setupLiveDemoUiComponents();

            imageGetter =
                    registerForActivityResult(
                            new ActivityResultContracts.StartActivityForResult(),
                            result -> {
                                Intent resultIntent = result.getData();
                                if (resultIntent != null) {
                                    if (result.getResultCode() == RESULT_OK) {
                                        Bitmap bitmap = null;
                                        try {
                                            bitmap =
                                                    downscaleBitmap(
                                                            MediaStore.Images.Media.getBitmap(
                                                                    getActivity().getContentResolver(), resultIntent.getData()));
                                        } catch (IOException e) {
                                            Log.e(TAG, "Bitmap reading error:" + e);
                                        }
                                        try {
                                            InputStream imageData =
                                                    getActivity().getContentResolver().openInputStream(resultIntent.getData());
                                            bitmap = rotateBitmap(bitmap, imageData);
                                        } catch (IOException e) {
                                            Log.e(TAG, "Bitmap rotation error:" + e);
                                        }
                                        if (bitmap != null) {
                                            facemesh.send(bitmap);
                                        }
                                    }
                                }
                            });

            if (POSITION == 4) {
                views.ivLine.setVisibility(View.VISIBLE);
                x = getArguments().getFloatArray("lips")[0];
                y = getArguments().getFloatArray("lips")[1];
            }
        }
    }

    private Bitmap downscaleBitmap(Bitmap originalBitmap) {
        double aspectRatio = (double) originalBitmap.getWidth() / originalBitmap.getHeight();
        Log.e("aspectRatio", String.valueOf(aspectRatio));
        int width = 1295;
        int height = 1582;
        Log.e("width", String.valueOf(width) + " " + height);
        if (((double) imageView.getWidth() / imageView.getHeight()) > aspectRatio) {
            width = (int) (height * aspectRatio);
        } else {
            height = (int) (width / aspectRatio);
        }
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    private Bitmap rotateBitmap(Bitmap inputBitmap, InputStream imageData) throws IOException {
        int orientation =
                new ExifInterface(imageData)
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        if (orientation == ExifInterface.ORIENTATION_NORMAL) {
            return inputBitmap;
        }
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                matrix.postRotate(0);
        }
        return Bitmap.createBitmap(
                inputBitmap, 0, 0, inputBitmap.getWidth(), inputBitmap.getHeight(), matrix, true);
    }

    private void setupLiveDemoUiComponents() {
        if (inputType == InputType.CAMERA) {
            return;
        }
        stopCurrentPipeline();
        setupStreamingModePipeline(InputType.CAMERA);
    }

    private void setupStreamingModePipeline(InputType inputType) {
        this.inputType = inputType;
        // Initializes a new MediaPipe Face Mesh solution instance in the streaming mode.
        facemesh =
                new FaceMesh(
                        getContext(),
                        FaceMeshOptions.builder()
                                .setStaticImageMode(false)
                                .setRefineLandmarks(true)
                                .setRunOnGpu(RUN_ON_GPU)
                                .build());
        facemesh.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Face Mesh error:" + message));

        if (inputType == InputType.CAMERA) {
            PermissionHelper.checkAndRequestCameraPermissions(getActivity());
            PermissionHelper.checkAndRequestReadExternalStoragePermissions(getActivity());
            setNewFrameListener(textureFrame -> facemesh.send(textureFrame));
        }

        // Initializes a new Gl surface view with a user-defined FaceMeshResultGlRenderer.
        glSurfaceView =
                new SolutionGlSurfaceView<>(getContext(), facemesh.getGlContext(), facemesh.getGlMajorVersion());
        FaceMeshResultGlRenderer face = new FaceMeshResultGlRenderer();
        face.position(POSITION);
        glSurfaceView.setSolutionResultRenderer(face);
        glSurfaceView.setRenderInputImage(true);
        facemesh.setResultListener(
                faceMeshResult -> {
                    logNoseLandmark(faceMeshResult, /*showPixelValues=*/ false);
                    glSurfaceView.setRenderData(faceMeshResult);
                    glSurfaceView.requestRender();
                });

        // The runnable to start camera after the gl surface view is attached.
        if (inputType == InputType.CAMERA) {
            glSurfaceView.post(this::startCamera);
        }

        // Updates the preview layout.
        frameLayout.removeAllViewsInLayout();
        frameLayout.addView(glSurfaceView);
        glSurfaceView.setVisibility(View.VISIBLE);
        frameLayout.requestLayout();
    }

    public void startCamera() {
        startCameraHelper(getActivity(),
                facemesh.getGlContext(),
                CameraHelper.CameraFacing.BACK,
                glSurfaceView.getWidth(),
                glSurfaceView.getWidth());
    }

    //Reconstruct Camera Input
    private void startCameraHelper(Activity activity, EGLContext eglContext, CameraHelper.CameraFacing cameraFacing, int width, int height) {
        if (PermissionHelper.cameraPermissionsGranted(activity) && PermissionHelper.readExternalStoragePermissionsGranted(activity)) {
            cameraHelper = new CameraXPreviewHelper();
            if (converter == null) {
                converter = new ExternalTextureConverter(eglContext, 2);
            }

            if (newFrameListener == null) {
                throw new MediaPipeException(MediaPipeException.StatusCode.FAILED_PRECONDITION.ordinal(), "newFrameListener is not set.");
            } else {
                frameTexture = converter.getSurfaceTexture();
                converter.setConsumer(newFrameListener);
                cameraHelper.setOnCameraStartedListener((surfaceTexture) -> {
                    if (glSurfaceView.getWidth() != 0 && glSurfaceView.getWidth() != 0) {
                        updateOutputSize(width, height);
                    }
                });
            }

            cameraHelper.startCamera(activity, new ImageCapture.Builder(), cameraFacing, frameTexture, new Size(width, height));
        }
    }

    private void updateOutputSize(int width, int height) {
        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(new Size(width, height));
        boolean isCameraRotated = cameraHelper.isCameraRotated();
        Log.i("CameraInput", "Set camera output texture frame size to width=" + displaySize.getWidth() + " , height=" + displaySize.getHeight());
        converter.setDestinationSize(isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(), isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
    }

    private void stopCurrentPipeline() {
        if (cameraHelper != null) {
            setNewFrameListener(null);
            converter.close();
        }
    }

    private void setNewFrameListener(TextureFrameConsumer listener) {
        newFrameListener = listener;
    }

    private void logNoseLandmark(FaceMeshResult result, boolean showPixelValues) {
        float safeZone = 20;
        float safeZoneY = 30;
        if (result == null || result.multiFaceLandmarks().isEmpty()) {
            return;
        }
        LandmarkProto.NormalizedLandmark noseLandmark = result.multiFaceLandmarks().get(0).getLandmarkList().get(4);
        lipsLandmark = result.multiFaceLandmarks().get(0).getLandmarkList().get(61);
        // For Bitmaps, show the pixel values. For texture inputs, show the normalized coordinates.
        if (showPixelValues) {
            int width = result.inputBitmap().getWidth();
            int height = result.inputBitmap().getHeight();
            Log.i(
                    TAG,
                    String.format(
                            "MediaPipe Face Mesh nose coordinates (pixel values): x=%f, y=%f",
                            noseLandmark.getX() * width, noseLandmark.getY() * height));
        } else {
            switch (POSITION) {
                case 0: {
                    Rect offsetViewBounds = new Rect();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.bottomDot, offsetViewBounds);
                    double chinSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double chinSideRight = ((double) offsetViewBounds.left + safeZone + views.bottomDot.getWidth()) / frameLayout.getWidth();
                    double chinSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double chinSideBottom = ((double) offsetViewBounds.top + safeZone + views.bottomDot.getHeight()) / frameLayout.getHeight();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.leftDot, offsetViewBounds);
                    double leftSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double leftSideRight = ((double) offsetViewBounds.left + safeZone + views.leftDot.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.leftDot, offsetViewBounds);
                    double leftSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double leftSideBottom = ((double) offsetViewBounds.top + safeZone + views.leftDot.getHeight()) / frameLayout.getHeight();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.rightDot, offsetViewBounds);
                    double rightSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double rightSideRight = ((double) offsetViewBounds.left + safeZone + views.rightDot.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.rightDot, offsetViewBounds);
                    double rightSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double rightSideBottom = ((double) offsetViewBounds.top + safeZone + views.rightDot.getHeight()) / frameLayout.getHeight();

                    LandmarkProto.NormalizedLandmark nose = result.multiFaceLandmarks().get(0).getLandmarkList().get(NOSE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark right = result.multiFaceLandmarks().get(0).getLandmarkList().get(RIGHT_EYE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark left = result.multiFaceLandmarks().get(0).getLandmarkList().get(LEFT_EYE_LANDMARK_INDEX);

//                    if (right.getX() <= leftSideRight && right.getX() >= leftSideLeft
//                            && right.getY() >= leftSideTop && right.getY() <= leftSideBottom) {
//                        views.rightDot.setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.MULTIPLY);
//                    } else {
//                        views.rightDot.setColorFilter(Color.RED);
//                    }
//                    if (left.getX() <= rightSideRight && left.getX() >= rightSideLeft
//                            && left.getY() >= rightSideTop && left.getY() <= rightSideBottom) {
//                        views.leftDot.setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.MULTIPLY);
//                    } else {
//                        views.leftDot.setColorFilter(Color.RED);
//                    }
//                    if (nose.getX() <= chinSideRight && nose.getX() >= chinSideLeft) {
//                        views.viewMiddle.setBackgroundColor(Color.BLUE);
//                    } else {
//                        views.viewMiddle.setBackgroundColor(Color.RED);
//                    }

//                    if (
////                    nose.getY() >= chinSideTop && nose.getY() <= chinSideBottom
////                            && nose.getX() <= chinSideRight && nose.getX() >= chinSideLeft
//                            right.getX() <= leftSideRight && right.getX() >= leftSideLeft
//                                    && right.getY() >= leftSideTop && right.getY() <= leftSideBottom
//                                    && left.getX() <= rightSideRight && left.getX() >= rightSideLeft
//                                    && left.getY() >= rightSideTop && left.getY() <= rightSideBottom
//                    )
                    if (
                            nose.getY() >= chinSideTop && nose.getY() <= chinSideBottom
                                    && right.getX() <= leftSideRight && right.getX() >= leftSideLeft
                                    && left.getX() <= rightSideRight && left.getX() >= rightSideLeft
                    ) {
                        status.setTextColor(Color.GREEN);
                        faceAlgined = true;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                new CountDownTimer(1000, 1000) {

                                    public void onTick(long millisUntilFinished) {
                                        Log.e("Tiktok", String.valueOf(millisUntilFinished));
                                    }

                                    public void onFinish() {
                                        faceReady = true;
                                    }

                                }.start();
                                faceReady = false;
                            }
                        });
                        if (faceReady) {
                            if (cameraHelper != null && !photoTaken) {
                                photoTaken = true;
                                String fileName = System.currentTimeMillis() + "effect1.jpg";
                                mainfilename = fileName;
                                File file = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures", fileName);
//                                    ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                                cameraHelper.takePicture(file, new ImageCapture.OnImageSavedCallback() {
                                    @Override
                                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getContext(), "Success " + file, Toast.LENGTH_SHORT).show();
                                                showPreview(fileName);
                                                faceReady = false;
                                                if (mp != null) {
                                                    mp = MediaPlayer.create(getActivity(), R.raw.camera_shutter_click);
                                                    mp.start();
                                                }
//                                                mp = null;
//                                            nextEffect();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(@NonNull ImageCaptureException exception) {
                                        Log.i(TAG, String.format(
                                                "error : %s", exception.toString()));
                                    }
                                });
                            }
                            faceAlgined = false;
                        }
                    } else {
                        status.setTextColor(Color.RED);
                        faceAlgined = false;
                    }

                    break;
                }

                case 4: {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            views.ivLine.setX(x * views.mainConstraint.getWidth());
                            views.ivLine.setY(y * views.mainConstraint.getHeight());
                            views.ivLine.setElevation(10);

                            LandmarkProto.NormalizedLandmark nose = result.multiFaceLandmarks().get(0).getLandmarkList().get(NOSE_LANDMARK_INDEX);

                            Rect offsetViewBounds = new Rect();
                            views.mainConstraint.offsetDescendantRectToMyCoords(views.ivLine, offsetViewBounds);
                            double leftSideTop = ((double) y * views.mainConstraint.getHeight() - safeZone) / frameLayout.getHeight();
                            double leftSideBottom = ((double) y * views.mainConstraint.getHeight() + safeZone + views.ivLine.getHeight()) / frameLayout.getHeight();

                            if (
                                    nose.getY() >= leftSideTop && nose.getY() <= leftSideBottom
                            ) {
                                views.ivLine.setColorFilter(Color.GREEN);
                                faceAlgined = true;

                                if (cameraHelper != null && !photoTaken2) {
                                    photoTaken2 = true;
                                    String fileName = System.currentTimeMillis() + "effect4.jpg";
                                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures", fileName);
//                                    ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                                    cameraHelper.takePicture(file, new ImageCapture.OnImageSavedCallback() {
                                        @Override
                                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getContext(), "Success " + file, Toast.LENGTH_SHORT).show();
                                                    showPreview(fileName);
                                                    mp = null;
//                                            nextEffect();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(@NonNull ImageCaptureException exception) {
                                            Log.i(TAG, String.format(
                                                    "error : %s", exception.toString()));
                                        }
                                    });
                                }
                                faceAlgined = false;
//                                    }
                            } else {
                                views.ivLine.setColorFilter(Color.RED);
                                faceAlgined = false;
                            }
                        }
                    });
                    break;
                }
            }

        }
    }

    public void showPreview(String fileName) {
//        mp.start();
        views.btRetake.setVisibility(View.VISIBLE);
        Log.e("position", String.valueOf(POSITION));
        if (POSITION != 0) {
            POSITION = 4;
            Bundle bundle = new Bundle();
            x = lipsLandmark.getX();
            y = lipsLandmark.getY();
        }else {
//        if (POSITION == 1 || POSITION == 4 || POSITION == 5) {
//            setupStaticImageDemoUiComponents(fileName, true);
//        } else {
            String qrPath = Environment.getExternalStorageDirectory() + "/Pictures/";
            Log.e("path", qrPath);
            File imgFile = new File(qrPath, fileName);
            Bitmap bitmap = BitmapFactory.decodeFile(qrPath + fileName);
            bitmap = manageOrentation(imgFile, bitmap);
            Matrix matrix = new Matrix();
            matrix.postScale(1f, 1f);
            int newW = (int) (bitmap.getWidth() * 0.86);
            int newH = (int) (bitmap.getHeight() * 0.76);
            int marginW = (int) (bitmap.getWidth() * 0.08);
            int marginH = (int) (bitmap.getHeight() * 0.12);
            bitmap = Bitmap.createBitmap(bitmap, marginW, marginH, newW, newH, matrix, true);
            ImageView imageView2 = new ImageView(mContext);
            imageView2.setScaleType(ImageView.ScaleType.FIT_XY);

            imageView2.setImageBitmap(bitmap);
            frameLayout.removeAllViewsInLayout();
            frameLayout.addView(imageView2);
            imageView2.setVisibility(View.VISIBLE);

            views.btNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCaptureImage(imgFile);
                    POSITION = 0;
                }
            });
        }
//        }

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

    public void saveCaptureImage(File file) {
        viewModel.onNext2(mainViewModel.getHairAnalysisID(), file);
        Navigation.findNavController(views.getRoot())
                .navigate(R.id.action_cameraFragment_to_returnToHomeScreen);
    }

    private void initViews() {
        views.groupCapture.setVisibility(View.VISIBLE);
        views.groupPreview.setVisibility(View.INVISIBLE);
    }

    private void initListeners() {
        views.captureImage.setOnClickListener(view -> takePhoto());
        views.btCancel.setOnClickListener(this::onCancelShoot);
        views.btRetake.setOnClickListener(view -> viewModel.retake());
        views.btNext.setOnClickListener(view -> next());
        viewModel.init(getActivity(), mainViewModel.getSelectedPositions(), mainViewModel);
        viewModel.getCaptureStateLiveData().observe(getViewLifecycleOwner(), this::handleCaptureStateChange);
        viewModel.getCaptureCompleteLiveData().observe(getViewLifecycleOwner(), this::handleCaptureUploadChange);
        viewModel.getCamMeasureLiveData().observe(getViewLifecycleOwner(), this::handleCamMeasureChange);
        viewModel.getEndProcessData().observe(getViewLifecycleOwner(), this::handleEndProcessResponse);
//        viewModel.getTextGuideLiveData().observe(getViewLifecycleOwner(),this::handleTextGuideChange);
        viewModel.getAutoCaptureState().observe(getViewLifecycleOwner(), this::handleAutoCaptureStateChange);
        views.preview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    if (views.preview.getController() != null) {
                        views.preview.getController().setTapToFocusEnabled(true);
                        views.preview.performClick();
                    }
                } catch (Exception e) {
                }
                return false;
            }
        });

        views.btEnd.setOnClickListener(this::onEndShoot);

        views.decreaseSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int currentHeight = views.imgCrownmiddle.getHeight();
                    views.imgCrownmiddle.getLayoutParams().height = currentHeight - 5;
                    views.imgCrownmiddle.requestLayout();
                } catch (Exception e) {

                }
            }
        });

        views.increaseSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int currentHeight = views.imgCrownmiddle.getHeight();
                    if (currentHeight < 900) {
                        views.imgCrownmiddle.getLayoutParams().height = currentHeight + 5;
                        views.imgCrownmiddle.requestLayout();
                    }
                } catch (Exception e) {

                }
            }
        });
    }

    private void next() {
        try {
            if (position.getOverlay() == R.drawable.crown_camera_overlay) {
                if (camData == null) {
                    viewModel.saveCameraMeasurement(mainViewModel.getSelectedPatient(), views.imgCrownmiddle.getHeight());
                }
            }
            Log.d("hairanalysis_id", String.valueOf(mainViewModel.getHairAnalysisID()));
            viewModel.onNext(mainViewModel.getHairAnalysisID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCaptureUploadChange(AsyncResponse<Boolean, Exception> response) {
        if (response.isFresh()) {
            response.pop();
            if (response.isNotStarted()) {
                isCaptured = false;
                views.btNext.setVisibility(View.INVISIBLE);
                views.progressBar.setVisibility(View.GONE);
            } else if (response.isError()) {
                Toast.makeText(getContext(), response.error.getMessage(), Toast.LENGTH_LONG).show();
                views.btNext.setVisibility(View.VISIBLE);
                views.progressBar.setVisibility(View.GONE);
            } else if (response.isLoading()) {
                views.btNext.setVisibility(View.INVISIBLE);
                views.progressBar.setVisibility(View.VISIBLE);
            } else if (response.isSuccess()) {
                Bundle b = getArguments();
                // b.putStringArrayList("IMAGE_LIST", viewModel.imagesPath);
                Navigation.findNavController(views.getRoot())
                        .navigate(R.id.action_cameraFragment_to_returnToHomeScreen, b);
            }
        }
    }

    private void onCancelShoot(View view) {
        Navigation.findNavController(view)
                .navigateUp();
    }

    private void onEndShoot(View view) {
 /*       Navigation.findNavController(views.getRoot())
                .navigate(R.id.pophome, getArguments());*/
     /*   Navigation.findNavController(views.getRoot())
                .navigate(R.id.pophome, getArguments());*/

        if (isCaptured) {
            viewModel.onEndProcess(mainViewModel.getHairAnalysisID());
        } else {
            Bundle b = getArguments();
            b.putBoolean("isEnd", true);
            Navigation.findNavController(views.getRoot())
                    .navigate(R.id.action_cameraFragment_to_returnToHomeScreen, b);
        }

    }

    private void handleEndProcessResponse(String response) {
        if (response.equals("ProcessDone")) {
            Bundle b = getArguments();
            b.putBoolean("isEnd", true);
            Navigation.findNavController(views.getRoot())
                    .navigate(R.id.action_cameraFragment_to_returnToHomeScreen, b);
        }
    }

    private void handleTextGuideChange(String guide) {
        views.txtGuide.setText(guide);
        if (guide.equals("OK") && !isCaptured) {
            takePhoto();
        }
    }

    private void handleAutoCaptureStateChange(CameraAutoCaptureState state) {
        views.txtGuide.setText(state.getTextGuide());
    }

    private void handleCamMeasureChange(CameraMeasurementResponse state) {
        camData = state;
        if (position.getOverlay() == R.drawable.crown_camera_overlay) {
            if (state != null) {
                views.llUpdown.setVisibility(View.GONE);
                views.imgCrownmiddle.getLayoutParams().height = (int) state.getData().getHeight();
                views.imgCrownmiddle.requestLayout();
            } else {
                views.llUpdown.setVisibility(View.VISIBLE);
            }
        }
    }

    private void handleCaptureStateChange(CameraCaptureState state) {
        final Patient patient = mainViewModel.getSelectedPatient();
        position = state.getPosition();
        toggleCapturedPreview(state.isPreview());
        setMarkerState(position);

        //For Camera Overlay
        if (state.isPreview()) {
            Glide.with(this)
                    .load(state.getPath())
                    .centerCrop()
                    .into(views.imgCapturePreview);
        } else {
            views.txtPositionName.setText(position.getText());
            Glide.with(this)
                    .load(position.getImage(!patient.isFemale()))
                    .into(position.isHairPosition() ? views.hairImgPosition : views.imgPosition); //if position is hair position show image on hairImgPosition View or else imgPosition View

            Glide.with(this)
                    .load(setOverlay(position))
                    .into(views.imgOverlay);

            startCamera(position.zoom);
        }

        //For Camera Frame size and zoomed text visibility
        if (position.isHairPosition()) {
            views.previewView.setVisibility(View.VISIBLE);
            views.txtType.setText("Close Up");
            //For zoomed text visibility
           /* if (position.zoom > 0) {
             //   views.txtPositionName.setText(position.getText() + "(zoomed)");
                views.zoomedText.setVisibility(View.VISIBLE);
            } else{
             //   views.txtPositionName.setText(position.getText());
                views.zoomedText.setVisibility(View.INVISIBLE);
            }*/
        } else {
            views.previewView.setVisibility(View.VISIBLE);
            views.txtType.setText("Global");
        }
    }

    private int setOverlay(CameraPositions position) {
        int overlay = position.getOverlay();
        if (overlay == R.drawable.hair_camera_overlay) {    //handling hair shoot overlay
            views.imgHairOverlay.setVisibility(View.VISIBLE);
            views.imgOverlay.setVisibility(View.INVISIBLE);
            views.imgCrownOverlay.setVisibility(View.INVISIBLE);
            views.imgCrownmiddle.setVisibility(View.INVISIBLE);
            views.llUpdown.setVisibility(View.INVISIBLE);
            views.imgOverlayBack.setVisibility(View.INVISIBLE);
            views.imgOverlayFront.setVisibility(View.INVISIBLE);
            views.imgFrontmiddle.setVisibility(View.INVISIBLE);
            views.imgBackmiddle.setVisibility(View.INVISIBLE);
        } else if (overlay == R.drawable.crown_camera_overlay) {      //positioning and handling crown shoot overlay
            viewModel.getCameraMeasurement(mainViewModel.getSelectedPatient());
            views.imgCrownOverlay.setVisibility(View.VISIBLE);
            views.imgOverlay.setVisibility(View.INVISIBLE);
            views.imgHairOverlay.setVisibility(View.INVISIBLE);
            views.imgCrownmiddle.setVisibility(View.VISIBLE);
            views.llUpdown.setVisibility(View.VISIBLE);
            views.imgOverlayBack.setVisibility(View.INVISIBLE);
            views.imgOverlayFront.setVisibility(View.INVISIBLE);
            views.imgFrontmiddle.setVisibility(View.INVISIBLE);
            views.imgBackmiddle.setVisibility(View.INVISIBLE);
        } else if (overlay == R.drawable.ic_frontal_bottom) {
            views.imgCrownOverlay.setVisibility(View.INVISIBLE);
            views.imgHairOverlay.setVisibility(View.INVISIBLE);
            views.imgOverlay.setVisibility(View.INVISIBLE);
            views.imgCrownmiddle.setVisibility(View.INVISIBLE);
            views.llUpdown.setVisibility(View.INVISIBLE);
            views.imgOverlayBack.setVisibility(View.INVISIBLE);
            views.imgOverlayFront.setVisibility(View.VISIBLE);
            views.imgFrontmiddle.setVisibility(View.VISIBLE);
            views.imgBackmiddle.setVisibility(View.INVISIBLE);
        } else if (overlay == R.drawable.ic_vertax_top) {
            views.imgCrownOverlay.setVisibility(View.INVISIBLE);
            views.imgHairOverlay.setVisibility(View.INVISIBLE);
            views.imgOverlay.setVisibility(View.INVISIBLE);
            views.imgCrownmiddle.setVisibility(View.INVISIBLE);
            views.llUpdown.setVisibility(View.INVISIBLE);
            views.imgOverlayBack.setVisibility(View.VISIBLE);
            views.imgOverlayFront.setVisibility(View.INVISIBLE);
            views.imgFrontmiddle.setVisibility(View.INVISIBLE);
            views.imgBackmiddle.setVisibility(View.VISIBLE);
        } else {
            views.imgCrownOverlay.setVisibility(View.INVISIBLE);
            views.imgHairOverlay.setVisibility(View.INVISIBLE);
            views.imgOverlay.setVisibility(View.VISIBLE);
            views.imgCrownmiddle.setVisibility(View.INVISIBLE);
            views.llUpdown.setVisibility(View.INVISIBLE);
            views.imgOverlayBack.setVisibility(View.INVISIBLE);
            views.imgOverlayFront.setVisibility(View.INVISIBLE);
            views.imgFrontmiddle.setVisibility(View.INVISIBLE);
            views.imgBackmiddle.setVisibility(View.INVISIBLE);
        }
        return overlay;
    }

    private void setMarkerState(CameraPositions position) {
        boolean isVisible = position.shouldShowMarker();
        views.imgPosition.setVisibility(!position.isHairPosition() ? View.VISIBLE : View.INVISIBLE);
        views.hairImgPosition.setVisibility(position.isHairPosition() ? View.VISIBLE : View.INVISIBLE);

        views.cbVertex.setVisibility(position.isHeadVisible() ? View.VISIBLE : View.GONE);
        views.cbCrown.setVisibility(position.isHeadVisible() ? View.VISIBLE : View.GONE);
        views.cbFrontal.setVisibility(position.isHeadVisible() ? View.VISIBLE : View.GONE);
        views.cbLeft.setVisibility(position.isLeftHeadVisible() ? View.VISIBLE : View.GONE);
        views.cbRight.setVisibility(position.isRightHeadVisible() ? View.VISIBLE : View.GONE);

        views.cbFrontal.setChecked(position.isFrontalChecked());
        views.cbVertex.setChecked(position.isVertexChecked());
        views.cbCrown.setChecked(position.isCrownChecked());
    }

    private void toggleCapturedPreview(boolean showCapturedImage) {
        //remove hair overlay, because we are making it visible in handleCaptureState anyway (if needed).
        views.imgHairOverlay.setVisibility(View.GONE);
        views.groupPreview.setVisibility(showCapturedImage ? View.VISIBLE : View.GONE);
        views.groupCapture.setVisibility(showCapturedImage ? View.INVISIBLE : View.VISIBLE);
        if (showCapturedImage)
            CameraUtils.Companion.getGetCameraProvider().unbindAll();
        views.captureImage.setClickable(!showCapturedImage);
        views.btNext.setVisibility(showCapturedImage ? View.VISIBLE : View.GONE);
        isCaptured = showCapturedImage ? true : false;
    }

    private void startCamera(float zoom) {
        if (Utils.checkCameraPermission(getContext())) {
            if (Utils.checkStoragePermission(getContext())) {
                //Start Camera after checking the permission
                CameraUtils.Companion.startCamera(getContext(), getViewLifecycleOwner(), views.preview, zoom, position).observe(getViewLifecycleOwner(), viewModel::updateAutoCaptureState);
            } else {
                Utils.notify(getContext(), "Unable to access Storage");
                getFragmentManager().beginTransaction().replace(R.id.main_layout, new HomeFragment()).commit();
            }
        } else {
            Utils.notify(getContext(), "Unable to access Camera");
            getFragmentManager().beginTransaction().replace(R.id.main_layout, new HomeFragment()).commit();
        }
    }


    private void takePhoto() {
        CameraUtils.Companion.takePhoto(getContext()).observe(getViewLifecycleOwner(), viewModel::onImageCaptured);
    }

}