package com.tensorflow.humanrec.model

data class Age(
        val range: IntRange = 0..5,
        val label: String = ""
) {
    override fun toString(): String {
        return "age is between ${range.first} and  ${range.last}\nperson is $label"
    }
}
