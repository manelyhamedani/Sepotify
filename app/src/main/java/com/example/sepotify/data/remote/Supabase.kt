package com.example.sepotify.data.remote

import com.example.sepotify.utils.Constants
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json

object Supabase {
    private const val SUPABASE_URL = Constants.SUPABASE_URL
    private const val SUPABASE_KEY = Constants.SUPABASE_KEY
    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }


    @OptIn(SupabaseInternal::class)
    val client: SupabaseClient by lazy {
        createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
            httpConfig {
                install(Logging) {
                    level = LogLevel.ALL
                }
            }
            defaultSerializer = KotlinXSerializer(json)
        }
    }
}