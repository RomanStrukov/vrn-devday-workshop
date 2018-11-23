package com.tensorflow.humanrec.utility.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.view.TextureView
import ru.faceprocessing.renderscript.FaceProcessingRenderScript

class CameraHelper {
    private lateinit var cameraManager: CameraManager
    private lateinit var outputTexture: AutoFitTextureView
    private lateinit var faceProcessingRenderScript: FaceProcessingRenderScript
    private var callback: CameraReadyCallback? = null

    @SuppressLint("MissingPermission")
    fun init(context: Activity, outputTexture: AutoFitTextureView,
             faceProcessingRenderScript: FaceProcessingRenderScript, callback: CameraReadyCallback) {
        this.callback = callback
        this.outputTexture = outputTexture
        this.faceProcessingRenderScript = faceProcessingRenderScript

        val cameraId = initCamera(context)

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: CameraDevice?) {
                cameraDevice ?: return

                if (outputTexture.isAvailable) {
                    createCaptureSession(cameraDevice)
                } else {
                    outputTexture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) { }
                        override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) { }
                        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?): Boolean = false

                        override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, p1: Int, p2: Int) {
                            createCaptureSession(cameraDevice)
                        }
                    }
                }
            }

            override fun onDisconnected(cameraDevice: CameraDevice?) { }
            override fun onError(cameraDevice: CameraDevice?, p1: Int) { }
        }, null)
    }

    private fun initCamera(context: Activity): String {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val camIds = cameraManager.cameraIdList
        val camId = camIds[1] // front camera

        chooseOptimalSize(camId)

        // if textureView rotation is wrong
        // outputTexture.rotation = 90f

        return camId
    }

    private fun chooseOptimalSize(camId: String) {
        val characteristics = cameraManager.getCameraCharacteristics(camId)
        val streamMap = characteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]
        val sizes = streamMap?.getOutputSizes(SurfaceTexture::class.java)

        val optimalSize = sizes?.filter { size -> size.width > FRAME_WIDTH && size.height > FRAME_HEIGHT }!![0]
        outputTexture.setAspectRatio(optimalSize.width, optimalSize.height)
    }

    private fun createCaptureSession(cameraDevice: CameraDevice) {
        val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        // feed camera preview frames to our processing thread
        val surfaces = faceProcessingRenderScript.connectToCamera(captureRequest, outputTexture.surfaceTexture)

        cameraDevice.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession?) {
                // ignore
            }

            override fun onConfigured(cameraCaptureSession: CameraCaptureSession?) {
                cameraCaptureSession ?: return

                captureRequest.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)

                callback?.ready()

                cameraCaptureSession.setRepeatingRequest(captureRequest.build(), null, null)
            }

        }, null)
    }

    companion object {
        const val FRAME_WIDTH = 640
        const val FRAME_HEIGHT = 480
    }
}
