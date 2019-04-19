@file:Suppress("unused", "UNUSED_VARIABLE")

package com.tensorflow.humanrec.processing.agegender

import android.content.Context
import android.os.Trace
import com.tensorflow.humanrec.model.Age
import com.tensorflow.humanrec.model.Gender
import com.tensorflow.humanrec.processing.face.FaceDetector.Companion.TENSOR_INPUT_SIZE
import com.tensorflow.humanrec.utility.endTrace
import com.tensorflow.humanrec.utility.executeWithTimeLog
import com.tensorflow.humanrec.utility.logEnabled
import com.tensorflow.humanrec.utility.trace
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import org.opencv.core.*
import org.opencv.core.CvType.channels



class AgeGenderDetector: IAgeGenderDetector {
    // tensor interfaces to run ml code
    private lateinit var ageInferenceInterface: TensorFlowInferenceInterface
    private lateinit var genderInferenceInterface: TensorFlowInferenceInterface

    // labels for tensor output
    private val ageLabels = listOf(0..2, 4..6, 8..12, 15..20, 25..32, 38..43, 48..53, 60..100)
    private val genderLabels = listOf(Gender.MALE, Gender.FEMALE)

    override fun initialize(context: Context) {
        // loading tensor models
        ageInferenceInterface = TensorFlowInferenceInterface(context.assets, "frozen_model.pb")
        genderInferenceInterface = TensorFlowInferenceInterface(context.assets, "frozen_model_gender.pb")
    }

    override fun fetchAge(rgbaMat: Mat?): Age {
        val inputName = "inputs"
        val inputSize = TENSOR_INPUT_SIZE.toLong()
        val outputName = "softmax_output:0"
        val outputNames = arrayListOf(outputName).toTypedArray()
        val outputs = FloatArray(8) // sizeof ageLabels

        if (logEnabled) {
            Trace.beginSection("feed")
        }
        val floatValuesTmp = floatArrayOf(1.2f, 0.6f, 1F, 4.8f, 3.3f, 1.6f, 0.4f, 2.3f, 0.3f)
        "feed".trace()

        // TODO
        // feed input to tensor
        //val buff = IntArray(rgbaMat!!.total().toInt() * rgbaMat.channels())
        //rgbaMat.get(0, 0, buff)
        //ageInferenceInterface.feed(inputName, buff, inputSize);

        endTrace()

        "run".trace()

        executeWithTimeLog {
            // TODO
            // run tensor network
            //ageInferenceInterface.run(outputNames);
        }

        endTrace()

        "fetch".trace()

        // TODO
        // get results from tensor
       // ageInferenceInterface.fetch(outputName, outputs)
        endTrace()


        // construct age model to show tensor output to user

        val ageLabelIndex = outputs.withIndex().maxBy { it.value }?.index

        val state = when (ageLabelIndex) {
            in 1..4 -> "Young"
            in 5..7 -> "Adult"
            8 -> "Elder"
            else  -> "Not detected"
        }

        return Age(ageLabels[ageLabelIndex ?: 0], state)
    }

    override fun fetchGender(floatValues: FloatArray): Gender {
        // TODO

        val inputName = "inputs"
        val inputSize = TENSOR_INPUT_SIZE.toLong()
        val outputName = "softmax_output:0"
        val outputNames = arrayListOf(outputName).toTypedArray()
        val outputs = FloatArray(2) // sizeof genderLabels

        "feed".trace()

        // TODO
        // feed input to tensor

        endTrace()

        // Run the inference call.

        "run".trace()

        executeWithTimeLog {
            // TODO
            // run tensor network
        }

        endTrace()

        // Copy the output Tensor back into the output array.
        "fetch".trace()

        // TODO
        // get results from tensor

        endTrace()

        // construct age model to show tensor output to user

        val genderLabelIndex = outputs.withIndex().maxBy { it.value }?.index
        return genderLabels[genderLabelIndex ?: 0]
    }
}
