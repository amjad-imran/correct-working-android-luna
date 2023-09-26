package com.noisefit_commans.constants

sealed class SyncEvents {
    data class Started(val progress: Int, val total: Int) : SyncEvents()
    data class InProgress(val progress: Int, val total: Int) : SyncEvents()
    data class Success(val progress: Int, val total: Int) : SyncEvents()
    object ServerSyncStarted : SyncEvents()
    object ServerSyncSuccess : SyncEvents()
    object Failed : SyncEvents()
}

object ConnectionEventsConstants {
    const val Disconnect_success = "disconnect_success"
    const val Success = "success"
    const val Connecting = "connecting"
    const val Failed = "failed"
    const val Disconnected = "disconnected"
    const val Timeout = "timeout"
    const val Retry = "retry"
}

object WatchFaceEventsConstants {
    const val Timeout = "timeout"
    const val Busy = "Busy"
    const val Failed = "failed"
    const val Battery_low = "Battery low"
    const val Complete = "complete"
    const val Empty_File = "empty file"
    const val Empty_File_2 = "empty file 2"
    const val Already_Installed = "already installed"
}