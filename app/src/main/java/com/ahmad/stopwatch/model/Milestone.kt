package com.ahmad.stopwatch.model

import org.joda.time.Duration

data class Milestone(
    val id:String,
    val name:String,
    val duration: Duration,
    val passed: Boolean
)