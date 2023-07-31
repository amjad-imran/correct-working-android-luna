package com.noisefit_commans.constants

object EventConstants {
    const val UPDATE_STATUS_STARTED = "started"
    const val UPDATE_STATUS_IN_PROGRESS = "in_progress"
    const val UPDATE_STATUS_INTERRUPTED = "interrupted"
    const val UPDATE_STATUS_SUCCESS = "success"
    const val UPDATE_STATUS_FAILED = "failed"
}

object ConnectionEventsConstants{
    const val Disconnect_success = "disconnect_success"
    const val Success = "success"
    const val Connecting = "connecting"
    const val Failed = "failed"
    const val Disconnected = "disconnected"
    const val Timeout = "timeout"
    const val Retry = "retry"
}
object WatchFaceEventsConstants{
    const val Timeout = "timeout"
    const val Busy = "Busy"
    const val Failed = "failed"
    const val Battery_low = "Battery low"
    const val Complete = "complete"
    const val Empty_File = "empty file"
    const val Empty_File_2 = "empty file 2"
    const val Already_Installed = "already installed"
}