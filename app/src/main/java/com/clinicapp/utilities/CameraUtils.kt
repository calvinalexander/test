package com.clinicapp.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.MotionEvent
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clinicapp.R
import com.clinicapp.models.CameraAutoCaptureState
import com.clinicapp.models.CameraPositions
import com.google.firebase.crashlytics.internal.common.SessionReportingCoordinator.convertInputStreamToString
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors.newFixedThreadPool


class CameraUtils {

    companion object {
        var responseObj: JSONObject? = null
        var imageCapture: ImageCapture? = null
        var camera: Camera? = null
        var preview: Preview? = null
        var preview_autocapture: Preview? = null
        var imageAnalysis: ImageAnalysis? = null
        var getCameraProvider: ProcessCameraProvider? = null
        var imageJson: JSONArray? = null
        var textGuide: String? = null
        var imageWidth: Int? = null
        var imageHeight: Int? = null
        var photoPath: String? = null
        var url: URL? = null

        @SuppressLint("UnsafeOptInUsageError")
        public fun startCamera(
            context: Context,
            lyfCycle: LifecycleOwner,
            previewView: PreviewView,
            zoom: Float,
            position: CameraPositions
        ): LiveData<CameraAutoCaptureState> {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val liveDataAutoCaptureState = MutableLiveData<CameraAutoCaptureState>()
            Log.d("camera_position", position.position.toString())
            if (position.position == 1) {
                url = URL("http://18.116.23.243:7000/frontal_straight_android")
            } else if (position.position == 3) {
                url = URL("http://18.116.23.243:7000/down_90_degrees_android")
            } else {
                liveDataAutoCaptureState.postValue(CameraAutoCaptureState("", null))
            }
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                preview_autocapture = Preview.Builder()
                    .setTargetResolution(Size(384, 384))
                    .build()
                    .also { mPreview ->
                        mPreview.setSurfaceProvider(previewView.surfaceProvider)
                    }

                preview = Preview.Builder()
                    .build()
                    .also { mPreview ->
                        mPreview.setSurfaceProvider(previewView.surfaceProvider)
                    }
                imageCapture = ImageCapture.Builder()
                    .build()

                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(224,224))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()


                imageAnalysis!!.setAnalyzer(
                    newFixedThreadPool(1),
                    ImageAnalysis.Analyzer { imageProxy ->
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        Log.d("rotation_degree", rotationDegrees.toString());
                        // insert your code here.
                        val bitmap = imageProxy.image?.toBitmap()
                        imageJson = JSONArray()
                        for (i in 0 until imageWidth!!) {
                            val arr = JSONArray()
                            for (j in 0 until imageHeight!!) {
                                val color: Int = bitmap!!.getPixel(i, imageHeight!! - j - 1)
                                val rgb = JSONArray()
                                rgb.put(0, Color.red(color))
                                rgb.put(1, Color.green(color))
                                rgb.put(2, Color.blue(color))
                                arr.put(j, rgb)
                            }
                            imageJson!!.put(i, arr)
                        }


                        val postData = JSONObject()
                        try {
                            postData.put("hair_img", imageJson)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }


                        responseObj = post(postData, url!!)
                        Log.d("width", bitmap!!.width.toString())
                        Log.d("height", bitmap!!.height.toString())
                        if (responseObj?.has("guidelines") == true) {
                            textGuide = responseObj?.get("guidelines").toString()
                            if (textGuide.equals("OK")) {
                                try {
                                    val photoFile = File(
                                        getOutputDirectory(context),
                                        "${R.string.imageFolder}-${System.currentTimeMillis()}.jpg"
                                    )
                                    Log.d("photo_file", photoFile.toString())
                                    val fileStream = photoFile.outputStream()
                                    val matrix = Matrix()
                                    matrix.postRotate(90F)
                                    //if you want bigger resolution for preview, please resize the bitmap here
                                    //default is 384x384
                                    val rotatedBitmap = Bitmap.createBitmap(
                                        bitmap!!,
                                        0,
                                        0,
                                        bitmap.width,
                                        bitmap.height,
                                        matrix,
                                        true
                                    )
                                    rotatedBitmap!!.compress(
                                        Bitmap.CompressFormat.JPEG,
                                        100,
                                        fileStream
                                    )
                                    fileStream.close()
                                    photoPath = Uri.fromFile(photoFile).toString()
                                } catch (e: java.lang.RuntimeException) {
                                    e.printStackTrace()
                                }
                            }
                        } else {
                            textGuide = responseObj?.get("note").toString()
                        }
                        val autoCaptureState = CameraAutoCaptureState(textGuide, photoPath)
                        // after done, release the ImageProxy object
                        liveDataAutoCaptureState.postValue(autoCaptureState)
                        imageProxy.close()
                    })


                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
//                    camera=cameraProvider.bindToLifecycle(lyfCycle, cameraSelector,imageAnalysis,preview_autocapture,imageCapture)
                    if ((position.position == 1) or (position.position == 3)) {
                        val viewPort =
                            ViewPort.Builder(Rational(Utils.dpToPx(context,100), Utils.dpToPx(context,200)), 0)
                                .build()
//width = 350, height = 100, rotation = Surface.ROTATION_0
                        val useCaseGroup = UseCaseGroup.Builder()
                            .addUseCase(preview_autocapture!!) //your preview
                            .addUseCase(imageAnalysis!!) //if you are using imageAnalysis
                            .addUseCase(imageCapture!!)
                            .setViewPort(previewView.viewPort!!)
                            .build()
//
//                        val useCaseGroup = UseCaseGroup.Builder()
//                            .addUseCase(preview_autocapture!!) //your preview
//                            .addUseCase(preview!!) //your preview
//                            .addUseCase(imageAnalysis!!) //if you are using imageAnalysis
//                            .addUseCase(imageCapture!!)
//                            .setViewPort(previewView.viewPort!!)
//                            .build()
                        camera =
                            cameraProvider.bindToLifecycle(lyfCycle, cameraSelector, useCaseGroup)

                      //  camera=cameraProvider.bindToLifecycle(lyfCycle, cameraSelector,imageAnalysis,preview_autocapture,imageCapture)
                    } else {
                        camera = cameraProvider.bindToLifecycle(
                            lyfCycle,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    }
                    camera!!.cameraControl.setZoomRatio(zoom)

                    previewView.setOnTouchListener { _, event ->
                        return@setOnTouchListener when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                true
                            }
                            MotionEvent.ACTION_UP -> {
                                val factory: MeteringPointFactory =
                                    SurfaceOrientedMeteringPointFactory(
                                        previewView.width.toFloat(), previewView.height.toFloat()
                                    )
                                val autoFocusPoint = factory.createPoint(event.x, event.y)
                                try {
                                    camera!!.cameraControl.startFocusAndMetering(
                                        FocusMeteringAction.Builder(
                                            autoFocusPoint,
                                            FocusMeteringAction.FLAG_AF
                                        ).apply {
                                            //focus only when the user tap the preview
                                            disableAutoCancel()
                                        }.build()
                                    )
                                } catch (e: CameraInfoUnavailableException) {
                                    Log.d("ERROR", "cannot access camera", e)
                                }
                                true
                            }
                            else -> false // Unhandled event.
                        }
                    }

                    getCameraProvider=cameraProvider


                } catch (e: Exception) {
                    Log.d("CameraAPI", "StartCamera Fail: ", e)
                }


            }, ContextCompat.getMainExecutor(context))
            return liveDataAutoCaptureState
        }

        fun post(postData: JSONObject, url: URL): JSONObject? {
            var urlConnection: HttpURLConnection? = null
            try {
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection!!.doInput = true
                urlConnection!!.doOutput = true
                urlConnection!!.setRequestProperty("Content-Type", "application/json")
                urlConnection!!.requestMethod = "POST"
                if (postData != null) {
                    val writer = OutputStreamWriter(urlConnection!!.outputStream)
                    writer.write(postData.toString())
                    writer.flush()
                }
                val statusCode = urlConnection!!.responseCode
                if (statusCode == 200) {
                    val inputStream: InputStream =
                        BufferedInputStream(urlConnection!!.inputStream)

                    val response: String = convertInputStreamToString(inputStream)
                    responseObj = JSONObject(response)
                    Log.d("response", response)
                    // From here you can convert the string to JSON with whatever JSON parser you like to use
                    // After converting the string to JSON, I call my custom callback. You can follow this process too, or you can implement the onPostExecute(Result) method
                    Log.d("responsebytes", response.toByteArray().toString())
                } else {
                    // Status code is not 200
                    // Do something to handle the error
                    val errorStream: InputStream =
                        BufferedInputStream(urlConnection!!.errorStream)
                    val error: String = convertInputStreamToString(errorStream)
                    Log.d("response_error", error)
                    responseObj = JSONObject(error)
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
            return responseObj
        }

        fun Image.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer // Y
            val vuBuffer = planes[2].buffer // VU

            val ySize = yBuffer.remaining()
            val vuSize = vuBuffer.remaining()

            val nv21 = ByteArray(ySize + vuSize)

            yBuffer.get(nv21, 0, ySize)
            vuBuffer.get(nv21, ySize, vuSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            imageWidth = yuvImage.width
            imageHeight = yuvImage.height
            Log.d("yuvImage", yuvImage.width.toString())
            Log.d("yuvImage", yuvImage.height.toString())
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }


        fun getOutputDirectory(context: Context): File {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // only for gingerbread and newer versions
                val mediaDir = context.externalCacheDirs.firstOrNull()?.let { mFile ->
                    File(mFile, context.resources.getString(R.string.imageFolder)).apply {
                        mkdirs()
                    }
                }
                return if (mediaDir != null && mediaDir.exists())
                    mediaDir else context.filesDir
            } else {
                val mediaDir = Environment.getExternalStorageDirectory()?.let { mFile ->
                    File(mFile, context.resources.getString(R.string.imageFolder)).apply {
                        mkdirs()
                    }
                }
                return if (mediaDir != null && mediaDir.exists())
                    mediaDir else context.filesDir
            }

        }

        fun takePhoto(context: Context): LiveData<String> {
            Log.d("takingphoto", "takingphoto")
            val liveData = MutableLiveData<String>()
            val imageCapture = imageCapture ?: return liveData
            val photoFile = File(
                getOutputDirectory(context),
                "${R.string.imageFolder}-${System.currentTimeMillis()}.jpg"
            )


            val outputOption = ImageCapture.OutputFileOptions
                .Builder(photoFile)
                .build()

            val takePic =
                imageCapture.takePicture(outputOption, ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                            val savedUri = Uri.fromFile(photoFile)
                            liveData.postValue(savedUri.toString())
                        }

                        override fun onError(exception: ImageCaptureException) {
                            liveData.postValue(exception.toString())
                            Log.e("CameraAPI", "", exception)
                        }
                    }
                )

            return liveData

        }
    }

    fun saveBitmapToFile(file: File): File? {
        return try {

            // BitmapFactory options to downsize the image
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image
            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            // The new size we want to scale to
            val REQUIRED_SIZE = 75

            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE
            ) {
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()

            // here i override the original image file
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            file
        } catch (e: java.lang.Exception) {
            null
        }
    }
}