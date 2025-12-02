package com.wakala.fakhr.model

import android.graphics.Rect

data class ChoiceBox(
    val text: String,
    val rect: Rect
)

data class ChoicePrediction(
    val text: String,
    val rect: Rect,
    val confidence: Float,
    val isWinner: Boolean
)
