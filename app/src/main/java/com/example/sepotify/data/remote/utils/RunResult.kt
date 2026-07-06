package com.example.sepotify.data.remote.utils

import android.util.Log
import com.example.sepotify.utils.Result

suspend inline fun <T> runResult(
    errorMessage: String,
    crossinline block: suspend () -> T
): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    Log.d("Supabase", e.message ?: errorMessage)
    Result.Error(e.message ?: errorMessage)
}