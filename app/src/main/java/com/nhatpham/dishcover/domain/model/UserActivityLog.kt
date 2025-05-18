package com.nhatpham.dishcover.domain.model

import java.util.Date

data class UserActivityLog(
    val logId: String? = null, //Firestore ID
    val userId: String? = null, // FK to USERS
    val activityType: String? = null,
    val activityDetails: String? = null,
    val activityTime: Date? = null,
    val ipAddress: String? = null
)