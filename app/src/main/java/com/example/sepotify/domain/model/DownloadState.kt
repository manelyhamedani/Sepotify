package com.example.sepotify.domain.model

sealed class DownloadState {
    object NotStarted : DownloadState()
    object Pending : DownloadState()
    data class Downloading(val progress: Float = 0f) : DownloadState()
    object Completed : DownloadState()
    object Failed : DownloadState()
}