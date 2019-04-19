package com.tensorflow.humanrec.processing.agegender

import android.content.Context
import com.tensorflow.humanrec.model.Age
import com.tensorflow.humanrec.model.Gender
import org.opencv.core.*

interface IAgeGenderDetector {
    fun initialize(context: Context)
    fun fetchAge(rgbaMat: Mat?): Age
    fun fetchGender(floatValues: FloatArray): Gender
}
