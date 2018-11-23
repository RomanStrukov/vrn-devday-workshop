package com.tensorflow.humanrec.processing.agegender

import android.content.Context
import com.tensorflow.humanrec.model.Age
import com.tensorflow.humanrec.model.Gender

interface IAgeGenderDetector {
    fun initialize(context: Context)
    fun fetchAge(floatValues: FloatArray): Age
    fun fetchGender(floatValues: FloatArray): Gender
}
