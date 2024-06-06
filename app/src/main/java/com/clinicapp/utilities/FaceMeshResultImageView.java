// Copyright 2021 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.clinicapp.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;

import com.clinicapp.App;
import com.google.common.collect.ImmutableSet;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.solutions.facemesh.FaceMeshConnections;
import com.google.mediapipe.solutions.facemesh.FaceMeshResult;

import java.util.ArrayList;
import java.util.List;

/**
 * An ImageView implementation for displaying {@link FaceMeshResult}.
 */
public class FaceMeshResultImageView extends AppCompatImageView {
    private static final String TAG = "FaceMeshResultImageView";
    Context mContext;

    private static final int[] LEFT_EYE_FACE_MESH_INDEX = new int[]{417, 441, 442, 443, 444, 445, 342, 446, 255, 339, 254, 253, 252, 256, 341, 463, 417};
    private static final int[] RIGHT_EYE_FACE_MESH_INDEX = new int[]{193, 221, 222, 223, 224, 225, 113, 226, 25, 110, 24, 23, 22, 26, 112, 243, 193};
    private static final int[][] EYES_FACE_MESH_INDEX = new int[][]{LEFT_EYE_FACE_MESH_INDEX, RIGHT_EYE_FACE_MESH_INDEX};
    private static final int[][] EYE_RIGHT_FACE_MESH_INDEX = new int[][]{RIGHT_EYE_FACE_MESH_INDEX};
    private static final int[][] EYE_LEFT_FACE_MESH_INDEX = new int[][]{LEFT_EYE_FACE_MESH_INDEX};
    private static final int[] NOSE_FACE_MESH_INDEX = new int[]{8, 417, 399, 420, 279, 331, 327, 326, 2, 97, 98, 64, 102, 49, 198, 174, 193, 8};
    private static final int[] NOSE_FACE_RIGHT_MESH_INDEX = new int[]{8, 193, 174, 198, 49, 102, 64, 98, 97, 2, 94, 19, 1, 4, 5, 195, 197, 6, 168, 8};
    private static final int[] NOSE_FACE_LEFT_MESH_INDEX = new int[]{8, 417, 399, 420, 279, 331, 327, 326, 2, 94, 19, 1, 4, 5, 195, 197, 6, 168, 8};
    private static final int[] MOUTH_FACE_MESH_INDEX = new int[]{57, 185, 40, 39, 37, 0, 267, 269, 270, 409, 287, 375, 321, 405, 314, 17, 84, 181, 91, 146, 57};
    private static final int[] FACE_CONTOUR_INDEX = new int[]{10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400, 377, 152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109, 10};
    private static final int[] FACE_LEFT_CONTOUR_INDEX = new int[]{8, 67, 109, 10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400, 377, 152, 148};
    private static final int[] FACE_RIGHT_CONTOUR_INDEX = new int[]{377, 152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109, 10, 338, 297, 8};
    private static final int[][] MOUTH_NOSE_FACE_MESH_INDEX = new int[][]{NOSE_FACE_MESH_INDEX, MOUTH_FACE_MESH_INDEX, FACE_CONTOUR_INDEX};
    private static final int[][] LEFT_MOUTH_NOSE_FACE_MESH_INDEX = new int[][]{NOSE_FACE_LEFT_MESH_INDEX, MOUTH_FACE_MESH_INDEX, FACE_LEFT_CONTOUR_INDEX};
    private static final int[][] RIGHT_MOUTH_NOSE_FACE_MESH_INDEX = new int[][]{NOSE_FACE_RIGHT_MESH_INDEX, MOUTH_FACE_MESH_INDEX, FACE_RIGHT_CONTOUR_INDEX};
    private static final int[][] TO_MOUTH_MESH_INDEX = new int[][]{new int[]{176, 211, 57}, new int[]{400, 431, 287}, new int[]{57, 98}, new int[]{287, 327}};
    private static final int[][] VERTICAL_MESH_INDEX = new int[][]{new int[]{223, 109}, new int[]{443, 338}};
    private static final int[][] VERTICAL_RIGHT_MESH_INDEX = new int[][]{new int[]{223, 109}};
    private static final int[][] VERTICAL_LEFT_MESH_INDEX = new int[][]{new int[]{443, 338}};
    private static final int[][] HORIZONTAL_MESH_INDEX = new int[][]{new int[]{226, 143, 127}, new int[]{446, 372, 356}, new int[]{102, 93}, new int[]{278, 323}, new int[]{57, 58}, new int[]{287, 288}};
    private static final int[][] HORIZONTAL_LEFT_MESH_INDEX = new int[][]{new int[]{446, 372, 356}, new int[]{278, 323}, new int[]{287, 288}};
    private static final int[][] HORIZONTAL_RIGHT_MESH_INDEX = new int[][]{new int[]{226, 143, 127}, new int[]{102, 93}, new int[]{57, 58}};

    private static final int FACE_MESH_COLOR = Color.parseColor("#ff2ae9e7");
    private static final int NOSE_LANDMARK_INDEX = 4;
    private static final int CHIN_LANDMARK_INDEX = 175;
    private static final int EYES_BRIDGE_LANDMARK_INDEX = 8;
    private static final int LEFT_EYE_LANDMARK_INDEX = 359;
    private static final int RIGHT_EYE_LANDMARK_INDEX = 130;

    private static final int CONTOUR_THICKNESS = 4;
    SharedPreferences sharedPref;

    private static final int TESSELATION_COLOR = Color.parseColor("#70C0C0C0");
    private static final int TESSELATION_THICKNESS = 3; // Pixels
    private static final int RIGHT_EYE_COLOR = Color.parseColor("#FF3030");
    private static final int RIGHT_EYE_THICKNESS = 5; // Pixels
    private static final int RIGHT_EYEBROW_COLOR = Color.parseColor("#FF3030");
    private static final int RIGHT_EYEBROW_THICKNESS = 5; // Pixels
    private static final int LEFT_EYE_COLOR = Color.parseColor("#30FF30");
    private static final int LEFT_EYE_THICKNESS = 5; // Pixels
    private static final int LEFT_EYEBROW_COLOR = Color.parseColor("#30FF30");
    private static final int LEFT_EYEBROW_THICKNESS = 5; // Pixels
    private static final int FACE_OVAL_COLOR = Color.parseColor("#E0E0E0");
    private static final int FACE_OVAL_THICKNESS = 5; // Pixels
    private static final int LIPS_COLOR = Color.parseColor("#E0E0E0");
    private static final int LIPS_THICKNESS = 5; // Pixels
    private Bitmap latest;
    private static int POSITION = 0;
    boolean condition = false;
    String location = "select";
    String position;
    List<String> dots = new ArrayList<String>();
    List<Float> forehead_center_x;
    //forehead center
    float y10;
    float y8;
    float x223;
    float x443;
    //forehead right
    float y103;
    float y27;
    float x21;
    float x109;
    //forehead left
    float y284;
    float y356;
    float x338;
    float x284;
    //under eye right
//    float y27;
    float y93;
    float x234;
    float x173;
    //undereye left
    float y341;
    float y331;
    float x279;
    float x454;
    //lower cheek right
    float y58;
    float y176;
    float x58;
    float x176;
    //lower cheek left
    float y288;
    float y400;
    float x400;
    float x288;
    //upper cheek left
    float y323;
    float y287;
    float x327;
    float x361;
    //upper cheek right
    float x132;
    float x98;
    //    float y93;
//    float y57;
    //chin
    float y57;
    float y152;
    float x211;
    float x431;
    //nose
//    float y8;
    float y2;
    float x102;
    float x331;

    int width;
    int height;
    boolean firstIn = true;

    private OnClickListener listener;

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public FaceMeshResultImageView(Context context) {
        super(context);
        mContext = context;
        setScaleType(ScaleType.FIT_CENTER);
        setDrawingCacheEnabled(true);

        App app = (App) getContext().getApplicationContext();
        sharedPref = app.getSharedPrefs();
    }

//    public void first(boolean first) {
//        firstIn = first;
//    }

    public void position(int i) {
        POSITION = i;
    }

    Canvas canvas;

    public boolean getCondition() {
        return condition;
    }

    /**
     * Sets a {@link FaceMeshResult} to render.
     *
     * @param result a {@link FaceMeshResult} object that contains the solution outputs and the input
     *               {@link Bitmap}.
     */
    public void setFaceMeshResult(FaceMeshResult result) {
        if (result == null) {
            return;
        }
        Bitmap bmInput = result.inputBitmap();
        width = bmInput.getWidth();
        height = bmInput.getHeight();
        latest = Bitmap.createBitmap(width, height, bmInput.getConfig());
        Canvas canvas = new Canvas(latest);
        Size imageSize = new Size(width, height);
        canvas.drawBitmap(bmInput, new Matrix(), null);
        int numFaces = result.multiFaceLandmarks().size();
        for (int i = 0; i < numFaces; ++i) {
            LandmarkProto.NormalizedLandmark _y10 = result.multiFaceLandmarks().get(i).getLandmarkList().get(10);
            LandmarkProto.NormalizedLandmark _y8 = result.multiFaceLandmarks().get(i).getLandmarkList().get(8);
            LandmarkProto.NormalizedLandmark _y103 = result.multiFaceLandmarks().get(i).getLandmarkList().get(103);
            LandmarkProto.NormalizedLandmark _y27 = result.multiFaceLandmarks().get(i).getLandmarkList().get(27);
            LandmarkProto.NormalizedLandmark _y284 = result.multiFaceLandmarks().get(i).getLandmarkList().get(284);
            LandmarkProto.NormalizedLandmark _y356 = result.multiFaceLandmarks().get(i).getLandmarkList().get(356);
            LandmarkProto.NormalizedLandmark _y93 = result.multiFaceLandmarks().get(i).getLandmarkList().get(93);
            LandmarkProto.NormalizedLandmark _y341 = result.multiFaceLandmarks().get(i).getLandmarkList().get(341);
            LandmarkProto.NormalizedLandmark _y331 = result.multiFaceLandmarks().get(i).getLandmarkList().get(331);
            LandmarkProto.NormalizedLandmark _y58 = result.multiFaceLandmarks().get(i).getLandmarkList().get(58);
            LandmarkProto.NormalizedLandmark _y176 = result.multiFaceLandmarks().get(i).getLandmarkList().get(176);
            LandmarkProto.NormalizedLandmark _y288 = result.multiFaceLandmarks().get(i).getLandmarkList().get(288);
            LandmarkProto.NormalizedLandmark _y400 = result.multiFaceLandmarks().get(i).getLandmarkList().get(400);
            LandmarkProto.NormalizedLandmark _y57 = result.multiFaceLandmarks().get(i).getLandmarkList().get(57);
            LandmarkProto.NormalizedLandmark _y152 = result.multiFaceLandmarks().get(i).getLandmarkList().get(152);
            LandmarkProto.NormalizedLandmark _y323 = result.multiFaceLandmarks().get(i).getLandmarkList().get(323);
            LandmarkProto.NormalizedLandmark _y287 = result.multiFaceLandmarks().get(i).getLandmarkList().get(287);
            LandmarkProto.NormalizedLandmark _y2 = result.multiFaceLandmarks().get(i).getLandmarkList().get(2);

            y10 = _y10.getY();
            y8 = _y8.getY();
            y103 = _y103.getY();
            y27 = _y27.getY();
            y284 = _y284.getY();
            y356 = _y356.getY();
            y93 = _y93.getY();
            y341 = _y341.getY();
            y331 = _y331.getY();
            y58 = _y58.getY();
            y176 = _y176.getY();
            y288 = _y288.getY();
            y400 = _y400.getY();
            y57 = _y57.getY();
            y152 = _y152.getY();
            y323 = _y323.getY();
            y287 = _y287.getY();
            y2 = _y2.getY();
//            forehead_center_y.add((Float) forehead_center_y_1.getY());
//            forehead_center_y.add(1, forehead_center_y_2.getY());
            LandmarkProto.NormalizedLandmark _x223 = result.multiFaceLandmarks().get(i).getLandmarkList().get(223);
            LandmarkProto.NormalizedLandmark _x443 = result.multiFaceLandmarks().get(i).getLandmarkList().get(443);
            LandmarkProto.NormalizedLandmark _x21 = result.multiFaceLandmarks().get(i).getLandmarkList().get(21);
            LandmarkProto.NormalizedLandmark _x109 = result.multiFaceLandmarks().get(i).getLandmarkList().get(109);
            LandmarkProto.NormalizedLandmark _x338 = result.multiFaceLandmarks().get(i).getLandmarkList().get(338);
            LandmarkProto.NormalizedLandmark _x284 = result.multiFaceLandmarks().get(i).getLandmarkList().get(284);
            LandmarkProto.NormalizedLandmark _x234 = result.multiFaceLandmarks().get(i).getLandmarkList().get(234);
            LandmarkProto.NormalizedLandmark _x173 = result.multiFaceLandmarks().get(i).getLandmarkList().get(173);
            LandmarkProto.NormalizedLandmark _x279 = result.multiFaceLandmarks().get(i).getLandmarkList().get(279);
            LandmarkProto.NormalizedLandmark _x454 = result.multiFaceLandmarks().get(i).getLandmarkList().get(454);
            LandmarkProto.NormalizedLandmark _x58 = result.multiFaceLandmarks().get(i).getLandmarkList().get(58);
            LandmarkProto.NormalizedLandmark _x176 = result.multiFaceLandmarks().get(i).getLandmarkList().get(176);
            LandmarkProto.NormalizedLandmark _x400 = result.multiFaceLandmarks().get(i).getLandmarkList().get(400);
            LandmarkProto.NormalizedLandmark _x288 = result.multiFaceLandmarks().get(i).getLandmarkList().get(288);
            LandmarkProto.NormalizedLandmark _x211 = result.multiFaceLandmarks().get(i).getLandmarkList().get(211);
            LandmarkProto.NormalizedLandmark _x431 = result.multiFaceLandmarks().get(i).getLandmarkList().get(431);
            LandmarkProto.NormalizedLandmark _x132 = result.multiFaceLandmarks().get(i).getLandmarkList().get(132);
            LandmarkProto.NormalizedLandmark _x98 = result.multiFaceLandmarks().get(i).getLandmarkList().get(98);
            LandmarkProto.NormalizedLandmark _x327 = result.multiFaceLandmarks().get(i).getLandmarkList().get(327);
            LandmarkProto.NormalizedLandmark _x361 = result.multiFaceLandmarks().get(i).getLandmarkList().get(361);
            LandmarkProto.NormalizedLandmark _x102 = result.multiFaceLandmarks().get(i).getLandmarkList().get(102);
            LandmarkProto.NormalizedLandmark _x331 = result.multiFaceLandmarks().get(i).getLandmarkList().get(331);
            x223 = _x223.getX();
            x443 = _x443.getX();
            x21 = _x21.getX();
            x109 = _x109.getX();
            x338 = _x338.getX();
            x284 = _x284.getX();
            x234 = _x234.getX();
            x173 = _x173.getX();
            x279 = _x279.getX();
            x454 = _x454.getX();
            x58 = _x58.getX();
            x176 = _x176.getX();
            x400 = _x400.getX();
            x288 = _x288.getX();
            x211 = _x211.getX();
            x431 = _x431.getX();
            x132 = _x132.getX();
            x98 = _x98.getX();
            x327 = _x327.getX();
            x361 = _x361.getX();
            x102 = _x102.getX();
            x331 = _x331.getX();
//            forehead_center_x.add(0,forehead_center_x_1.getX());
//            forehead_center_x.add(1,forehead_center_x_2.getX());
            drawConnectionFromIndex(
                    canvas,
                    imageSize,
                    result.multiFaceLandmarks().get(i).getLandmarkList(),
                    TO_MOUTH_MESH_INDEX,
                    FACE_MESH_COLOR,
                    CONTOUR_THICKNESS);
            switch (POSITION) {
                case 0:
                case 1: {
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            EYES_FACE_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            MOUTH_NOSE_FACE_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            VERTICAL_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);

                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            HORIZONTAL_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    break;
                }
                case 4: {
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            EYE_LEFT_FACE_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            LEFT_MOUTH_NOSE_FACE_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            VERTICAL_LEFT_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            HORIZONTAL_LEFT_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    break;
                }
                case 5: {
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            EYE_RIGHT_FACE_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            RIGHT_MOUTH_NOSE_FACE_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            VERTICAL_RIGHT_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    drawConnectionFromIndex(
                            canvas,
                            imageSize,
                            result.multiFaceLandmarks().get(i).getLandmarkList(),
                            HORIZONTAL_RIGHT_MESH_INDEX,
                            FACE_MESH_COLOR,
                            CONTOUR_THICKNESS);
                    break;
                }
            }
        }
    }


    /**
     * Updates the image view with the latest {@link FaceMeshResult}.
     */
    public void update() {
        postInvalidate();
        if (latest != null) {
            setImageBitmap(latest);
        }
    }

    public String changeLocation() {
        if (location.equals("camera")) {
            return "select";
        } else {
            return "camera";
        }
    }

    public String getLocation() {
        return location;
    }

    private void drawConnectionFromIndex(
            Canvas canvas,
            Size imageSize,
            List<NormalizedLandmark> faceLandmakrList,
            int[][] landmarkIndex,
            int color,
            int thickness
    ) {
        NormalizedLandmark start;
        NormalizedLandmark end;

        for (int l = 0; l < landmarkIndex.length; l++) {
            for (int i = 0; i < landmarkIndex[l].length; i++) {
                Paint connectionPaint = new Paint();
                start = faceLandmakrList.get(landmarkIndex[l][i]);
                if (i <= landmarkIndex[l].length - 2) {
                    end = faceLandmakrList.get(landmarkIndex[l][i + 1]);
                    connectionPaint.setColor(color);
                    connectionPaint.setStrokeWidth(thickness);
                    Log.e("cek dalem", String.valueOf(start.getX()+" "+imageSize.getWidth()));
                    canvas.drawLine(
                            start.getX() * imageSize.getWidth(),
                            start.getY() * imageSize.getHeight(),
                            end.getX() * imageSize.getWidth(),
                            end.getY() * imageSize.getHeight(),
                            connectionPaint);
                }
            }
        }
    }

    private void drawLandmarksOnCanvas(
            Canvas canvas,
            List<NormalizedLandmark> faceLandmarkList,
            ImmutableSet<FaceMeshConnections.Connection> connections,
            Size imageSize,
            int color,
            int thickness) {
        // Draw connections.
        for (FaceMeshConnections.Connection c : connections) {
            Paint connectionPaint = new Paint();
            connectionPaint.setColor(color);
            connectionPaint.setStrokeWidth(thickness);
            NormalizedLandmark start = faceLandmarkList.get(c.start());
            NormalizedLandmark end = faceLandmarkList.get(c.end());
            canvas.drawLine(
                    start.getX() * imageSize.getWidth(),
                    start.getY() * imageSize.getHeight(),
                    end.getX() * imageSize.getWidth(),
                    end.getY() * imageSize.getHeight(),
                    connectionPaint);
        }
    }

    private int x = 0;
    private int y = 0;
    private Paint paint = new Paint();
    private float radius = 20;
    List<List<Integer>> draw = new ArrayList<List<Integer>>();
    int state = -2;

    public int getState() {
        return state;
    }

    public void removeLast() {
//        int index = draw.size() - 1;
//        draw.remove(index);
        state -= 1;
    }

    public void changeCondition() {
        condition = !condition;
        Log.e("xyCondition2", "" + condition);
    }

    public void backToDefault() {
        condition = false;
        draw.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (POSITION == 0){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = (int) event.getX();
                y = (int) event.getY();
                Log.e("xyCondition", "" + condition);
                if (!condition) {
                    float xper = (float) x / (float) 1295 * 100 / 100;
                    float yper = (float) y / (float) 1582 * 100 / 100;
//                    Log.e("masuk", latest.getWidth()+" "+x+" "+xper+" "+x338*latest.getWidth()+" "+x284+" "+latest.getHeight()+" "+y+" "+yper+" "+y284*latest.getHeight()+" "+y356);
                    if (xper > x223 && xper < x443 && yper > y10 && yper < y8) {
                        Log.e("masuk", "forehead center");
                        position = "forehead_center";
                        draw();
                    } else if (xper > x21 && xper < x109 && yper > y103 && yper < y27) {
                        Log.e("masuk", "forehead right");
                        position = "forehead_right";
                        draw();
                    } else if (xper > x338 && xper < x284 && yper > y284 && yper < y356) {
                        Log.e("masuk", "forehead left");
                        position = "forehead_left";
                        draw();
                    } else if (xper > x234 && xper < x173 && yper > y27 && yper < y93) {
                        Log.e("masuk", "undereye right");
                        position = "under_eye_right";
                        draw();
                    } else if (xper > x279 && xper < x454 && yper > y341 && yper < y331) {
                        Log.e("masuk", "undereye left");
                        position = "under_eye_left";
                        draw();
                    } else if (xper > x132 && xper < x98 && yper > y93 && yper < y57) {
                        Log.e("masuk", "upper cheek right");
                        position = "upper_cheek_right";
                        draw();
                    } else if (xper > x327 && xper < x361 && yper > y323 && yper < y287) {
                        Log.e("masuk", "upper cheek left");
                        position = "upper_cheek_left";
                        draw();
                    } else if (xper > x58 && xper < x176 && yper > y58 && yper < y176) {
                        Log.e("masuk", "lower cheek right");
                        position = "lower_cheek_right";
                        draw();
                    } else if (xper > x400 && xper < x288 && yper > y288 && yper < y400) {
                        Log.e("masuk", "lower cheek left");
                        position = "lower_cheek_left";
                        draw();
                    } else if (xper > x211 && xper < x431 && yper > y57 && yper < y152) {
                        Log.e("masuk", "chin");
                        position = "chin";
                        draw();
                    } else if (xper > x102 && xper < x331 && yper > y8 && yper < y2) {
                        Log.e("masuk", "nose");
                        position = "nose";
                        draw();
                    } else {
                        Toast.makeText(mContext, "Please pick inside grid", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
//            case MotionEvent.ACTION_UP:
//                x = (int) event.getX();
//                y = (int) event.getY();
//                Log.e("masuk", "chin"+" "+x+" "+y);
//                invalidate();
//                break;
        }
        }
        return true;
    }

    private void draw() {
        if (!dots.contains(position)) {
            invalidate();
            dots.add(position);
        }
    }

    public List<List<Integer>> drawData() {
        return draw;
    }

    public String getPosition() {
        return position;
    }

    public List<String> getDots() {
        return dots;
    }

    public void undo() {
        if (draw.size() > 0 && dots.size() > 0) {
            int index = draw.size() - 1;
            draw.remove(index);
            dots.remove(dots.size() - 1);
            Log.e("dota", draw + " " + dots + " " + draw.size());
            invalidate();
        } else {
            Toast.makeText(mContext, "There is no dots left", Toast.LENGTH_SHORT).show();
            Activity activity = (Activity) mContext;
            activity.onBackPressed();
        }
    }

    public List<Integer> getData() {
        ArrayList<Integer> point = new ArrayList<Integer>();
        point.add(x);
        point.add(y);
        return point;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        state += 1;
        if (draw.size() > 0) {
            draw.forEach((n) -> {
                paint.setColor(Color.RED);
                canvas.drawCircle(n.get(1), n.get(2), radius, paint);
                paint.setColor(Color.WHITE);
                paint.setTextSize(40);
//                canvas.drawText(String.valueOf(n.get(0)), n.get(1), n.get(2), paint);
            });
        }

//        Log.e("mase", state + " " + draw + " " + firstIn + " " + dots+"~"+POSITION);
        if (firstIn) {
            if (state == 1) {
//                Log.e("mase", "masuk");
                state -= 1;
                firstIn = false;
            }
        }
        int remove = sharedPref.getInt("remove", 0);
        if (!condition) {
//            Log.e("maseuk bawah",state + " " + draw + " " + firstIn + " " + dots);
            firstIn = false;
            if (state > 0) {
                if (dots.size() != 0) {
                    condition = true;
                    if (remove == 0) {
                        paint.setColor(Color.RED);
                        canvas.drawCircle(x, y, radius, paint);

                        paint.setColor(Color.WHITE);
                        paint.setTextSize(40);
//                    canvas.drawText(String.valueOf(state), x, y, paint);
                        ArrayList<Integer> point = new ArrayList<Integer>();
                        point.add(state);
                        point.add(x);
                        point.add(y);
                        draw.add(new ArrayList<Integer>(point));
                        canvas.save();
                        if (listener != null) listener.onClick(this);
//        new SheetChoice().show(fragmentManager,"");
//    x=0;
//    y=0;
                    } else {
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.remove("remove");
                        editor.apply();
                        removeLast();
                    }
                }
            }
//            else{
//                state = 1;
//            }
        } else {
            condition = false;
        }
    }
}
