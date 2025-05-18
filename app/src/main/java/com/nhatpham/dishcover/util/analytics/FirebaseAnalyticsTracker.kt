package com.nhatpham.dishcover.util.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.nhatpham.dishcover.util.error.AppError
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of the AnalyticsTracker
 */
@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun logError(error: AppError, additionalInfo: Map<String, Any>?) {
        val errorBundle = Bundle().apply {
            putString("error_type", error::class.simpleName)
            putString("error_message", error.message)
            putString("error_cause", error.cause?.toString())

            // Add additional info
            additionalInfo?.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }

        firebaseAnalytics.logEvent("app_error", errorBundle)
    }

    override fun logEvent(name: String, params: Map<String, Any>?) {
        val bundle = Bundle().apply {
            params?.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Float -> putFloat(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }

        firebaseAnalytics.logEvent(name, bundle)
    }
}