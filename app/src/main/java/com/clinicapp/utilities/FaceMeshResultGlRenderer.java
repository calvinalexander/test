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

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.opengl.GLES20;
import android.util.Log;

import com.google.common.collect.ImmutableSet;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.solutioncore.ResultGlRenderer;
import com.google.mediapipe.solutions.facemesh.FaceMeshConnections;
import com.google.mediapipe.solutions.facemesh.FaceMeshResult;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

/** A custom implementation of {@link ResultGlRenderer} to render {@link FaceMeshResult}. */
public class FaceMeshResultGlRenderer implements ResultGlRenderer<FaceMeshResult> {
  private static final String TAG = "FaceMeshResultGlRenderer";

  private static final int[] FACE_MESH_INDEX = new int[] {151,337,299,333,298,301,368,264,447,366,401};
  //  private static final int[] FACE_SQUARE_FOREHEAD = new int[][] {{109,338,337},108,109};
  private static final int[] FACE_SQUARE_RIGHT = new int[] {329,347,280,266,329};
  private static final int[] FACE_SQUARE_LEFT = new int[] {100,118,50,36,100};
  private static final int[] LEFT_EYE_FACE_MESH_INDEX = new int[] {417,441,442,443,444,445,342,446,255,339,254,253,252,256,341,463,417};
  private static final int[] RIGHT_EYE_FACE_MESH_INDEX = new int[] {193,221,222,223,224,225,113,226,25,110,24,23,22,26,112,243,193};
  private static final int[][] EYES_FACE_MESH_INDEX = new int[][] {LEFT_EYE_FACE_MESH_INDEX, RIGHT_EYE_FACE_MESH_INDEX};
  private static final int[] NOSE_FACE_MESH_INDEX = new int[] {8,417,399,420,360,278,294,327,326,2,97,98,64,48,131,198,174,193,8};
  private static final int[] MOUTH_FACE_MESH_INDEX = new int[] {57,185,40,39,37,0,267,269,270,409,287,375,321,405,314,17,84,181,91,146,57};
  private static final int[][] MOUTH_NOSE_FACE_MESH_INDEX = new int[][] {NOSE_FACE_MESH_INDEX, MOUTH_FACE_MESH_INDEX};
  private static final int[][] TO_MOUTH_MESH_INDEX = new int[][] {new int[]{136,135,57,92,165,98},new int[]{176,32,194,181},new int[]{400,262,418,405}, new int[]{365,364,287,322,391,327}, new int[]{39,167,97}, new int[]{269,393,328}};
  private static final int[][] VERTICAL_MESH_INDEX = new int[][] {new int[]{23,230,119,101,205,207,214,210,211,32,208,199,428,262,431,430,434,427,425,330,348,450,253},new int[]{224,105,67},new int[]{221,108,109}, new int[]{441,337,338}, new int[]{39,167,97}, new int[]{444,334,297}};
  private static final int[][] HORIZONTAL_MESH_INDEX = new int[][] {new int[]{54,104,69,108,151,337,299,333,284}, new int[]{162,71,63,105,66,9,296,334,293,301,389},new int[]{127,34,143,35,226}, new int[]{446,265,372,264,356}, new int[]{234,101,198}, new int[]{420,330,454}, new int[]{132,207,98}, new int[]{361,427,327}};
  private static final float[] FACE_MESH_COLOR = new float[] {0f, 0f, 1f, 1f};
  public static final int NOSE_LANDMARK_INDEX = 4;
  private static final int CHIN_LANDMARK_INDEX = 152;
  public static final int EYES_BRIDGE_LANDMARK_INDEX = 8;
  public static final int LEFT_EYE_LANDMARK_INDEX = 359;
  public static final int RIGHT_EYE_LANDMARK_INDEX = 130;
  public static final int TOP_FACE_LANDMARK_INDEX = 10;
  public static final int BOTTOM_FACE_LANDMARK_INDEX = 152;
  public static final int RIGHT_FACE_LANDMARK_INDEX = 234;
  public static final int LEFT_FACE_LANDMARK_INDEX = 454;
  private static int POSITION = 0;
  private static final float[] POINT_COLOR = new float[] {1f, 1f, 0f, 1f};
  private static final int POINT_THICKNESS = 10;
  private static final float[] CONTOUR_COLOR = new float[] {1f, 0.2f, 0.2f, 1f};
  private static final int CONTOUR_THICKNESS = 4;
  private static final float[] TESSELATION_COLOR = new float[] {0.75f, 0.75f, 0.75f, 0.5f};
  private static final int TESSELATION_THICKNESS = 5;
  private static final float[] RIGHT_EYE_COLOR = new float[] {1f, 0.2f, 0.2f, 1f};
  private static final int SQUARE_THICKNESS = 10;
  private static final float[] RIGHT_EYEBROW_COLOR = new float[] {1f, 0.2f, 0.2f, 1f};
  private static final int RIGHT_EYEBROW_THICKNESS = 8;
  private static final float[] LEFT_EYE_COLOR = new float[] {0.2f, 1f, 0.2f, 1f};
  private static final int LEFT_EYE_THICKNESS = 8;
  private static final float[] LEFT_EYEBROW_COLOR = new float[] {0.2f, 1f, 0.2f, 1f};
  private static final int LEFT_EYEBROW_THICKNESS = 8;
  private static final float[] FACE_OVAL_COLOR = new float[] {0.9f, 0.9f, 0.9f, 1f};
  private static final int FACE_OVAL_THICKNESS = 8;
  private static final float[] LIPS_COLOR = new float[] {0.9f, 0.9f, 0.9f, 1f};
  private static final int LIPS_THICKNESS = 8;
  private static final String VERTEX_SHADER =
          "uniform mat4 uProjectionMatrix;\n"
                  + "attribute vec4 vPosition;\n"
                  + "void main() {\n"
                  + "  gl_Position = uProjectionMatrix * vPosition;\n"
                  + "}";
  private static final String FRAGMENT_SHADER =
          "precision mediump float;\n"
                  + "uniform vec4 uColor;\n"
                  + "void main() {\n"
                  + "  gl_FragColor = uColor;\n"
                  + "}";
  private int program;
  private int positionHandle;
  private int projectionMatrixHandle;
  private int colorHandle;
  private float[] pos = new float[2];

  private int loadShader(int type, String shaderCode) {
    int shader = GLES20.glCreateShader(type);
    GLES20.glShaderSource(shader, shaderCode);
    GLES20.glCompileShader(shader);
    return shader;
  }

  @Override
  public void setupRendering() {
    program = GLES20.glCreateProgram();
    int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
    GLES20.glAttachShader(program, vertexShader);
    GLES20.glAttachShader(program, fragmentShader);
    GLES20.glLinkProgram(program);
    positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
    projectionMatrixHandle = GLES20.glGetUniformLocation(program, "uProjectionMatrix");
    colorHandle = GLES20.glGetUniformLocation(program, "uColor");
  }

//  public static final ImmutableSet<FaceMeshConnections.Connection> TEST = ImmutableSet.of(FaceMeshConnections.Connection);

  public void position(int i){
    POSITION = i;
  }
  @Override
  public void renderResult(FaceMeshResult result, float[] projectionMatrix) {
    if (result == null) {
      return;
    }
    GLES20.glUseProgram(program);
    GLES20.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);

    int numFaces = result.multiFaceLandmarks().size();

    for (int i = 0; i < numFaces; ++i) {
//      drawDotsOnLandmarks(
//              result.multiFaceLandmarks().get(i).getLandmarkList(),
//              NOSE_LANDMARK_INDEX,
//              POINT_COLOR);
//      drawDotsOnLandmarks(
//              result.multiFaceLandmarks().get(i).getLandmarkList(),
//              CHIN_LANDMARK_INDEX,
//              POINT_COLOR);
//      drawDotsOnLandmarks(
//              result.multiFaceLandmarks().get(i).getLandmarkList(),
//              EYES_BRIDGE_LANDMARK_INDEX,
//              POINT_COLOR);
//      drawDotsOnLandmarks(
//              result.multiFaceLandmarks().get(i).getLandmarkList(),
//              LEFT_EYE_LANDMARK_INDEX,
//              POINT_COLOR);
//      drawDotsOnLandmarks(
//              result.multiFaceLandmarks().get(i).getLandmarkList(),
//              RIGHT_EYE_LANDMARK_INDEX,
//              POINT_COLOR);
      drawDotsOnLandmarks(
              result.multiFaceLandmarks().get(i).getLandmarkList(),
              TOP_FACE_LANDMARK_INDEX,
              POINT_COLOR);
      drawDotsOnLandmarks(
              result.multiFaceLandmarks().get(i).getLandmarkList(),
              BOTTOM_FACE_LANDMARK_INDEX,
              POINT_COLOR);
      drawDotsOnLandmarks(
              result.multiFaceLandmarks().get(i).getLandmarkList(),
              RIGHT_FACE_LANDMARK_INDEX,
              POINT_COLOR);
      drawDotsOnLandmarks(
              result.multiFaceLandmarks().get(i).getLandmarkList(),
              LEFT_FACE_LANDMARK_INDEX,
              POINT_COLOR);

      switch (POSITION) {
        case 1: {
//          drawLandmarks(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConnections.FACEMESH_FACE_OVAL,
//                  FACE_MESH_COLOR,
//                  CONTOUR_THICKNESS);
//          drawConnectionFromIndex(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  EYES_FACE_MESH_INDEX,
//                  FACE_MESH_COLOR,
//                  CONTOUR_THICKNESS);
//          drawConnectionFromIndex(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  MOUTH_NOSE_FACE_MESH_INDEX,
//                  FACE_MESH_COLOR,
//                  CONTOUR_THICKNESS);
//          drawConnectionFromIndex(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  TO_MOUTH_MESH_INDEX,
//                  FACE_MESH_COLOR,
//                  CONTOUR_THICKNESS);
//          drawConnectionFromIndex(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  VERTICAL_MESH_INDEX,
//                  FACE_MESH_COLOR,
//                  CONTOUR_THICKNESS);
//          drawConnectionFromIndex(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  HORIZONTAL_MESH_INDEX,
//                  FACE_MESH_COLOR,
//                  CONTOUR_THICKNESS);
          break;
        }
        case 2: {
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_RIGHT,
//                  FACE_MESH_COLOR,
//                  SQUARE_THICKNESS);
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_LEFT,
//                  FACE_MESH_COLOR,
//                  SQUARE_THICKNESS);
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_FOREHEAD,
//                  POINT_COLOR,
//                  SQUARE_THICKNESS);
//          drawLandmarksForehead(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_FOREHEAD_2,
//                  POINT_COLOR,
//                  SQUARE_THICKNESS);
          break;
        }
        case 3: {
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_RIGHT,
//                  POINT_COLOR,
//                  SQUARE_THICKNESS);
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_LEFT,
//                  FACE_MESH_COLOR,
//                  SQUARE_THICKNESS);
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_FOREHEAD,
//                  FACE_MESH_COLOR,
//                  SQUARE_THICKNESS);
          drawLandmarksForehead(
                  result.multiFaceLandmarks().get(i).getLandmarkList(),
                  FaceMeshConn2.FACE_SQUARE_CHIN,
                  POINT_COLOR,
                  SQUARE_THICKNESS);
          break;
        }
        case 4: {
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_RIGHT,
//                  FACE_MESH_COLOR,
//                  SQUARE_THICKNESS);
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_LEFT,
//                  POINT_COLOR,
//                  SQUARE_THICKNESS);
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_FOREHEAD,
//                  FACE_MESH_COLOR,
//                  SQUARE_THICKNESS);
          break;
        }
        case 5: {
//          drawLandmarksSquare(
//                  result.multiFaceLandmarks().get(i).getLandmarkList(),
//                  FaceMeshConn2.FACE_SQUARE_LEFT,
//                  POINT_COLOR,
//                  SQUARE_THICKNESS);
          break;
        }
        case 6: {
          break;
        }
        case 7: {
          drawLandmarksSquare(
                  result.multiFaceLandmarks().get(i).getLandmarkList(),
                  FaceMeshConn2.FACE_SQUARE_RIGHT,
                  POINT_COLOR,
                  SQUARE_THICKNESS);
          break;
        }
      }
    }
  }

  /**
   * Deletes the shader program.
   *
   * <p>This is only necessary if one wants to release the program while keeping the context around.
   */
  public void release() {
    GLES20.glDeleteProgram(program);
  }

  private void drawLandmarks(
          List<NormalizedLandmark> faceLandmarkList,
          ImmutableSet<FaceMeshConnections.Connection> connections,
          float[] colorArray,
          int thickness) {
    GLES20.glUniform4fv(colorHandle, 1, colorArray, 0);
    GLES20.glLineWidth(thickness);
    for (FaceMeshConnections.Connection c : connections) {
      NormalizedLandmark start = faceLandmarkList.get(c.start());
      NormalizedLandmark end = faceLandmarkList.get(c.end());
      float[] vertex = {start.getX(), start.getY(), end.getX(), end.getY()};
      FloatBuffer vertexBuffer =
              ByteBuffer.allocateDirect(vertex.length * 4)
                      .order(ByteOrder.nativeOrder())
                      .asFloatBuffer()
                      .put(vertex);
      vertexBuffer.position(0);
      GLES20.glEnableVertexAttribArray(positionHandle);
      GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
      GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
    }
  }

  private void drawDotsOnLandmarks(
          List<NormalizedLandmark> faceLandmakrList,
          int landmarkIndex,
          float[] colorArray
  ) {
    int vertexCount = 10;
    float radius = 0.015f;
    int idx=0;

    GLES20.glUniform4fv(colorHandle, 1, colorArray, 0);

    float[] buffer = new float[vertexCount*2];

    NormalizedLandmark thePoint = faceLandmakrList.get(landmarkIndex);
    float center_x = thePoint.getX();
    float center_y = thePoint.getY();

    if (landmarkIndex == NOSE_LANDMARK_INDEX) {
      pos[0] = thePoint.getX();
      pos[1] = thePoint.getY();
      Log.d("PointX of Nose", String.valueOf(pos[0]));

    }

    buffer[idx++] = center_x;
    buffer[idx++] = center_y;

    int outerVertexCount = vertexCount-1;


    for (int i = 0; i < outerVertexCount; i++) {
      float percent = (i / (float) (outerVertexCount-1));
      double rad = percent * 2*Math.PI;

      double outer_x = center_x + radius * cos(rad);
      double outer_y = center_y + radius * sin(rad);

      buffer[idx++] = (float) outer_x;
      buffer[idx++] = (float) outer_y;
    }
    FloatBuffer vertexBuffer =
            ByteBuffer.allocateDirect(buffer.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(buffer);
    vertexBuffer.position(0);
    GLES20.glEnableVertexAttribArray(positionHandle);
    GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
  }

  private void drawConnectionFromIndex(
          List<NormalizedLandmark> faceLandmakrList,
          int[][] landmarkIndex,
          float[] colorArray,
          int thickness
  ) {
    GLES20.glUniform4fv(colorHandle, 1, colorArray, 0);
    GLES20.glLineWidth(thickness);
    NormalizedLandmark start;
    NormalizedLandmark end;

    for (int l=0; l < landmarkIndex.length; l++) {
      for (int i=0; i < landmarkIndex[l].length; i++) {
        start = faceLandmakrList.get(landmarkIndex[l][i]);
        if (i <= landmarkIndex[l].length-2) {
          end = faceLandmakrList.get(landmarkIndex[l][i+1]);
          float[] vertex = {start.getX(), start.getY(), end.getX(), end.getY()};
          FloatBuffer vertexBuffer =
                  ByteBuffer.allocateDirect(vertex.length * 4)
                          .order(ByteOrder.nativeOrder())
                          .asFloatBuffer()
                          .put(vertex);
          vertexBuffer.position(0);
          GLES20.glEnableVertexAttribArray(positionHandle);
          GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
          GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
        }
      }

    }

  }

  float[] saveFirst;
  float[] saveSecond;
  float[] saveThird;
  float[] saveFourth;
  private void drawLandmarksSquare(
          List<NormalizedLandmark> faceLandmarkList,
          ImmutableSet<FaceMeshConn2.Connection> connections,
          float[] colorArray,
          int thickness) {
    GLES20.glUniform4fv(colorHandle, 1, colorArray, 0);
    GLES20.glLineWidth(thickness);
    int count = 0;
    for (FaceMeshConn2.Connection c : connections) {
      count+=1;
      NormalizedLandmark start = faceLandmarkList.get(c.start());
      NormalizedLandmark end = faceLandmarkList.get(c.end());

      float[] vertex = {start.getX(), start.getY(), end.getX(), end.getY()};
      switch (count){
        case 1:{
          saveFirst = new float[]{start.getX(),start.getY()};
          saveSecond = new float[]{end.getX(),end.getY()};
          //x1 y1 , x1 y1
          vertex = new float[]{start.getX(), start.getY(), end.getX(), start.getY()};
          break;
        }
        case 2:{
          //x2 y1, x2 y3
          saveThird= new float[]{end.getX(),end.getY()};
          vertex = new float[]{start.getX(), saveFirst[1], start.getX(), end.getY()};
          break;
        }
        case 3:{
          //x2 y3, x1 y3
          vertex = new float[]{saveSecond[0], start.getY(), saveFirst[0], saveThird[1]};
          break;
        }
        case 4:{
          //x1 y3, x1 y1
          vertex = new float[]{saveFirst[0], saveThird[1], saveFirst[0], saveFirst[1]};
          break;
        }
      }
      FloatBuffer vertexBuffer =
              ByteBuffer.allocateDirect(vertex.length * 4)
                      .order(ByteOrder.nativeOrder())
                      .asFloatBuffer()
                      .put(vertex);

      vertexBuffer.position(0);
      GLES20.glEnableVertexAttribArray(positionHandle);
      GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
      GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
    }
  }

  private void drawLandmarksForehead(
          List<NormalizedLandmark> faceLandmarkList,
          ImmutableSet<FaceMeshConn2.Connection> connections,
          float[] colorArray,
          int thickness) {
    GLES20.glUniform4fv(colorHandle, 1, colorArray, 0);

//    float lineWidthRange[] = {0.0f, 0.0f};
//    ByteBuffer vbb = ByteBuffer.allocateDirect(lineWidthRange.length * 4);
//    vbb.order(ByteOrder.nativeOrder());
//    GLES20.glGetFloatv(GLES20.GL_ALIASED_LINE_WIDTH_RANGE, vbb.asFloatBuffer());
//    GLES20.glPolygonOffset(100.0f, 100.0f);
    GLES20.glLineWidth(thickness);
    int count = 0;
    for (FaceMeshConn2.Connection c : connections) {
      count+=1;
      NormalizedLandmark start = faceLandmarkList.get(c.start());
      NormalizedLandmark end = faceLandmarkList.get(c.end());
      if(count==1){
        saveFirst = new float[]{start.getX(),start.getY()};
        saveSecond = new float[]{end.getX(),end.getY()};
      }else if (count == 2){
        saveThird= new float[]{end.getX(),end.getY()};
      }else if (count == 3){
        saveFourth= new float[]{end.getX(),end.getY()};
      }
    }
    count = 0;
    for (FaceMeshConn2.Connection c : connections) {
      count+=1;
      NormalizedLandmark start = faceLandmarkList.get(c.start());
      NormalizedLandmark end = faceLandmarkList.get(c.end());

      float[] vertex= {start.getX(), start.getY(), end.getX(), end.getY()};
      switch (count){
        case 1:{
          vertex = new float[]{saveFirst[0], saveSecond[1], saveThird[0], saveSecond[1]};
          break;
        }
        case 2:{
          vertex = new float[]{saveThird[0], saveSecond[1], saveThird[0], saveFourth[1]};
          break;
        }
        case 3:{
          vertex = new float[]{saveThird[0], saveFourth[1], saveFirst[0], saveFourth[1]};
          break;
        }
        case 4:{
          vertex = new float[]{saveFirst[0], saveFourth[1], saveFirst[0], saveSecond[1]};
          break;
        }
      }
      FloatBuffer vertexBuffer =
              ByteBuffer.allocateDirect(vertex.length * 4)
                      .order(ByteOrder.nativeOrder())
                      .asFloatBuffer()
                      .put(vertex);

      vertexBuffer.position(0);
      GLES20.glEnableVertexAttribArray(positionHandle);
      GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
//      GLES20.glGetFloatv(GLES20.GL_ALIASED_LINE_WIDTH_RANGE,vertexBuffer);
      GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
    }
  }

}
