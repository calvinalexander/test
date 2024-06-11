package com.clinicapp.ui.glo.fragments;

import static android.app.Activity.RESULT_OK;
import static com.clinicapp.utilities.FaceMeshResultGlRenderer.NOSE_LANDMARK_INDEX;

import static java.lang.Math.abs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
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
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.clinicapp.App;
import com.clinicapp.R;
import com.clinicapp.databinding.FragmentGloBinding;
import com.clinicapp.models.CameraAutoCaptureState;
import com.clinicapp.models.CameraMeasurementResponse;
import com.clinicapp.models.CameraPositions;
import com.clinicapp.models.FaceCameraCaptureState;
import com.clinicapp.models.Patient;
import com.clinicapp.providers.AsyncResponse;
import com.clinicapp.ui.glo.viewmodel.GloCameraViewModel;
import com.clinicapp.ui.home.HomeFragment;
import com.clinicapp.ui.home.viewmodels.MainViewModel;
import com.clinicapp.utilities.FaceMeshResultGlRenderer;
import com.clinicapp.utilities.FaceMeshResultImageView;
import com.clinicapp.utilities.GloCameraUtils;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLContext;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GloFragment extends Fragment {
    public static final int RIGHT_FACE_LANDMARK_INDEX = 234;
    public static final int LEFT_FACE_LANDMARK_INDEX = 454;
    public static final int TOP_FACE_LANDMARK_INDEX = 10;
    public static final int LEFT_EYE_LANDMARK_INDEX = 359;
    public static final int RIGHT_EYE_LANDMARK_INDEX = 130;
    public static final int RIGHT_CHEEK_NOSE_LANDMARK_INDEX = 120;
    public static final int RIGHT_CHEEK_FAR_LANDMARK_INDEX = 116;
    public static final int RIGHT_CHEEK_FARDOWN_LANDMARK_INDEX = 213;
    public static final int LEFT_CHEEK_NOSE_LANDMARK_INDEX = 349;
    public static final int LEFT_CHEEK_FAR_LANDMARK_INDEX = 345;
    public static final int LEFT_CHEEK_FARDOWN_LANDMARK_INDEX = 433;
    private static final String TAG = "GloCameraFragment";
    private static final boolean RUN_ON_GPU = true;
    private static final int CHIN_LANDMARK_INDEX = 152;
    private static final int EYES_BRIDGE_LANDMARK_INDEX = 8;
    private static int POSITION = 1;
    public Boolean first = true;
    CameraPositions position;
    CameraMeasurementResponse camData = null;
    boolean isCaptured = false;
    String from;
    String faceTo = "0";
    String audioStoopp = "0";
    boolean leftRight = false;
    ArrayList<ArrayList<Float>> arrayFace = new ArrayList<ArrayList<Float>>();
    ArrayList<Float> eyeXY = new ArrayList<Float>();
    MediaPlayer mp;
    String mainfilename = "";
    double latestCoordinateX;
    double latestCoordinateY;
    SharedPreferences sharedPref;
    File file;
    Bitmap bmInput;
    Context mContext;
    String hasil = "";
    private FragmentGloBinding views;
    private GloCameraViewModel viewModel;
    private MainViewModel mainViewModel;
    private FaceMeshResultImageView imageView;
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
    //    private ImageView imgEyeLeft;
//    private ImageView imgEyeRight;
    private TextView status;
    private ImageCapture imageCapture;
    private boolean faceAlgined = false;
    private boolean faceReady = false;
    private boolean inPreview = false;
    private ExecutorService cameraExecutor;
    private CameraSelector cameraSelector;
    private Preview previewCamera;
    private ActivityResultLauncher<Intent> imageGetter;
    private TextureFrameConsumer newFrameListener;
    private SurfaceTexture frameTexture;
    private Boolean photoTaken = false;
    private Boolean photoTaken2 = false;
    private Boolean photoTaken3 = false;
    private Boolean photoTaken4 = false;
    private Boolean photoTaken5 = false;
    private Boolean photoTaken6 = false;
    private Boolean photoTaken7 = false;
    private Boolean frontCheck;
    private Boolean rightCheck;
    private Boolean leftCheck;

    private static int[] findMaxProductIndex(ArrayList<ArrayList<Float>> arrayList) {
        float maxProduct = Float.MIN_VALUE;
        int[] maxProductIndex = new int[]{-1, -1};

        for (int i = 0; i < arrayList.size(); i++) {
            ArrayList<Float> innerList = arrayList.get(i);

            if (innerList.size() > 2) { // Ensure both indices in the second dimension exist
//                float product = innerList.get(10) == 0.0f ? abs(innerList.get(9) - innerList.get(3)) : abs(innerList.get(9) - innerList.get(1));
                float product = abs(innerList.get(1) - innerList.get(3));

                if (product > maxProduct) {
                    maxProduct = product;
                    maxProductIndex[0] = i; // Outer list index
                    maxProductIndex[1] = (int) product; // Maximum product
                }
            }
        }

        return maxProductIndex;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
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
        views = FragmentGloBinding.inflate(getLayoutInflater(), container, false);
        from = getArguments().getString("FROM");

        frameLayout = views.previewDisplayLayout;
        imgOverlay = views.ivStaticAlign;
        imgChin = views.ivChinDot;
        imgEyeBridge = views.ivEyeBridgeDot;
        status = views.txtPositionName;
        App app = (App) getContext().getApplicationContext();
        sharedPref = app.getSharedPrefs();

        if (from.equals("closeup")) {
            String fileName = sharedPref.getString("front", "");
            setupStaticImageDemoUiComponents(fileName, true);
            imageView.position(0);
            views.btNext.setOnClickListener(view -> next());
            views.btCancel.setOnClickListener(this::onCancelShoot);
            views.captureImage.setVisibility(View.INVISIBLE);
            views.preview.setVisibility(View.INVISIBLE);
            views.btNext.setVisibility(View.INVISIBLE);
        } else {
            if (getArguments().getString("IS_FROM_START").equals("true")) {
                POSITION = 1;
            } else {
                if (POSITION > 5) {
                    POSITION = 1;
                }
            }

            views.preview.setVisibility(View.INVISIBLE);
            try {
                applicationInfo =
                        mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Cannot find application info: " + e);
            }

            ConstraintLayout.LayoutParams newLayoutParams = (ConstraintLayout.LayoutParams) views.ivStaticAlign.getLayoutParams();

            frontCheck = getArguments().getBoolean("FRONTAL");
            rightCheck = getArguments().getBoolean("RIGHT_SIDE");
            leftCheck = getArguments().getBoolean("LEFT_SIDE");

            positioning();

            views.btCancel.setOnClickListener(this::onCancelShoot);
            views.btEnd.setVisibility(View.INVISIBLE);
            views.btEnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mainfilename.equals("")) {
                        FragmentTransaction ft;
                        switchPosition();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("FRONTAL", frontCheck);
                        bundle.putBoolean("RIGHT_SIDE", rightCheck);
                        bundle.putBoolean("LEFT_SIDE", leftCheck);
                        ft = getFragmentManager().beginTransaction();

                        ReturnToGloHomeFragment returnToGloHomeFragment = new ReturnToGloHomeFragment();
                        returnToGloHomeFragment.setArguments(bundle);
                        switchPrevPosition();
                        ft.replace(R.id.nav_host_fragment, returnToGloHomeFragment);
                        ft.commit();
                    } else {
                        views.btCancel.performClick();
                    }

                }

            });
            views.btRetake.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    retake();
                }
            });

        }

        views.txtType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("kondisi", String.valueOf("firsht:" + first + " kondisi:" + imageView.getCondition() + " state:" + imageView.getState() + " data:" + imageView.getData()));
            }
        });

        views.llPores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                views.cbPores.setChecked(!views.cbPores.isChecked());
            }
        });
        views.llRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                views.cbRedspot.setChecked(!views.cbRedspot.isChecked());
            }
        });
        views.llBrown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                views.cbBrownspot.setChecked(!views.cbBrownspot.isChecked());
            }
        });

        return views.getRoot();
    }

    private void positioning() {

//        if (POSITION == 2) {
//            POSITION += 2;
//        } else
        if (POSITION == 3) {
            POSITION += 1;
        }

        switch (POSITION) {
            case 1: {
                views.txtPositionName.setText("Frontal");
                views.ivStaticAlign.setVisibility(View.VISIBLE);
                break;
            }
            case 2: {
                views.txtPositionName.setText("Forehead");
                views.cbCrown.setVisibility(View.VISIBLE);
//                views.ivForeheadRect.setVisibility(View.VISIBLE);

                break;
            }
            case 3: {
                views.ivChinRect.setVisibility(View.VISIBLE);
                views.cbFrontal.setVisibility(View.VISIBLE);
                break;
            }
            case 4: {
                views.txtPositionName.setText("Left");
//                views.ivLeftFaceRect.setVisibility(View.VISIBLE);
                faceTo = String.valueOf(0);
                views.imgPosition.setImageDrawable(getResources().getDrawable(R.drawable.male_head_left));
                break;
            }
            case 5: {
                views.txtPositionName.setText("Right");
//                views.ivRightFaceRect.setVisibility(View.VISIBLE);
//                views.ivLeftCheekRect.setVisibility(View.INVISIBLE);
                views.imgPosition.setImageDrawable(getResources().getDrawable(R.drawable.male_head_right));
                break;
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GloCameraViewModel.class);
        mainViewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

//        mp = MediaPlayer.create(getActivity(), R.raw.camera_shutter_click);
//        mp = MediaPlayer.create(getActivity(), R.raw.capture_done_v3);
        mp = MediaPlayer.create(getActivity(), R.raw.hold_still_v2);

        if (!from.equals("closeup")) {
            cameraExecutor = Executors.newSingleThreadExecutor();
            setupLiveDemoUiComponents();
            views.captureImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (cameraHelper != null && !photoTaken) {
                        photoTaken = true;
                        String fileName = System.currentTimeMillis() + "effect" + POSITION + ".jpg";
                        mainfilename = fileName;
                        File file = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures", fileName);
//                                    ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                        cameraHelper.takePicture(file, new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        Toast.makeText(getContext(), "Success " + file, Toast.LENGTH_SHORT).show();
                                        showPreview(fileName);
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
                }
            });
        }

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
//        if (from.equals("global")) {
//            viewModel.init(getActivity(), mainViewModel.getSelectedFacePositions(), mainViewModel, views);
//            views.closeupHint.setVisibility(View.GONE);
//        }
    }

    public void retake() {
        inPreview = false;
        if (POSITION == 1) {
            Bundle bundle = new Bundle();
            bundle.putString("FROM", "global");
            bundle.putString("IS_FROM_START", "true");
            GloFragment gloCameraFragment = new GloFragment();
            gloCameraFragment.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .remove(this) // "this" refers to current instance of Fragment2
                    .commit();
            fragmentManager.popBackStack();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, gloCameraFragment).addToBackStack(null).commit();

        } else {
            frameLayout.removeAllViewsInLayout();
            frameLayout.addView(glSurfaceView);
            glSurfaceView.setVisibility(View.VISIBLE);
//        frameLayout.requestLayout();
            photoTaken = false;
            photoTaken2 = false;
            photoTaken4 = false;
            photoTaken5 = false;

            positioning();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("pori", String.valueOf(sharedPref.contains("position")));
        if (inputType == InputType.CAMERA) {
            PermissionHelper.checkAndRequestCameraPermissions(getActivity());
            PermissionHelper.checkAndRequestReadExternalStoragePermissions(getActivity());
            setNewFrameListener(textureFrame -> facemesh.send(textureFrame));
            glSurfaceView.post(this::startCamera);
            glSurfaceView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (inputType == InputType.CAMERA) {
            glSurfaceView.setVisibility(View.GONE);
            if (converter != null)
                converter.close();
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

    /**
     * Sets up the UI components for the static image demo.
     */
    private void setupStaticImageDemoUiComponents(String file_name, Boolean face_mesh) {
        imageView = new FaceMeshResultImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        if (face_mesh) {
            stopCurrentPipeline();
            setupStaticImageModePipeline(file_name);
        }
        String qrPath = Environment.getExternalStorageDirectory() + "/Pictures/";
        Log.e("path", qrPath);
        File imgFile = new File(qrPath, file_name);
        Bitmap bitmap = BitmapFactory.decodeFile(qrPath + file_name);
        bitmap = manageOrentation(imgFile, bitmap);
        bitmap = downscaleBitmap(bitmap);
        Matrix matrix = new Matrix();
        matrix.postScale(1f, 1f);
        int newW = (int) (bitmap.getWidth() * 0.86);
        int newH = (int) (bitmap.getHeight() * 0.76);
        int marginW = (int) (bitmap.getWidth() * 0.08);
        int marginH = (int) (bitmap.getHeight() * 0.12);
        bitmap = Bitmap.createBitmap(bitmap, marginW, marginH, newW, newH, matrix, true);
        facemesh.send(bitmap);
    }

    /**
     * Sets up core workflow for static image mode.
     */
    private void setupStaticImageModePipeline(String file_name) {
        this.inputType = InputType.IMAGE;
        // Initializes a new MediaPipe Face Mesh solution instance in the static image mode.
        facemesh =
                new FaceMesh(
                        mContext.getApplicationContext(),
                        FaceMeshOptions.builder()
                                .setStaticImageMode(true)
                                .setRefineLandmarks(true)
                                .setRunOnGpu(RUN_ON_GPU)
                                .build());

        // Connects MediaPipe Face Mesh solution to the user-defined FaceMeshResultImageView.
        facemesh.setResultListener(
                faceMeshResult -> {
                    logNoseLandmark(faceMeshResult, /*showPixelValues=*/ true);
                    if (first) {
                        imageView.backToDefault();
                    }
                    imageView.setFaceMeshResult(faceMeshResult);
                    bmInput = faceMeshResult.inputBitmap();
                    int width = bmInput.getWidth();
                    int height = bmInput.getHeight();
                    Bitmap latest = Bitmap.createBitmap(width, height, bmInput.getConfig());
                    Canvas canvas = new Canvas(latest);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            Log.e("xylocation", imageView.getCondition() + " " + imageView.getLocation());
                            if (imageView.getLocation() == "select" && imageView.getCondition()) {
                                views.btCancel.setVisibility(View.VISIBLE);
                                views.btNext.setVisibility(View.VISIBLE);
                                views.btCancel.setText("Undo");
                            }
                        }
                    });
                    getActivity().runOnUiThread(() -> imageView.update());
                });
        facemesh.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Face Mesh error:" + message));

        // Updates the preview layout.
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                views.btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (views.btCancel.getText().toString().equals("Undo")) {
                            if (first) {
                                views.btCancel.setText("Go Back");
                            } else {
                                views.btCancel.setVisibility(View.INVISIBLE);
                            }
                            views.btNext.setVisibility(View.INVISIBLE);
                            views.btEnd.setVisibility(View.VISIBLE);
                            imageView.undo();
                        } else {
                            getActivity().onBackPressed();
                        }
                    }
                });
                return false;
            }
        });
        frameLayout.removeAllViewsInLayout();
        imageView.setImageDrawable(null);
        frameLayout.addView(imageView);
        imageView.setVisibility(View.VISIBLE);
//        imageView.first(true);
    }

    public Bitmap viewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
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

    private void setupLiveDemoUiComponents() {
        if (inputType == InputType.CAMERA) {
            return;
        }
        stopCurrentPipeline();
        setupStreamingModePipeline(InputType.CAMERA);
    }

    public void startCamera() {
        startCameraHelper(getActivity(),
                facemesh.getGlContext(),
                CameraHelper.CameraFacing.FRONT,
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

    private void setNewFrameListener(TextureFrameConsumer listener) {
        newFrameListener = listener;
    }

    private void updateOutputSize(int width, int height) {
        try {
            Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(new Size(width, height));
            boolean isCameraRotated = cameraHelper.isCameraRotated();
            Log.i("CameraInput", "Set camera output texture frame size to width=" + displaySize.getWidth() + " , height=" + displaySize.getHeight());
            converter.setDestinationSize(isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(), isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
        } catch (Exception e) {
        }
    }

    private void stopCurrentPipeline() {
        if (cameraHelper != null) {
            setNewFrameListener(null);
            converter.close();
        }
    }

    protected Size computeViewSize(int width, int height) {
        return new Size(width, height);
    }

    private void checkDistance(){

    }

    private void logNoseLandmark(FaceMeshResult result, boolean showPixelValues) {
        float safeZone = 30;
        float radius = 25;
        int faceClose = 18;
        if (result == null || result.multiFaceLandmarks().isEmpty()) {
            return;
        }
        LandmarkProto.NormalizedLandmark noseLandmark = result.multiFaceLandmarks().get(0).getLandmarkList().get(4);
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
            Log.i(
                    TAG,
                    String.format(
                            "MediaPipe Face Mesh nose normalized coordinates (value range: [0, 1]): x=%f, y=%f",
                            noseLandmark.getX(), noseLandmark.getY()));
            Log.e("posisi", "" + POSITION);

            Bitmap bitmap = Bitmap.createBitmap(frameLayout.getWidth(), frameLayout.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(10);
            paint.setColor(Color.BLACK);
            canvas.drawLine(
                    frameLayout.getWidth() / 2,
                    0,
                    frameLayout.getWidth() / 2,
                    frameLayout.getHeight(),
                    paint);
            paint.setColor(Color.RED);
            canvas.save();
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) imgOverlay.getLayoutParams();
                    params.width = frameLayout.getWidth();
                    params.height = frameLayout.getHeight();

                    imgOverlay.setLayoutParams(params);
                    imgOverlay.setImageBitmap(bitmap);
                    if(!inPreview) {
                        imgOverlay.setVisibility(View.VISIBLE);
                    }
                }
            });

            switch (POSITION) {
                case 1: {
                    Rect offsetViewBounds = new Rect();

                    views.mainConstraint.offsetDescendantRectToMyCoords(imgChin, offsetViewBounds);
//                    double chinSideLeft = ((double) offsetViewBounds.left - safeZone)/frameLayout.getWidth();
//                    double chinSideRight = ((double) offsetViewBounds.left+ safeZone+imgChin.getWidth())/frameLayout.getWidth();
                    double chinSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double chinSideBottom = ((double) offsetViewBounds.top + safeZone + imgChin.getHeight()) / frameLayout.getHeight();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(imgEyeBridge, offsetViewBounds);
                    double bridgeSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double bridgeSideRight = ((double) offsetViewBounds.left + safeZone + imgEyeBridge.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEyeLeft1Dot, offsetViewBounds);
                    double leftSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double leftSideRight = ((double) offsetViewBounds.left + safeZone + views.ivEyeLeft1Dot.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEyeRight1Dot, offsetViewBounds);
                    double rightSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double rightSideRight = ((double) offsetViewBounds.left + safeZone + views.ivEyeRight1Dot.getWidth()) / frameLayout.getWidth();
//                    double bridgeSideTop = ((double) offsetViewBounds.top - safeZone)/frameLayout.getHeight();
//                    double bridgeSideBottom = ((double) offsetViewBounds.top+ safeZone+imgEyeBridge.getHeight())/frameLayout.getHeight();

                    LandmarkProto.NormalizedLandmark bridge = result.multiFaceLandmarks().get(0).getLandmarkList().get(EYES_BRIDGE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark chin = result.multiFaceLandmarks().get(0).getLandmarkList().get(CHIN_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark right = result.multiFaceLandmarks().get(0).getLandmarkList().get(RIGHT_FACE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark left = result.multiFaceLandmarks().get(0).getLandmarkList().get(LEFT_FACE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark nose = result.multiFaceLandmarks().get(0).getLandmarkList().get(TOP_FACE_LANDMARK_INDEX);

//                    Log.e("cek ini", String.format("apa ni " + (nose.getY() * frameLayout.getWidth() - safeZone) + "Pembanding" + (float) frameLayout.getHeight() / 2 ));
//                    Log.e("cek ini", String.format("apa ni " + (nose.getY() * frameLayout.getWidth() + safeZone) + "Pembanding" + (float) frameLayout.getHeight() / 2 ));
//                    Log.e("cek ini 2", String.valueOf(chin.getX() * frameLayout.getWidth() - safeZone));
                    if (faceTo != "") {
                        if (nose.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 2 &&
                            nose.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 2) {

                            if (
                                nose.getY() * frameLayout.getHeight() - safeZone <= (float) 210 &&
                                nose.getY() * frameLayout.getHeight() + safeZone >= (float) 210 &&
                                right.getX() * frameLayout.getWidth() - safeZone <= (float) 265 &&
                                right.getX() * frameLayout.getWidth() + safeZone >= (float) 265 &&
                                left.getX() * frameLayout.getWidth() - safeZone <= (float) 620 &&
                                left.getX() * frameLayout.getWidth() + safeZone >= (float) 620
                            ) {
                                paint.setColor(Color.GREEN);
                                if (audioStoopp == "0") {
                                    audioStoopp = "done";
                                    mp = MediaPlayer.create(getActivity(), R.raw.hold_still_v2);
                                    mp.start();
                                }
                                if (cameraHelper != null && !photoTaken) {
                                    photoTaken = true;
                                    take();
                                }
                            }

                            paint.setStyle(Paint.Style.STROKE);
                            canvas.drawOval(220,220,670,860, paint);


                            canvas.save();

//                            if (right.getX() * frameLayout.getWidth() <= (float) frameLayout.getWidth() / 3 + safeZone && right.getX() * frameLayout.getWidth() >= (float) frameLayout.getWidth() / 3 - safeZone
//                                    && left.getX() * frameLayout.getWidth() <= (float) frameLayout.getWidth() / 3 * 2 + safeZone && left.getX() * frameLayout.getWidth() >= (float) frameLayout.getWidth() / 3 * 2 - safeZone) {
//
//                                if (faceTo == "0" || faceTo == "pause") {
//                                    if (nose.getY() * frameLayout.getHeight() - safeZone <= (float) frameLayout.getHeight() / 2 && nose.getY() * frameLayout.getHeight() + safeZone >= (float) frameLayout.getHeight() / 2
////                                        chin.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 2 && chin.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 2
//                                    ) {
////                                    if(abs(right.getX() * frameLayout.getWidth() - nose.getX() * frameLayout.getWidth())/frameLayout.getWidth() * 100 < faceClose){
////                                        paint.setTextSize(50);
////                                        paint.setStrokeWidth(20);
////                                        paint.setStyle(Paint.Style.FILL);
////                                        paint.setColor(Color.RED);
////                                        canvas.drawText("Get Closer",frameLayout.getWidth() / 2 - 100,frameLayout.getHeight() / 2 - 20,paint);
////                                    }else {
////                                        faceTo = "ready";
////                                        mp.start();
////                                    }
//                                    }
//                                }
////                                ArrayList<Float> temp = new ArrayList<Float>();
////                                if (faceTo == "ready") {
//////                                    if (!arrayFace.isEmpty()) {
//////                                        if (faceTo != "up") {
//////                                            faceTo = "down";
//////                                            temp.add(chin.getX());
//////                                            temp.add(chin.getY());
//////                                            temp.add(bridge.getX());
//////                                            temp.add(bridge.getY());
//////                                            temp.add(right.getX());
//////                                            temp.add(right.getY());
//////                                            temp.add(left.getX());
//////                                            temp.add(left.getY());
//////                                            temp.add(nose.getX());
//////                                            temp.add(nose.getY());
//////                                            temp.add(0.001f);
//////                                            arrayFace.add(temp);
//////                                        }
//////                                    } else {
//////                                        faceTo = "up";
//////                                        temp.add(chin.getX());
//////                                        temp.add(chin.getY());
//////                                        temp.add(bridge.getX());
//////                                        temp.add(bridge.getY());
//////                                        temp.add(right.getX());
//////                                        temp.add(right.getY());
//////                                        temp.add(left.getX());
//////                                        temp.add(left.getY());
//////                                        temp.add(nose.getX());
//////                                        temp.add(nose.getY());
//////                                        temp.add(0.0f);
//////                                        arrayFace.add(temp);
//////                                    }
////                                }
//                                if (bridge.getY() * frameLayout.getHeight() - safeZone <= (float) frameLayout.getHeight() / 2 && bridge.getY() * frameLayout.getHeight() + safeZone >= (float) frameLayout.getHeight() / 2 && faceTo == "up"
////                                    chin.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 2 && chin.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 2
//                                ) {
////                                    mp.start();
////                                    faceTo = "pause";
//                                }
//                                if (chin.getY() * frameLayout.getHeight() - safeZone <= (float) frameLayout.getHeight() / 2 && chin.getY() * frameLayout.getHeight() + safeZone >= (float) frameLayout.getHeight() / 2 && faceTo == "down"
////                                    chin.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 2 && chin.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 2
//                                ) {
////                                    mp.start();
////                                    faceTo = "";
//                                }
//
//                            }
                        }
                    }
//                    if (faceTo == "") {
////                        int[] maxFace = findMaxProductIndex(arrayFace);
//////                        Log.e("cek nilai 2", Arrays.toString(maxFace) + " " + frameLayout.getHeight());
//////                        Log.e("cek nilai 3", String.valueOf(arrayFace.get(maxFace[0])));
////                        Float[] chinArr = {arrayFace.get(maxFace[0]).get(0) * frameLayout.getWidth(), arrayFace.get(maxFace[0]).get(1) * frameLayout.getHeight()};
////                        Float[] bridgeArr = {arrayFace.get(maxFace[0]).get(2) * frameLayout.getWidth(), arrayFace.get(maxFace[0]).get(3) * frameLayout.getHeight()};
////                        Float[] rightArr = {arrayFace.get(maxFace[0]).get(4) * frameLayout.getWidth(), arrayFace.get(maxFace[0]).get(5) * frameLayout.getHeight()};
////                        Float[] leftArr = {arrayFace.get(maxFace[0]).get(6) * frameLayout.getWidth(), arrayFace.get(maxFace[0]).get(7) * frameLayout.getHeight()};
////                        if (
////                                chin.getX() * frameLayout.getWidth() <= chinArr[0] + safeZone && chin.getX() * frameLayout.getWidth() >= chinArr[0] - safeZone &&
////                                        chin.getY() * frameLayout.getHeight() >= chinArr[1] - safeZone && chin.getY() * frameLayout.getHeight() <= chinArr[1] + safeZone
////                                        && bridge.getX() * frameLayout.getWidth() <= bridgeArr[0] + safeZone && bridge.getX() * frameLayout.getWidth() >= bridgeArr[0] - safeZone &&
////                                        bridge.getY() * frameLayout.getHeight() >= bridgeArr[1] - safeZone && bridge.getY() * frameLayout.getHeight() <= bridgeArr[1] + safeZone
////                                        && right.getX() * frameLayout.getWidth() <= rightArr[0] + safeZone && right.getX() * frameLayout.getWidth() >= rightArr[0] - safeZone
////                                        && right.getY() * frameLayout.getHeight() <= rightArr[1] + safeZone && right.getY() * frameLayout.getHeight() >= rightArr[1] - safeZone
////                                        && left.getX() * frameLayout.getWidth() <= leftArr[0] + safeZone && left.getX() * frameLayout.getWidth() >= leftArr[0] - safeZone
////                                        && left.getY() * frameLayout.getHeight() <= leftArr[1] + safeZone && left.getY() * frameLayout.getHeight() >= leftArr[1] - safeZone
////                        ) {
////                            paint.setColor(Color.GREEN);
////                        }else {
////                            paint.setColor(Color.RED);
////                        }
////                        paint.setStyle(Paint.Style.STROKE);
////                        canvas.drawCircle(chinArr[0], chinArr[1], radius, paint);
////                        canvas.drawCircle(bridgeArr[0], bridgeArr[1], radius, paint);
//////                        canvas.drawCircle(rightArr[0] - 30, rightArr[1], 10, paint);
//////                        canvas.drawCircle(leftArr[0] + 30, leftArr[1], 10, paint);
////                        canvas.drawCircle(rightArr[0], rightArr[1], radius, paint);
////                        canvas.drawCircle(leftArr[0], leftArr[1], radius, paint);
////                        canvas.save();
//
////                        if (
////                                chin.getX() * frameLayout.getWidth() <= chinArr[0] + safeZone && chin.getX() * frameLayout.getWidth() >= chinArr[0] - safeZone &&
////                                        chin.getY() * frameLayout.getHeight() >= chinArr[1] - safeZone && chin.getY() * frameLayout.getHeight() <= chinArr[1] + safeZone
////                                        && bridge.getX() * frameLayout.getWidth() <= bridgeArr[0] + safeZone && bridge.getX() * frameLayout.getWidth() >= bridgeArr[0] - safeZone &&
////                                        bridge.getY() * frameLayout.getHeight() >= bridgeArr[1] - safeZone && bridge.getY() * frameLayout.getHeight() <= bridgeArr[1] + safeZone
////                                        && right.getX() * frameLayout.getWidth() <= rightArr[0] + safeZone && right.getX() * frameLayout.getWidth() >= rightArr[0] - safeZone
////                                        && right.getY() * frameLayout.getHeight() <= rightArr[1] + safeZone && right.getY() * frameLayout.getHeight() >= rightArr[1] - safeZone
////                                        && left.getX() * frameLayout.getWidth() <= leftArr[0] + safeZone && left.getX() * frameLayout.getWidth() >= leftArr[0] - safeZone
////                                        && left.getY() * frameLayout.getHeight() <= leftArr[1] + safeZone && left.getY() * frameLayout.getHeight() >= leftArr[1] - safeZone
////                        ) {
////                            tiktok();
////                            if (faceReady) {
////                                if (cameraHelper != null && !photoTaken) {
////                                    photoTaken = true;
////                                    take();
////                                }
////                                faceAlgined = false;
////                            }
////                        } else {
////                            status.setTextColor(Color.RED);
////                            faceAlgined = false;
////                        }
//                    }

                    break;
                }
                case 2: {
                    Rect offsetViewBounds = new Rect();
//                    requireActivity().runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            if (!inPreview) {
//                                                                imgOverlay.s
//                                                                etVisibility(View.VISIBLE);
//                                                            }
//                                                            status.setVisibility(View.VISIBLE);
//                                                        }
//                                                    }
//                    );
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEyeLeftDot, offsetViewBounds);
                    double leftSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double leftSideRight = ((double) offsetViewBounds.left + safeZone + views.ivEyeLeftDot.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEyeRightDot, offsetViewBounds);
                    double rightSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double rightSideRight = ((double) offsetViewBounds.left + safeZone + views.ivEyeRightDot.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEyeBottomDot, offsetViewBounds);
                    double bottomSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double bottomSideBottom = ((double) offsetViewBounds.top + safeZone + views.ivEyeBottomDot.getHeight()) / frameLayout.getHeight();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEyeTopDot, offsetViewBounds);
                    double topSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double topSideBottom = ((double) offsetViewBounds.top + safeZone + views.ivEyeTopDot.getHeight()) / frameLayout.getHeight();

                    LandmarkProto.NormalizedLandmark right = result.multiFaceLandmarks().get(0).getLandmarkList().get(RIGHT_FACE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark left = result.multiFaceLandmarks().get(0).getLandmarkList().get(LEFT_FACE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark top = result.multiFaceLandmarks().get(0).getLandmarkList().get(10);
                    LandmarkProto.NormalizedLandmark bottom = result.multiFaceLandmarks().get(0).getLandmarkList().get(2);
                    LandmarkProto.NormalizedLandmark nose = result.multiFaceLandmarks().get(0).getLandmarkList().get(NOSE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark chin = result.multiFaceLandmarks().get(0).getLandmarkList().get(CHIN_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark bridge = result.multiFaceLandmarks().get(0).getLandmarkList().get(EYES_BRIDGE_LANDMARK_INDEX);

//                    paint.setStyle(Paint.Style.STROKE);
//                    if (right.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 3 && right.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3) {
//                        paint.setColor(Color.GREEN);
//                    } else {
//                        paint.setColor(Color.RED);
//                    }
//                    canvas.drawCircle(frameLayout.getWidth() / 3, right.getY() * frameLayout.getHeight(), radius, paint);
//                    if (left.getX() * frameLayout.getWidth() - safeZone <= (float) (frameLayout.getWidth() / 3) * 2 && left.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3 * 2) {
//                        paint.setColor(Color.GREEN);
//                    } else {
//                        paint.setColor(Color.RED);
//                    }
//                    canvas.drawCircle(frameLayout.getWidth() / 3 * 2, left.getY() * frameLayout.getHeight(), radius, paint);

                    if (faceTo != "") {
                        if (nose.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 2 &&
                            nose.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 2) {
//                            paint.setStyle(Paint.Style.STROKE);
                            if (
//                                nose.getY() * frameLayout.getHeight() - safeZone <= (float) frameLayout.getHeight() / 2 &&
//                                nose.getY() * frameLayout.getHeight() + safeZone >= (float) frameLayout.getHeight() / 2
                                top.getY() * frameLayout.getHeight() - safeZone <= (float) 210 &&
                                top.getY() * frameLayout.getHeight() + safeZone >= (float) 210 &&
                                right.getX() * frameLayout.getWidth() - safeZone <= (float) 265 &&
                                right.getX() * frameLayout.getWidth() + safeZone >= (float) 265 &&
                                left.getX() * frameLayout.getWidth() - safeZone <= (float) 620 &&
                                left.getX() * frameLayout.getWidth() + safeZone >= (float) 620

                            ) {
                                if(audioStoopp == "0"){
                                    audioStoopp = "sudah";
                                    mp = MediaPlayer.create(getActivity(), R.raw.tilt_head_v4);
                                    mp.start();
                                }
                                faceTo = "ready";
                                paint.setColor(Color.GREEN);
                            } else {
                                paint.setColor(Color.RED);
                            }
                            paint.setStyle(Paint.Style.STROKE);
//                            canvas.drawCircle(frameLayout.getWidth() / 2, frameLayout.getHeight() / 2, radius, paint);
                            canvas.drawOval(220,220,670,860, paint);

                            if(faceTo == "ready"){

                                if (bridge.getY() * frameLayout.getHeight() - safeZone <= (float) 670 &&
                                    bridge.getY() * frameLayout.getHeight() + safeZone >= (float) 650) {
                                    paint.setColor(Color.GREEN);
                                    if (cameraHelper != null && !photoTaken) {
                                        photoTaken = true;
                                        take();
                                    }


                                } else {
                                    paint.setColor(Color.RED);
                                }
                                canvas.drawLine(111,670,777,670, paint);

                            }



//                            canvas.save();
//                            if(abs(right.getX() * frameLayout.getWidth() - nose.getX() * frameLayout.getWidth())/frameLayout.getWidth() * 100 < faceClose){
//                                paint.setTextSize(50);
//                                paint.setColor(Color.RED);
//                                canvas.drawText("Get Closer",frameLayout.getWidth() / 2 - 100,frameLayout.getHeight() / 2 - 20,paint);
//                            }else {
//                                if (nose.getY() * frameLayout.getHeight() - safeZone <= (float) frameLayout.getHeight() / 2 && nose.getY() * frameLayout.getHeight() + safeZone >= (float) frameLayout.getHeight() / 2
//                                && left.getX() * frameLayout.getWidth() - safeZone <= (float) (frameLayout.getWidth() / 3) * 2 && left.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3 * 2
//                                && right.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 3 && right.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3) {
//                                    faceTo = "";
//                                    mp.start();
//                                    eyeXY.add(chin.getX() * frameLayout.getWidth());
//                                    eyeXY.add(chin.getY() * frameLayout.getHeight() + 50);
//                                }
//                            }
                        }
                    }
//                    if (faceTo == "") {
//                        paint.setStyle(Paint.Style.FILL);
//                        try {
//                            if (nose.getY() * frameLayout.getHeight() - safeZone <= eyeXY.get(1) && nose.getY() * frameLayout.getHeight() + safeZone >= eyeXY.get(1)
//                                    && left.getX() * frameLayout.getWidth() - safeZone <= (float) (frameLayout.getWidth() / 3) * 2 && left.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3 * 2
//                                    && right.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 3 && right.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3) {
//                                paint.setColor(Color.GREEN);
//                            } else {
//                                paint.setColor(Color.RED);
//                            }
//
//                        canvas.drawLine(eyeXY.get(0) -100,
//                                eyeXY.get(1),
//                                eyeXY.get(0)+100,
//                                eyeXY.get(1), paint);
//                        canvas.save();
//                        if (nose.getY() * frameLayout.getHeight() - safeZone <= eyeXY.get(1) && nose.getY() * frameLayout.getHeight() + safeZone >= eyeXY.get(1)
//                                && left.getX() * frameLayout.getWidth() - safeZone <= (float) (frameLayout.getWidth() / 3) * 2 && left.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3 * 2
//                                && right.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 3 && right.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3) {
//                            tiktok();
//                            if (faceReady) {
//                                if (cameraHelper != null && !photoTaken2) {
//                                    photoTaken2 = true;
//                                    take();
//                                }
//                            }
//                        } else {
//                            status.setTextColor(Color.RED);
//                            faceAlgined = false;
//                        }}catch(Exception e) {
//                            //  Block of code to handle errors
//                            Log.e("error", String.valueOf(e));
//                            Log.e("error2", String.valueOf(eyeXY));
//                        }
//                    }
                    break;
                }

                case 3: {
                    Rect offsetViewBounds = new Rect();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEyeRightDot, offsetViewBounds);
                    double rightSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double rightSideRight = ((double) offsetViewBounds.left + safeZone + views.ivEyeRightDot.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEyeLeftDot, offsetViewBounds);
                    double leftSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double leftSideRight = ((double) offsetViewBounds.left + safeZone + views.ivEyeLeftDot.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivChinBottomDot, offsetViewBounds);
                    double chinSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double chinSideBottom = ((double) offsetViewBounds.top + safeZone + views.ivChinBottomDot.getHeight()) / frameLayout.getHeight();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivChinTopDot, offsetViewBounds);
                    double topSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double topSideBottom = ((double) offsetViewBounds.top + safeZone + views.ivChinTopDot.getHeight()) / frameLayout.getHeight();

                    LandmarkProto.NormalizedLandmark right = result.multiFaceLandmarks().get(0).getLandmarkList().get(187);
                    LandmarkProto.NormalizedLandmark left = result.multiFaceLandmarks().get(0).getLandmarkList().get(411);
                    LandmarkProto.NormalizedLandmark top = result.multiFaceLandmarks().get(0).getLandmarkList().get(2);
                    LandmarkProto.NormalizedLandmark nose = result.multiFaceLandmarks().get(0).getLandmarkList().get(NOSE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark bottom = result.multiFaceLandmarks().get(0).getLandmarkList().get(152);
                    //                            Log.i(TAG,String.format(
                    //                                            "chin x1=%f, x2=%f, y1=%f, y2=%f",
                    //                                            bridgeSideRight, bridgeSideLeft, bridgeSideTop, bridgeSideBottom));

                        if (
                                right.getX() <= leftSideRight && right.getX() >= leftSideLeft
                                        && left.getX() <= rightSideRight && left.getX() >= rightSideLeft &&
                                        bottom.getY() >= chinSideTop && bottom.getY() <= chinSideBottom
//                                    top.getY() >= topSideTop && top.getY() <= topSideBottom
                        ) {
                            tiktok();
                            if (faceReady) {
                                if (cameraHelper != null && !photoTaken3) {
                                    photoTaken3 = true;
                                    take();
                                }
                            }
                        } else {
                            status.setTextColor(Color.RED);
                            faceAlgined = false;
                        }

                    break;
                }
                case 4: {
                    Rect offsetViewBounds = new Rect();
//                    requireActivity().runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            if (!inPreview) {
//                                                                imgOverlay.setVisibility(View.VISIBLE);
//                                                            }
//                                                        }
//                                                    }
//                    );
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEffect43, offsetViewBounds);
                    double noseSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double noseSideRight = ((double) offsetViewBounds.left + safeZone + views.ivEffect43.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEffect31, offsetViewBounds);
                    double topLeftCornerTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double topLeftCornerBottom = ((double) offsetViewBounds.bottom + safeZone) / frameLayout.getHeight();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivChinDot, offsetViewBounds);
                    double chinSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double chinSideBottom = ((double) offsetViewBounds.top + safeZone + views.ivChinBottomDot.getHeight()) / frameLayout.getHeight();

                    LandmarkProto.NormalizedLandmark nose = result.multiFaceLandmarks().get(0).getLandmarkList().get(NOSE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark below = result.multiFaceLandmarks().get(0).getLandmarkList().get(CHIN_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark eye = result.multiFaceLandmarks().get(0).getLandmarkList().get(RIGHT_EYE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark right = result.multiFaceLandmarks().get(0).getLandmarkList().get(RIGHT_EYE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark left = result.multiFaceLandmarks().get(0).getLandmarkList().get(LEFT_EYE_LANDMARK_INDEX);

                    if (faceTo != "") {
                        if (nose.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 2 && nose.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 2) {

                            paint.setStyle(Paint.Style.STROKE);
                            if (nose.getY() * frameLayout.getHeight() - safeZone <= (float) frameLayout.getHeight() / 2 && nose.getY() * frameLayout.getHeight() + safeZone >= (float) frameLayout.getHeight() / 2) {
                                paint.setColor(Color.GREEN);
                            } else {
                                paint.setColor(Color.RED);
                            }
                            canvas.drawCircle(frameLayout.getWidth() / 2, frameLayout.getHeight() / 2, radius, paint);

                            if (right.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 3 && right.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3) {
                                paint.setColor(Color.GREEN);
                            } else {
                                paint.setColor(Color.RED);
                            }
                            canvas.drawCircle(frameLayout.getWidth() / 3, right.getY() * frameLayout.getHeight(), radius, paint);
                            if (left.getX() * frameLayout.getWidth() - safeZone <= (float) (frameLayout.getWidth() / 3) * 2 && left.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3 * 2) {
                                paint.setColor(Color.GREEN);
                            } else {
                                paint.setColor(Color.RED);
                            }
                            canvas.drawCircle(frameLayout.getWidth() / 3 * 2, left.getY() * frameLayout.getHeight(), radius, paint);
//                            canvas.save();
//                            if(abs(eye.getX() * frameLayout.getWidth() - nose.getX() * frameLayout.getWidth())/frameLayout.getWidth() * 100 < faceClose){
//                                paint.setTextSize(50);
//                                paint.setColor(Color.RED);
//                                canvas.drawText("Get Closer",frameLayout.getWidth() / 2 - 100,frameLayout.getHeight() / 2 - 20,paint);
//                            }else {
                                if (nose.getY() * frameLayout.getHeight() - safeZone <= (float) frameLayout.getHeight() / 2 && nose.getY() * frameLayout.getHeight() + safeZone >= (float) frameLayout.getHeight() / 2) {
                                    faceTo = "";
                                    mp.start();
//                                eyeXY.add(eye.getX() * frameLayout.getWidth());
//                                eyeXY.add(eye.getY() * frameLayout.getHeight());

                                }
//                            }
                        }
                    }
                    if (faceTo == "") {
                        paint.setStyle(Paint.Style.FILL);
                        if (nose.getX() * frameLayout.getWidth() - safeZone <= eye.getX() * frameLayout.getWidth() && nose.getX() * frameLayout.getWidth() + safeZone >= eye.getX() * frameLayout.getWidth()) {
                            paint.setColor(Color.GREEN);
                        } else {
                            paint.setColor(Color.RED);
                        }
                        canvas.drawLine(eye.getX() * frameLayout.getWidth() -30,
                                eye.getY() * frameLayout.getHeight() + 300,
                                eye.getX() * frameLayout.getWidth()-30,
                                eye.getY() * frameLayout.getHeight(), paint);
//                        if (left.getX() * frameLayout.getWidth() - safeZone <= frameLayout.getWidth()/2 && left.getX() * frameLayout.getWidth() + safeZone >= frameLayout.getWidth()/2){
//                            paint.setColor(Color.GREEN);
//                        } else {
//                            paint.setColor(Color.RED);
//                        }
//                        canvas.drawLine(frameLayout.getWidth()/2,
//                                0,
//                                frameLayout.getWidth()/2,
//                                frameLayout.getHeight(), paint);
//                        canvas.save();
                        if (nose.getX() * frameLayout.getWidth() - safeZone <= eye.getX() * frameLayout.getWidth() && nose.getX() * frameLayout.getWidth() + safeZone >= eye.getX() * frameLayout.getWidth()) {
                        tiktok();
                            if (faceReady) {
                                if (cameraHelper != null && !photoTaken4) {
                                    photoTaken4 = true;
                                    take();
                                }
                            }
                        } else {
                            status.setTextColor(Color.RED);
                            faceAlgined = false;
                        }
                    }

                    break;
                }
                case 5: {
                    Rect offsetViewBounds = new Rect();
//                    requireActivity().runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            if (!inPreview) {
//                                                                imgOverlay.setVisibility(View.VISIBLE);
//                                                            }
//                                                        }
//                                                    }
//                    );

                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEffect33, offsetViewBounds);
                    double noseSideLeft = ((double) offsetViewBounds.left - safeZone) / frameLayout.getWidth();
                    double noseSideRight = ((double) offsetViewBounds.left + safeZone + views.ivEffect33.getWidth()) / frameLayout.getWidth();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivEffect31, offsetViewBounds);
                    double topLeftCornerTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double topLeftCornerBottom = ((double) offsetViewBounds.bottom + safeZone) / frameLayout.getHeight();
                    offsetViewBounds.setEmpty();
                    views.mainConstraint.offsetDescendantRectToMyCoords(views.ivChinDot, offsetViewBounds);
                    double chinSideTop = ((double) offsetViewBounds.top - safeZone) / frameLayout.getHeight();
                    double chinSideBottom = ((double) offsetViewBounds.top + safeZone + views.ivChinBottomDot.getHeight()) / frameLayout.getHeight();

                    LandmarkProto.NormalizedLandmark below = result.multiFaceLandmarks().get(0).getLandmarkList().get(CHIN_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark eye = result.multiFaceLandmarks().get(0).getLandmarkList().get(LEFT_EYE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark nose = result.multiFaceLandmarks().get(0).getLandmarkList().get(NOSE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark right = result.multiFaceLandmarks().get(0).getLandmarkList().get(RIGHT_EYE_LANDMARK_INDEX);
                    LandmarkProto.NormalizedLandmark left = result.multiFaceLandmarks().get(0).getLandmarkList().get(LEFT_EYE_LANDMARK_INDEX);

                    if (faceTo != "") {
                        if (nose.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 2 && nose.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 2) {
                            paint.setStyle(Paint.Style.STROKE);
                            if (nose.getY() * frameLayout.getHeight() - safeZone <= (float) frameLayout.getHeight() / 2 && nose.getY() * frameLayout.getHeight() + safeZone >= (float) frameLayout.getHeight() / 2) {
                                paint.setColor(Color.GREEN);
                            } else {
                                paint.setColor(Color.RED);
                            }
                            canvas.drawCircle(frameLayout.getWidth() / 2, frameLayout.getHeight() / 2, radius, paint);

                            if (right.getX() * frameLayout.getWidth() - safeZone <= (float) frameLayout.getWidth() / 3 && right.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3) {
                                paint.setColor(Color.GREEN);
                            } else {
                                paint.setColor(Color.RED);
                            }
                            canvas.drawCircle(frameLayout.getWidth() / 3, right.getY() * frameLayout.getHeight(), radius, paint);
                            if (left.getX() * frameLayout.getWidth() - safeZone <= (float) (frameLayout.getWidth() / 3) * 2 && left.getX() * frameLayout.getWidth() + safeZone >= (float) frameLayout.getWidth() / 3 * 2) {
                                paint.setColor(Color.GREEN);
                            } else {
                                paint.setColor(Color.RED);
                            }
                            canvas.drawCircle(frameLayout.getWidth() / 3 * 2, left.getY() * frameLayout.getHeight(), radius, paint);
                            canvas.save();
//                            if(abs(eye.getX() * frameLayout.getWidth() - nose.getX() * frameLayout.getWidth())/frameLayout.getWidth() * 100 < faceClose){
//                                paint.setTextSize(50);
//                                paint.setColor(Color.RED);
//                                canvas.drawText("Get Closer",frameLayout.getWidth() / 2 - 100,frameLayout.getHeight() / 2 - 20,paint);
//                            }else {
                                if (nose.getY() * frameLayout.getHeight() - safeZone <= (float) frameLayout.getHeight() / 2 && nose.getY() * frameLayout.getHeight() + safeZone >= (float) frameLayout.getHeight() / 2) {
                                    faceTo = "";
                                    mp.start();
//                                eyeXY.add(eye.getX() * frameLayout.getWidth());
//                                eyeXY.add(eye.getY() * frameLayout.getHeight());
                                }
//                            }
                        }
                    } else {
                        paint.setStyle(Paint.Style.FILL);
                        if (nose.getX() * frameLayout.getWidth() - safeZone <= eye.getX() * frameLayout.getWidth() && nose.getX() * frameLayout.getWidth() + safeZone >= eye.getX() * frameLayout.getWidth()) {
                            paint.setColor(Color.GREEN);
                        } else {
                            paint.setColor(Color.RED);
                        }
                        canvas.drawLine(eye.getX() * frameLayout.getWidth()+30 ,
                                eye.getY() * frameLayout.getHeight() + 300,
                                eye.getX() * frameLayout.getWidth()+30,
                                        eye.getY() * frameLayout.getHeight(), paint);
//                        if (right.getX() * frameLayout.getWidth() - safeZone <= frameLayout.getWidth()/2 && right.getX() * frameLayout.getWidth() + safeZone >= frameLayout.getWidth()/2){
//                            paint.setColor(Color.GREEN);
//                        } else {
//                            paint.setColor(Color.RED);
//                        }
//                        canvas.drawLine(frameLayout.getWidth()/2,
//                                0,
//                                frameLayout.getWidth()/2,
//                                frameLayout.getHeight(), paint);
//                        canvas.save();
                        if (nose.getX() * frameLayout.getWidth() - safeZone <= eye.getX() * frameLayout.getWidth() && nose.getX() * frameLayout.getWidth() + safeZone >= eye.getX() * frameLayout.getWidth()) {
                            tiktok();
                            if (faceReady) {
                                if (cameraHelper != null && !photoTaken5) {
                                    photoTaken5 = true;
                                    take();
                                }
                            }
                        } else {
                            status.setTextColor(Color.RED);
                            faceAlgined = false;
                        }
                    }

                    break;
                }
            }
        }
    }

    private void tiktok() {
        status.setTextColor(Color.GREEN);
        faceAlgined = true;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                new CountDownTimer(500, 500) {

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
    }

    private void take() {
        String fileName = System.currentTimeMillis() + "effect" + POSITION + ".jpg";
        mainfilename = fileName;
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures", fileName);

        cameraHelper.takePicture(file, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showPreview(fileName);
                        mp.stop();
                        mp = MediaPlayer.create(getActivity(), R.raw.capture_done_v3);
                        mp.start();
                        status.setTextColor(Color.GREEN);
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

    public void switchPosition() {
        POSITION += 1;
    }

    public void switchPrevPosition() {
        if (POSITION > 1) {
            POSITION -= 1;
        } else {
            POSITION = 1;
        }
    }

    public void reposition() {
        POSITION = 1;
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

    public void showPreview(String fileName) {
        mp.start();
        inPreview = true;
        eyeXY.clear();
        stopCurrentPipeline();
        views.btNext.setVisibility(View.VISIBLE);
        views.btRetake.setVisibility(View.VISIBLE);
        Log.e("position", String.valueOf(POSITION));
        if (POSITION == 1) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("front", fileName);
            editor.apply();
            setupStaticImageDemoUiComponents(fileName, true);
            imageView.position(1);
        } else {
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

        }

        views.btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveCaptureImage(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setupStreamingModePipeline(InputType.CAMERA);
                        nextEffect();
                        inPreview = false;
                    }},9000);
            }
        });

//        views.ivStaticAlign.setVisibility(View.INVISIBLE);
        views.ivEyeLeftDot.setVisibility(View.INVISIBLE);
        views.ivEyeRightDot.setVisibility(View.INVISIBLE);
        views.cbCrown.setVisibility(View.INVISIBLE);
        views.ivEyeBridgeDot.setVisibility(View.INVISIBLE);
        views.ivChinDot.setVisibility(View.INVISIBLE);
        views.ivForeheadRect.setVisibility(View.INVISIBLE);
        views.ivChinRect.setVisibility(View.INVISIBLE);
        views.ivEffect31.setVisibility(View.INVISIBLE);
        views.ivEffect32.setVisibility(View.INVISIBLE);
        views.ivEffect33.setVisibility(View.INVISIBLE);
        views.ivEffect41.setVisibility(View.INVISIBLE);
        views.ivEffect42.setVisibility(View.INVISIBLE);
        views.ivEffect43.setVisibility(View.INVISIBLE);
        views.ivStaticAlign.setVisibility(View.INVISIBLE);
        views.ivForeheadRect.setVisibility(View.INVISIBLE);
        views.ivChinRect.setVisibility(View.INVISIBLE);
        views.ivRightFaceRect.setVisibility(View.INVISIBLE);
        views.ivLeftCheekRect.setVisibility(View.INVISIBLE);
        views.ivLeftFaceRect.setVisibility(View.INVISIBLE);
        views.ivRightCheekRect.setVisibility(View.INVISIBLE);
    }

    /// change code for upload
    public void saveCaptureImage(String fileName) throws IOException {
        viewModel.init(getActivity(), mainViewModel.getSelectedFacePositions(), mainViewModel, views);
        String qrPath = Environment.getExternalStorageDirectory() + "/Pictures/";
        MultipartBody.Part imagePart = null;
        MultipartBody.Part facePosition = null;
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                sendImg(fileName, qrPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        switch (POSITION) {
            case 1: {
                OutputStream fOut = null;
                File file = new File(qrPath, fileName);

                File file2 = new File(qrPath, System.currentTimeMillis() + "gridgrlo" + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
                fOut = new FileOutputStream(file2);

                Bitmap pictureBitmap = viewToBitmap(imageView); // obtaining the Bitmap
                pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                fOut.flush(); // Not really required
                fOut.close(); // do not forget to close the stream

                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), file2.getAbsolutePath(), file2.getName(), file2.getName());
                RequestBody imageBody = RequestBody.create(file2, MediaType.parse("image/jpg"));
                imagePart = MultipartBody.Part.createFormData("image", file2.getName(), imageBody);
                facePosition = MultipartBody.Part.createFormData("position", "front");
                mainViewModel.addImage(file.getAbsolutePath());
                break;
            }
            case 2: {
                File file = new File(qrPath, fileName);
                RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
                imagePart = MultipartBody.Part.createFormData("image", fileName, imageBody);
                facePosition = MultipartBody.Part.createFormData("position", "forehead");
                mainViewModel.addImage(file.getAbsolutePath());

                break;
            }
            case 4: {
                File file = new File(qrPath, fileName);
                RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
                imagePart = MultipartBody.Part.createFormData("image", fileName, imageBody);
                facePosition = MultipartBody.Part.createFormData("position", "left");
                mainViewModel.addImage(file.getAbsolutePath());

                break;
            }
            case 5: {
                File file = new File(qrPath, fileName);
                RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
                imagePart = MultipartBody.Part.createFormData("image", fileName, imageBody);
                facePosition = MultipartBody.Part.createFormData("position", "right");
                mainViewModel.addImage(file.getAbsolutePath());

                break;
            }
        }
        faceTo = "0";
        MultipartBody.Part captureType = MultipartBody.Part.createFormData("type", "global");
        MultipartBody.Part xAxis = MultipartBody.Part.createFormData("x_axis", "0.0");
        MultipartBody.Part yAxis = MultipartBody.Part.createFormData("y_axis", "0.0");
        viewModel.saveFaceGlobalImage(captureType, facePosition, imagePart, xAxis, yAxis, mainViewModel.getFaceAnalysisID());
    }

    public void nextEffect() {
        FragmentTransaction ft;
        switchPosition();
        Bundle bundle = new Bundle();
        bundle.putString("FROM", from);
        bundle.putString("IS_FROM_START", "false");

        if (POSITION > 5) {
            FinishFragment returnToGloHomeFragment = new FinishFragment();
            Bundle bundle2 = new Bundle();
            bundle2.putString("FROM", "global");
            returnToGloHomeFragment.setArguments(bundle2);
            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();
        } else {
            GloFragment gloCameraFragment = new GloFragment();
            gloCameraFragment.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .remove(GloFragment.this) // "this" refers to current instance of Fragment2
                    .commit();
            fragmentManager.popBackStack();
            fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, gloCameraFragment).addToBackStack(null).commit();
        }
    }

    private void initViews() {
        views.groupPreview.setVisibility(View.GONE);
    }

    private void sendImg(String fileName, String path){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType medsendImgiaType = MediaType.parse("text/plain");
        File file = new File(path, fileName);
        RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("skin_img",fileName, imageBody)
                .build();
        Request request = new Request.Builder()
                .url("http://3.141.4.121:19014/upload-image")
                .method("POST", body)
                .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String jsonData = response.body().string();
                    getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        views.txtGuide.setText(jsonData);
                    }
                    }
                    );
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }
            });
//            Log.e("cekini", response.body().string());
//            getActivity().runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        views.txtGuide.setText(response.body().string());
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }});
////            hasil = response.body().string();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initListeners() {
        views.captureImage.setOnClickListener(view -> takePhoto());
//        views.btCancel.setOnClickListener(this::onCancelShoot);
        views.btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                views.mainConstraint.setVisibility(View.VISIBLE);
                views.groupPreview.setVisibility(View.GONE);
                views.llSpotSelection.setVisibility(View.GONE);
                views.closeupHint.setVisibility(View.GONE);
                views.preview.setVisibility(View.INVISIBLE);
                views.txtPositionName.setText("Pick Position");
                views.imgPosition.setImageDrawable(getResources().getDrawable(R.drawable.male_head_frontal));
                imageView.undo();
                imageView.changeCondition();
                imageView.changeLocation();

                views.btNext.setVisibility(View.GONE);
            }
        });
        views.btCancel.setText("Go Back");
        views.btRetake.setOnClickListener(view -> viewModel.retake());
        views.btNext.setOnClickListener(view -> next());
        viewModel.getCaptureStateLiveData().observe(getViewLifecycleOwner(), this::handleCaptureStateChange);
        viewModel.getCaptureCompleteLiveData().observe(getViewLifecycleOwner(), this::handleCaptureUploadChange);
        viewModel.getEndProcessData().observe(getViewLifecycleOwner(), this::handleEndProcessResponse);
        views.btEnd.setVisibility(View.VISIBLE);
        views.btEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FragmentTransaction ft;
//                ft = getFragmentManager().beginTransaction();
//
//                ReturnToGloHomeFragment returnToGloHomeFragment = new ReturnToGloHomeFragment();
//                ft.replace(R.id.nav_host_fragment, returnToGloHomeFragment);
//                ft.commit();
            }
        });
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

//        views.btEnd.setOnClickListener(this::onEndShoot);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void next() {
        if (first) {
            first = false;
        }

        if (!imageView.getCondition()) {
            viewModel.getCaptureStateLiveData().observe(getViewLifecycleOwner(), this::handleCaptureStateChange2);
            try {
//            viewModel.onNext(mainViewModel.getHairAnalysisID());
                views.mainConstraint.setVisibility(View.VISIBLE);
                views.groupPreview.setVisibility(View.GONE);
                views.llSpotSelection.setVisibility(View.GONE);
                views.closeupHint.setVisibility(View.GONE);
                views.preview.setVisibility(View.INVISIBLE);
                views.btEnd.setVisibility(View.VISIBLE);

                //HERE TO GET DOT COORDINATE
                ArrayList<Integer> coordinateData = new ArrayList<Integer>(imageView.getData());
                latestCoordinateX = coordinateData.get(0) / (double) imageView.getWidth() * 100 / 100;
                latestCoordinateY = coordinateData.get(1) / (double) imageView.getHeight() * 100 / 100;
                imageView.changeCondition();
                imageView.changeLocation();

                views.btNext.setVisibility(View.GONE);
                views.btCancel.setVisibility(View.INVISIBLE);
                views.btCancel.setText("Undo");
                views.btCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageView.undo();
                        views.btCancel.setVisibility(View.INVISIBLE);
                        views.btNext.setVisibility(View.INVISIBLE);
                    }
                });
                views.btEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FinishFragment returnToGloHomeFragment = new FinishFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("FROM", from);
                        returnToGloHomeFragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().popBackStack();
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();
                    }
                });

                RequestBody imageBody = RequestBody.create(file, MediaType.parse("image/jpg"));
                MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), imageBody);
                MultipartBody.Part captureType = MultipartBody.Part.createFormData("type", "closeup");
                MultipartBody.Part faceArea = MultipartBody.Part.createFormData("area", imageView.getPosition());
                MultipartBody.Part brownSpot = MultipartBody.Part.createFormData("is_brown_spot", views.cbBrownspot.isChecked() ? "1" : "0");
                MultipartBody.Part redSpot = MultipartBody.Part.createFormData("is_red_spot", views.cbRedspot.isChecked() ? "1" : "0");
                MultipartBody.Part pores = MultipartBody.Part.createFormData("is_pores", views.cbPores.isChecked() ? "1" : "0");
                MultipartBody.Part facePosition = MultipartBody.Part.createFormData("position", "front");
                MultipartBody.Part xAxis = MultipartBody.Part.createFormData("x_axis", String.valueOf(latestCoordinateX));
                MultipartBody.Part yAxis = MultipartBody.Part.createFormData("y_axis", String.valueOf(latestCoordinateY));
                mainViewModel.addImage(file.getAbsolutePath());
                viewModel.saveFaceLocalImage(captureType, facePosition, faceArea, imagePart, xAxis, yAxis, brownSpot, redSpot, pores, mainViewModel.getFaceAnalysisID());

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            viewModel.init(getActivity(), mainViewModel.getSelectedCloseupFacePositions(), mainViewModel, views);
            views.closeupHint.setVisibility(View.VISIBLE);
            views.preview.setVisibility(View.VISIBLE);
            views.btEnd.setVisibility(View.GONE);
            views.clCloseImgSelection.setVisibility(View.GONE);
            views.txtPositionName.setText("Take a Picture");
            views.captureImage.setVisibility(View.VISIBLE);
            initViews();
            initListeners();
            imageView.changeCondition();
            imageView.changeLocation();
            views.previewView.setVisibility(View.INVISIBLE);
            views.mainConstraint.setVisibility(View.GONE);
        }
    }

    private void handleCaptureUploadChange(AsyncResponse<Boolean, Exception> response) {
        if (response.isFresh()) {
            response.pop();
            if (response.isNotStarted()) {
                isCaptured = false;
                views.btNext.setVisibility(View.GONE);
                views.progressBar.setVisibility(View.GONE);
            } else if (response.isError()) {
                Toast.makeText(getContext(), response.error.getMessage(), Toast.LENGTH_LONG).show();
                views.btNext.setVisibility(View.VISIBLE);
                views.progressBar.setVisibility(View.GONE);
            } else if (response.isLoading()) {
                views.btNext.setVisibility(View.GONE);
                views.progressBar.setVisibility(View.VISIBLE);
            } else if (response.isSuccess()) {
                FinishFragment returnToGloHomeFragment = new FinishFragment();
                Bundle bundle = new Bundle();
                bundle.putString("FROM", from);
                returnToGloHomeFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, returnToGloHomeFragment).addToBackStack(null).commit();
            }
        }
    }

    private void onCancelShoot(View view) {
        getActivity().onBackPressed();
    }

    private void onEndShoot(View view) {
        if (isCaptured) {
//            imageView.backToDefault();
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

    private void handleAutoCaptureStateChange(CameraAutoCaptureState state) {
        views.txtGuide.setText(state.getTextGuide());
    }

    private void handleCaptureStateChange2(FaceCameraCaptureState state) {
        views.txtPositionName.setText("Pick Position");
        Glide.with(this)
                .load(R.drawable.male_head_frontal)
                .into(views.imgPosition);
        views.captureImage.setVisibility(View.GONE);
    }

    private void handleCaptureStateChange(FaceCameraCaptureState state) {
        final Patient patient = mainViewModel.getSelectedPatient();
        position = state.getPosition();
        toggleCapturedPreview(state.isPreview());

        //For Camera Overlay
        if (state.isPreview()) {
            Glide.with(this)
                    .load(state.getPath())
                    .centerCrop()
                    .into(views.imgCapturePreview);
            try {
                file = new File(new URI(state.getPath()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            switch (imageView.getPosition()) {
                case "forehead_center": {
                    Glide.with(this)
                            .load(R.drawable.male_head_frontal_glo)
                            .into(views.imgPosition);
                    views.txtPositionName.setText("forehead center");
                    break;
                }
                case "forehead_right": {
                    Glide.with(this)
                            .load(R.drawable.male_forehead_right)
                            .into(views.imgPosition);
                    views.txtPositionName.setText("forehead right");
                    break;
                }
                case "forehead_left": {
                    Glide.with(this)
                            .load(R.drawable.male_forehead_left)
                            .into(views.imgPosition);
                    views.txtPositionName.setText("forehead left");
                    break;
                }
                case "under_eye_right": {
                    Glide.with(this)
                            .load(R.drawable.male_head_frontal_leye)
                            .into(views.imgPosition);
                    views.txtPositionName.setText("under eye right");
                    break;
                }
                case "under_eye_left": {
                    Glide.with(this)
                            .load(R.drawable.male_head_frontal_reye)
                            .into(views.imgPosition);
                    views.txtPositionName.setText("under eye left");
                    break;
                }
                case "upper_cheek_right": {
                    Glide.with(this)
                            .load(R.drawable.male_upper_cheek_right)
                            .into(views.imgPosition);
                    views.txtPositionName.setText("upper cheek right");
                    break;
                }
                case "upper_cheek_left": {
                    Glide.with(this)
                            .load(R.drawable.male_upper_cheek_left)
                            .into(views.imgPosition);
                    views.txtPositionName.setText("upper cheek left");
                    break;
                }
                case "lower_cheek_right": {
                    Glide.with(this)
                            .load(R.drawable.male_lower_cheek_right)
                            .into(views.imgPosition);
                    views.txtPositionName.setText("lower cheek right");
                    break;
                }
                case "lower_cheek_left": {
                    Glide.with(this)
                            .load(R.drawable.male_lower_cheek_left)
                            .into(views.imgPosition);
                    views.txtPositionName.setText("lower cheek left");
                    break;
                }
                case "chin": {
                    Glide.with(this)
                            .load(R.drawable.male_chin)
                            .into(views.imgPosition);
                    views.txtPositionName.setText(imageView.getPosition());
                    break;
                }
                case "nose": {
                    Glide.with(this)
                            .load(R.drawable.male_nose)
                            .into(views.imgPosition);
                    views.txtPositionName.setText(imageView.getPosition());
                    break;
                }
            }
//            Glide.with(this)
//                    .load(position.getImage(!patient.isFemale()))
//                    .into(views.imgPosition); //if position is hair position show image on hairImgPosition View or else imgPosition View

            setOverlay(position);
            startCamera(position.zoom);
        }

        if (from.equals("closeup")) {
            views.previewView.setVisibility(View.VISIBLE);
            views.txtType.setText("Close Up");
        } else {
            views.previewView.setVisibility(View.VISIBLE);
            views.txtType.setText("Global");
        }
    }

    private void setOverlay(CameraPositions position) {
        switch (position.position) {
            case CameraPositions.Positions.FRONTAL:
                views.ovFront.setVisibility(View.VISIBLE);
                views.ovL.setVisibility(View.GONE);
                views.ovR.setVisibility(View.GONE);
                break;
            case CameraPositions.Positions.RIGHT:
                views.ovFront.setVisibility(View.GONE);
                views.ovL.setVisibility(View.GONE);
                views.ovR.setVisibility(View.VISIBLE);
                break;
            case CameraPositions.Positions.LEFT:
                views.ovFront.setVisibility(View.GONE);
                views.ovL.setVisibility(View.VISIBLE);
                views.ovR.setVisibility(View.GONE);
                break;
        }
    }

    private void toggleCapturedPreview(boolean showCapturedImage) {
        //remove hair overlay, because we are making it visible in handleCaptureState anyway (if needed).
        views.groupPreview.setVisibility(showCapturedImage ? View.VISIBLE : View.GONE);
        if (showCapturedImage)
            GloCameraUtils.Companion.getGetCameraProvider().unbindAll();

        views.captureImage.setClickable(!showCapturedImage);
        views.captureImage.setVisibility(showCapturedImage ? View.GONE : View.VISIBLE);
        views.btNext.setVisibility(showCapturedImage ? View.VISIBLE : View.GONE);
        if (from.equals("closeup")) {
            views.llSpotSelection.setVisibility(showCapturedImage ? View.VISIBLE : View.GONE);
            views.cbBrownspot.setChecked(false);
            views.cbRedspot.setChecked(false);
            views.cbPores.setChecked(false);
        }
        isCaptured = showCapturedImage ? true : false;
    }

    private void startCamera(float zoom) {
        if (Utils.checkCameraPermission(getContext())) {
            if (Utils.checkStoragePermission(getContext())) {
                //Start Camera after checking the permission
                GloCameraUtils.Companion.startCamera(getContext(), getViewLifecycleOwner(), views.preview, zoom, position).observe(getViewLifecycleOwner(), viewModel::updateAutoCaptureState);
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
        GloCameraUtils.Companion.takePhoto(getContext()).observe(getViewLifecycleOwner(), viewModel::onImageCaptured);
    }

    private enum InputType {
        UNKNOWN,
        IMAGE,
        CAMERA
    }

}