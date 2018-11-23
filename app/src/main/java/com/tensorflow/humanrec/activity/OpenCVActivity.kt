@file:Suppress("DEPRECATION")

package com.tensorflow.humanrec.activity

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.tensorflow.humanrec.R
import com.tensorflow.humanrec.model.Age
import com.tensorflow.humanrec.model.Gender
import com.tensorflow.humanrec.processing.agegender.AgeGenderDetector
import com.tensorflow.humanrec.processing.agegender.IAgeGenderDetector
import com.tensorflow.humanrec.processing.face.FaceDetector
import com.tensorflow.humanrec.processing.face.IFaceDetector
import com.tensorflow.humanrec.processing.processing.FaceProcessing
import com.tensorflow.humanrec.processing.processing.IFaceProcessing
import com.tensorflow.humanrec.utility.opencvfix.CameraBridgeViewBase
import com.tensorflow.humanrec.utility.permissions.PermissionsCallback
import com.tensorflow.humanrec.utility.permissions.PermissionsHelper
import org.opencv.android.OpenCVLoader
import org.opencv.core.*

class OpenCVActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    // matrix to store current camera output
    private var rgbaMat: Mat? = null

    // variables to store results of face processing

    private var distance = 0.0
    private var age: Age? = null
    private var gender = Gender.MALE
    private var facesDetected: Boolean = false

    // buffer for storing reduced rgba camera frame
    private var imageBuffer = ByteArray(128 * 128 * 4)

    // buffer for storing face rect
    private var facesBuffer = FloatArray(3 * 227 * 227)

    // processing classes
    private lateinit var ageGenderDetector: IAgeGenderDetector
    private lateinit var faceDetector: IFaceDetector
    private lateinit var faceProcessing: IFaceProcessing

    // openCv camera class
    // by default opencv camera does not support portrait orientation
    // so we override default camera bridge to add portrait orientation
    private var cameraView: CameraBridgeViewBase? = null

    // textViews to show result on the screen
    private lateinit var stateView: TextView
    private lateinit var stateViewForDistance: TextView
    private lateinit var stateViewForFrameRate: TextView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_opencv)

        initProcessing()
        initScreen()
        initCameraPreview()
    }

    private fun initProcessing() {
        OpenCVLoader.initDebug()
        ageGenderDetector = AgeGenderDetector()
        ageGenderDetector.initialize(this)

        faceDetector = FaceDetector()
        faceDetector.initialize(this, imageBuffer, facesBuffer)

        faceProcessing = FaceProcessing()
        faceProcessing.initialize()

        // here we specify all processing steps as arrayList

        faceProcessing.addProcessingSteps(arrayListOf(
                { detectFaces() },
                { facesDetected = faceDetector.processFaces() },
                { age = ageGenderDetector.fetchAge(facesBuffer) },
                { gender = ageGenderDetector.fetchGender(facesBuffer) },
                { runOnUiThread { distance = faceDetector.detectDistance() } },
                { logResults() },
                { faceDetector.releaseObjects() }
        ))
    }

    private fun detectFaces() {
        // getting matrix where we stored reduced camera frame
        val resizedMat = faceProcessing.rgbaResizedMat()

        if (resizedMat != null) {
            // run face detector
            faceDetector.detectFaces(resizedMat)
        }
    }

    private fun initCamera() {
        cameraView!!.setCameraIndex(1)
        cameraView!!.enableView()

        // as soon as camera started we run our processing thread
        faceProcessing.startProcessing()
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat? {
        // opencv method to get rgba data from camera frame
        // basically it just get yuv data from camera frame and applies cvtColor
        // Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);
        rgbaMat = inputFrame.rgba()

        // feed this to processing thread to start frame processing
        faceProcessing.feed(rgbaMat!!)

        // draw raw opencv rgba mat in the camera preview
        return rgbaMat
    }

    @SuppressLint("SetTextI18n")
    private fun logResults() {
        runOnUiThread {
            stateViewForFrameRate.text = "frameRate: " + faceProcessing.frameRate()
            if (facesDetected) {
                val state = age?.label.toString() + "\ngender: " + gender.label

                val distanceState =  "distance (m): ${when (distance) {
                    in 1..2 -> "Very close"
                    in 2..6 -> "Normal"
                    in 6..100 -> "Far"
                    else -> "Not detected"
                }
                }"

                stateView.text = state
                stateViewForDistance.text = distanceState

                when (distance) {
                    in 0..2 ->
                        stateViewForDistance.setTextColor(getColor(R.color.colorRed))
                    else -> stateViewForDistance.setTextColor(getColor(R.color.colorGreen))
                }
            } else {
                runOnUiThread {
                    stateView.text = ""
                    stateViewForDistance.text = ""
                }
            }
        }
    }

    // region Not interesting

    public override fun onPause() {
        super.onPause()
        if (cameraView != null) {
            cameraView!!.disableView()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        cameraView!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        rgbaMat = Mat()
    }

    override fun onCameraViewStopped() {
        rgbaMat!!.release()
    }

    private fun initScreen() {
        initViews()
    }

    private fun initViews() {
        cameraView = findViewById(R.id.previewSurface)
        cameraView!!.visibility = CameraBridgeViewBase.VISIBLE
        cameraView!!.setCvCameraViewListener(this)

        stateView = findViewById(R.id.state_view)
        stateViewForDistance = findViewById(R.id.state_view_for_distance)
        stateViewForFrameRate = findViewById(R.id.state_view_for_frame_rate)
    }

    private fun initCameraPreview() {
        PermissionsHelper.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE),
                object: PermissionsCallback {
                    override fun granted() {
                        initCamera()
                    }

                    override fun disabled() {
                        Toast.makeText(this@OpenCVActivity,
                                getString(R.string.permissions_error), Toast.LENGTH_SHORT).show()
                    }
                })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PermissionsHelper.onPermissionsResult(requestCode, grantResults)
    }

    // endregion
}
