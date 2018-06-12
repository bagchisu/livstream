package io.livstream.model

import java.io.Serializable

data class LivDuring (
        val fromTime: String = "",
        val toTime: String = "",
        val dow: String = ""
) : Serializable