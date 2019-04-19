package com.tensorflow.humanrec.processing.processing

import com.tensorflow.humanrec.utility.i
import org.opencv.core.Mat
import org.opencv.core.Core
import org.opencv.imgproc.Imgproc
import org.opencv.core.Size

class FaceProcessing: IFaceProcessing {
    // mat to store rotated image
    private var rgbaRotatedMat: Mat? = null

    // mat to store resized and previously rotated image
    private var rgbaResizedMat: Mat? = null

    // input mat
    private var rgbaMat: Mat? = null

    // frame rate counter
    private var frameRate = 0L

    @Volatile private var currentTime = System.currentTimeMillis()

    private lateinit var processingThread: Thread

    // our processing steps
    @Volatile private var steps: ArrayList<() -> Unit>? = null

    override fun initialize() {
        processingThread = object : Thread() {
            override fun run() {
                while (!interrupted()) {
                    // detect current frame rate
                    logFrameTime()

                    //steps = ArrayList()
                    rgbaRotatedMat = Mat()
                    rgbaResizedMat = Mat()
                    // pre process image
                    rotate()
                    resize()

                    // run processing steps if we have something to process

                    if (rgbaRotatedMat != null && !rgbaRotatedMat!!.size().empty()) {
                        steps?.forEach {
                            it.invoke()
                        }
                    }
                }
            }
        }
    }

    override fun rotate() {
        // if no input skip this step
        if (rgbaMat == null || rgbaMat!!.size().empty()) {
            return
        }

        // TODO
        // rotate input rgbaMat and store result to rgbaRotatedMat
        Core.rotate(rgbaMat, rgbaRotatedMat, Core.ROTATE_90_CLOCKWISE);
    }

    override fun resize() {
        // if no input skip this step
        if (rgbaRotatedMat == null || rgbaRotatedMat!!.size().empty()) {
            return
        }

        // TODO
        // reduce input rgbaRotatedMat and store result to rgbaResizedMat
        var sizeTmp = Size((rgbaMat!!.height() / 1.2).toDouble(), (rgbaMat!!.height() / 1.2).toDouble())
        Imgproc.resize(rgbaRotatedMat, rgbaResizedMat, sizeTmp)
        //rgbaResizedMat = rgbaRotatedMat
    }

    // region Not interesting

    override fun frameRate(): Long {
        return frameRate
    }

    override fun rgbaResizedMat(): Mat? = rgbaResizedMat

    override fun feed(mat: Mat) {
        this.rgbaMat = mat
    }

    override fun addProcessingSteps(steps: ArrayList<() -> Unit>) {
        this.steps = steps
    }

    override fun startProcessing() {
        processingThread.priority = Thread.MAX_PRIORITY
        processingThread.start()
    }

    private fun logFrameTime() {
        val frameT = System.currentTimeMillis()
        "Frame time: ${(frameT - currentTime)}".i()
        var time = frameT - currentTime

        if (time == 0L) {
            time = 1
        }

        frameRate = 1000 / time

        currentTime = frameT
    }

    // endregion
}
