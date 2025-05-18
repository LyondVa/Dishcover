package com.nhatpham.dishcover.util.analytics

import com.nhatpham.dishcover.util.error.AppError

/**
 * Interface for tracking analytics events and errors
 */
interface AnalyticsTracker {
    /**
     * Log an error for analytics
     */
    fun logError(error: AppError, additionalInfo: Map<String, Any>? = null)

    /**
     * Log an analytics event with optional parameters
     */
    fun logEvent(name: String, params: Map<String, Any>? = null)
}