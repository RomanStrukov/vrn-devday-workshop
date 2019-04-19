@file:Suppress("unused")

package com.tensorflow.humanrec.processing.face

import android.content.Context
import android.support.constraint.solver.widgets.Rectangle
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import org.opencv.core.CvType.channels



class FaceDetector: IFaceDetector {
    // output of CascadeClassifier
    public var faces: MatOfRect? = null

    // CascadeClassifier instance
    private var faceCascade: CascadeClassifier? = null

    private var mat: Mat? = null
    private var rgbMat: Mat? = null
    private var face: Mat? = null
    private var input: Mat? = null

    // rect to store face if found
    private var faceRect: Rect? = null

    // buffer for storing reduced rgba camera frame
    private var imageBuffer = ByteArray(128 * 128 * 4)

    // buffer for storing face rect
    private var facesBuffer = FloatArray(3 * 227 * 227)

    override fun initialize(context: Context, imageBuffer: ByteArray, facesBuffer: FloatArray) {
        this.imageBuffer = imageBuffer
        this.facesBuffer = facesBuffer

        // reading cascade file or load it from assets if file is not existing

        val file = File(context.filesDir, "cascade.xml")

        if(!file.exists()) {
            val filename = "cascade.xml"
            val inputStream = context.assets.open("haarcascade_frontalface_alt.xml")

            val outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)

            val bufferLocal = ByteArray(1024)
            var len: Int = inputStream.read(bufferLocal)
            while ((len ) != -1) {
                outputStream.write(bufferLocal, 0, len)
                len = inputStream.read(bufferLocal)
            }
            outputStream.close()
            inputStream.close()
        }

        faceCascade = CascadeClassifier()
        faceCascade?.load(file.absolutePath)
    }

    override fun detectFaces(inputMat: Mat?) {
        faces = MatOfRect()

        // if we have inputMat as argument then we just use it
        // else we create mat with size and format of out pre process step output type
        mat = inputMat ?: Mat(REDUCED_RGBA_SIZE, REDUCED_RGBA_SIZE, CvType.CV_8UC4)

        // if we have inputMat as argument then we just use it
        // else we get out input from image buffer
        // depending on the processing type we can get out input in different ways

        if (inputMat == null) {
            mat?.put(0, 0, imageBuffer)
        }

        // TODO
        // run face cascade to find faces on camera frame

        var grayScale = Mat()
        Imgproc.cvtColor(inputMat, grayScale, Imgproc.COLOR_BGRA2GRAY);
        faceCascade!!.detectMultiScale(grayScale, faces)
    }

    override fun detectDistance(): Double {
        // TODO
//        var currentFace = faces!!.toList()[0]
//        var distancei = (2 * 3.14 * 180) / (12 + 11 )
//        var distance = distancei * 2.54
//        distance = Math.floor(distance)
//        return distance
        return 0.0;
    }

    override fun processFaces(): Boolean {
        val facesDetected = faces?.size()?.empty() == false

        if (facesDetected) {
            // TODO
            // process image to feed to tensor
           return true;
        }

        return facesDetected
    }

    override fun releaseObjects() {
        // release all objects to free memory

        if (faces?.size()?.empty() == false) {
            rgbMat?.release()
            face?.release()
            input?.release()
        }

        mat?.release()
    }

    companion object {
        const val REDUCED_RGBA_SIZE = 128
        const val TENSOR_INPUT_SIZE = 227
    }
}
