package com.ahmad.stopwatch.model

import org.joda.time.Period
import java.io.Serializable

data class Milestone(
    var id:String,
    var name:String,
    var period: Period,
    var passed: Boolean
):Serializable