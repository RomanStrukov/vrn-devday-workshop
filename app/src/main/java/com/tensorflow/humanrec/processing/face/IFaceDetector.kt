package com.tensorflow.humanrec.processing.face

import android.content.Context
import org.opencv.core.Mat

interface IFaceDetector {
    fun initialize(context: Context, imageBuffer: ByteArray, facesBuffer: FloatArray)
    fun detectFaces(inputMat: Mat? = null)
    fun detectDistance(): Double
    fun processFaces(): Boolean
    fun releaseObjects()
}
