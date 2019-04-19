@file:Suppress("DEPRECATION")

package com.tensorflow.humanrec.activity

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import com.tensorflow.humanrec.R
import com.tensorflow.humanrec.model.Age
import com.tensorflow.humanrec.model.Gender
import com.tensorflow.humanrec.processing.agegender.AgeGenderDetector
import com.tensorflow.humanrec.processing.agegender.IAgeGenderDetector
import com.tensorflow.humanrec.processing.face.FaceDetector
import com.tensorflow.humanrec.processing.face.IFaceDetector
import com.tensorflow.humanrec.utility.camera.AutoFitTextureView
import com.tensorflow.humanrec.utility.camera.CameraHelper
import com.tensorflow.humanrec.utility.camera.CameraHelper.Companion.FRAME_HEIGHT
import com.tensorflow.humanrec.utility.camera.CameraHelper.Companion.FRAME_WIDTH
import com.tensorflow.humanrec.utility.camera.CameraReadyCallback
import com.tensorflow.humanrec.utility.permissions.PermissionsCallback
import com.tensorflow.humanrec.utility.permissions.PermissionsHelper
import org.opencv.android.OpenCVLoader
import ru.faceprocessing.renderscript.FaceProcessingRenderScript

class RenderScriptActivity : AppCompatActivity() {
    // variables to store results of face processing
    private var distanceToFace = 0.0
    private var age: Age? = null
    private var gender = Gender.MALE
    private var facesDetected: Boolean = false

    // buffer for storing reduced rgba camera frame
    private var imageBuffer = ByteArray(128 * 128 * 4)

    // buffer for storing face rect
    private var facesBuffer = FloatArray(3 * 227 * 227)

    // texture to draw camera frames
    private lateinit var outputTexture: AutoFitTextureView

    // textViews to show result on the screen
    private lateinit var stateView: TextView
    private lateinit var stateViewForDistance: TextView
    private lateinit var stateViewForFrameRate: TextView

    // basic camera settings and output configuration
    private var cameraHelper: CameraHelper? = null

    // processing classes
    private lateinit var ageGenderDetector: IAgeGenderDetector
    private lateinit var faceDetector: IFaceDetector
    private lateinit var faceProcessingRenderScript: FaceProcessingRenderScript

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initProcessing()
        initScreen()
    }

    private fun initProcessing() {
        OpenCVLoader.initDebug()
        ageGenderDetector = AgeGenderDetector()
        ageGenderDetector.initialize(this)

        faceDetector = FaceDetector()
        faceDetector.initialize(this, imageBuffer, facesBuffer)

        faceProcessingRenderScript = FaceProcessingRenderScript()
        faceProcessingRenderScript.initialize(this, FRAME_WIDTH, FRAME_HEIGHT, imageBuffer, true)

        // here we specify all processing steps as arrayList

        faceProcessingRenderScript.addProcessingSteps(arrayListOf(
                { faceDetector.detectFaces() },
                { facesDetected = faceDetector.processFaces() },
                { /*age = ageGenderDetector.fetchAge(facesBuffer)*/ },
                { gender = ageGenderDetector.fetchGender(facesBuffer) },
                { runOnUiThread { distanceToFace = faceDetector.detectDistance() } },
                { logResults() },
                { faceDetector.releaseObjects() }
        ))
    }

    private fun initCamera() {
        cameraHelper = CameraHelper()
        cameraHelper?.init(this, outputTexture, faceProcessingRenderScript, object : CameraReadyCallback {
            override fun ready() {
                // as soon as camera started we run our processing thread
                faceProcessingRenderScript.startProcessing()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun logResults() {
        runOnUiThread {
            stateViewForFrameRate.text = "frameRate: " + faceProcessingRenderScript.frameRate
            if (facesDetected) {
                val state = age?.label + "\ngender: " + gender.label

                val distanceState =  "distance (m): ${when (distanceToFace) {
                    in 1..2 -> "Very close"
                    in 2..6 -> "Normal"
                    in 6..100 -> "Far"
                    else -> "Not detected"
                }
                }"

                stateView.text = state
                stateViewForDistance.text = distanceState

                when (distanceToFace) {
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

    private fun initScreen() {
        initViews()
        initCameraPreview()
    }

    private fun initViews() {
        outputTexture = findViewById(R.id.output_texture)
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
                        Toast.makeText(this@RenderScriptActivity,
                                getString(R.string.permissions_error), Toast.LENGTH_SHORT).show()
                    }
                })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        PermissionsHelper.onPermissionsResult(requestCode, grantResults)
    }

    // endregion
}
