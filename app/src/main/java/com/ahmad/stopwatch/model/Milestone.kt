package com.ahmad.stopwatch.model

import org.joda.time.Period
import java.io.Serializable

data class Milestone(
    val id:String,
    val name:String,
    val period: Period,
    val passed: Boolean
):Serializable