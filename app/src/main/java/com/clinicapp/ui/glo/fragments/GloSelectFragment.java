//package com.clinicapp.ui.glo.fragments;
//
//import android.content.SharedPreferences;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Matrix;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import android.os.Environment;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import com.clinicapp.R;
//import com.clinicapp.databinding.FragmentGloBinding;
//import com.clinicapp.ui.glo.dialog.SheetChoice;
//import com.clinicapp.utilities.FaceMeshResultImageView;
//import com.google.mediapipe.solutions.facemesh.FaceMesh;
//import com.google.mediapipe.solutions.facemesh.FaceMeshOptions;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.RequestBody;
//
//public class GloSelectFragment extends Fragment {
//
//    private FragmentGloBinding views;
//    private FaceMeshResultImageView imageView;
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_glo_select, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        setupStaticImageDemoUiComponents(fileName, true);
//    }
//
//    /**
//     * Sets up the UI components for the static image demo.
//     */
//    private void setupStaticImageDemoUiComponents(String file_name, Boolean face_mesh) {
//        imageView = new FaceMeshResultImageView(getContext());
//        imageView.position(POSITION);
//        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//
//        if (face_mesh) {
//            stopCurrentPipeline();
//            setupStaticImageModePipeline(file_name);
//        }
//        String qrPath = Environment.getExternalStorageDirectory() + "/Pictures/";
//        Log.e("path", qrPath);
//        File imgFile = new File(qrPath, file_name);
//        Bitmap bitmap = BitmapFactory.decodeFile(qrPath + file_name);
//        bitmap = manageOrentation(imgFile, bitmap);
//        bitmap = downscaleBitmap(bitmap);
//        Matrix matrix = new Matrix();
//        matrix.postScale(1f, 1f);
//        int newW = (int) (bitmap.getWidth() * 0.86);
//        int newH = (int) (bitmap.getHeight() * 0.76);
//        int marginW = (int) (bitmap.getWidth() * 0.08);
//        int marginH = (int) (bitmap.getHeight() * 0.12);
//        bitmap = Bitmap.createBitmap(bitmap, marginW, marginH, newW, newH, matrix, true);
//        facemesh.send(bitmap);
//    }
//
//    /**
//     * Sets up core workflow for static image mode.
//     */
//    private void setupStaticImageModePipeline(String file_name) {
//        this.inputType = GloFragment.InputType.IMAGE;
//        // Initializes a new MediaPipe Face Mesh solution instance in the static image mode.
//        facemesh =
//                new FaceMesh(
//                        mContext.getApplicationContext(),
//                        FaceMeshOptions.builder()
//                                .setStaticImageMode(true)
//                                .setRefineLandmarks(true)
//                                .setRunOnGpu(RUN_ON_GPU)
//                                .build());
//
//        // Connects MediaPipe Face Mesh solution to the user-defined FaceMeshResultImageView.
//        facemesh.setResultListener(
//                faceMeshResult -> {
//                    logNoseLandmark(faceMeshResult, /*showPixelValues=*/ true);
//                    imageView.setFaceMeshResult(faceMeshResult);
//                    imageView.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View v, MotionEvent event) {
//                            if (event.getAction() == MotionEvent.ACTION_UP) {
//                                SheetChoice bottomSheetDialogFragment = new SheetChoice();
//                                bottomSheetDialogFragment.setCancelable(false);
//                                bottomSheetDialogFragment.show(getChildFragmentManager(), "");
//
////                                viewModel.init(getActivity(), mainViewModel.getSelectedCloseupFacePositions(), mainViewModel, views);
////                                views.closeupHint.setVisibility(View.VISIBLE);
////                                views.clCloseImgSelection.setVisibility(View.GONE);
////                                initViews();
////                                initListeners();
//                            }
//                            return false;
//                        }
//                    });
//                    getActivity().runOnUiThread(() -> imageView.update());
//
////                    Bitmap bmInput = faceMeshResult.inputBitmap();
////                    int width = bmInput.getWidth();
////                    int height = bmInput.getHeight();
////                    Bitmap latest = Bitmap.createBitmap(width, height, bmInput.getConfig());
////                    Canvas canvas = new Canvas(latest);
//                });
//        facemesh.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Face Mesh error:" + message));
//
//        // Updates the preview layout.
//        frameLayout.removeAllViewsInLayout();
//        imageView.setImageDrawable(null);
//        frameLayout.addView(imageView);
//        imageView.setVisibility(View.VISIBLE);
//
//        views.btNext.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                SharedPreferences.Editor editor = sharedPref.edit();
//                editor.putInt("remove", 1);
//                editor.apply();
//
//                FileOutputStream outStream = null;
//                File sdCard = Environment.getExternalStorageDirectory();
//                String name = String.valueOf(System.currentTimeMillis());
//
//                File outFile = new File(sdCard.toString() + "/Pictures", name+"_"+POSITION + ".jpeg");
//                File imageNoGrid = new File(sdCard.toString() + "/Pictures", file_name);
//                try {
//                    outStream = new FileOutputStream(outFile);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                Bitmap bitmap = viewToBitmap(imageView);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
////                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
////                Bitmap bitmap = drawable.getBitmap();
////                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
//                try {
//                    outStream.flush();
//                    Toast.makeText(getActivity().getApplicationContext(), "Saved ", Toast.LENGTH_SHORT).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    outStream.close();
//                    RequestBody imageBody = RequestBody.create(imageNoGrid, MediaType.parse("image/jpeg"));
//                    MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", imageNoGrid.getName(), imageBody);
//                    MultipartBody.Part captureType = MultipartBody.Part.createFormData("type", "global");
//                    MultipartBody.Part facePosition = MultipartBody.Part.createFormData("position", "front");
////                    viewModel.saveFaceGlobalImage(captureType, facePosition, imagePart,imagePart,imagePart, mainViewModel.getFaceAnalysisID());
//                    nextEffect();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }
//
//    public Bitmap viewToBitmap(View view) {
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        view.draw(canvas);
//        return bitmap;
//    }
//}