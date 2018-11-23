package com.tensorflow.humanrec.processing.processing

import org.opencv.core.Mat

interface IFaceProcessing {
    fun feed(mat: Mat)
    fun rgbaResizedMat(): Mat?
    fun rotate()
    fun resize()
    fun addProcessingSteps(steps: ArrayList<() -> Unit>)
    fun startProcessing()
    fun frameRate(): Long
    fun initialize()
}
