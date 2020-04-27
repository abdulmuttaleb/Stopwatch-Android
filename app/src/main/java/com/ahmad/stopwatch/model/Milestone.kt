package com.ahmad.stopwatch.model

import org.joda.time.Duration
import org.joda.time.Period

data class Milestone(
    val id:String,
    val name:String,
    val period: Period,
    val passed: Boolean
)