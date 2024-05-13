package com.noisefit_zhsdk.log

import android.content.Context

/**
 * Created by Android on 2023/7/4.
 */
class ZhLoggerBuilder(mContext: Context) : AbsLoggerBuilder() {

    private var zhLogger: ZhLogger = ZhLogger(mContext)

    override fun setIsWriteLog(isWriteLog: Boolean): ZhLoggerBuilder {
        zhLogger.setIsWriteLog(isWriteLog)
        return this
    }

    override fun setExpiredDay(expiredDay: Int): ZhLoggerBuilder {
        zhLogger.setExpiredDay(expiredDay)
        return this
    }

    override fun setFileDirPath(dir: String?): ZhLoggerBuilder {
        zhLogger.setFileDirPath(dir)
        return this
    }

    override fun setPrefixFlag(flag: String?): ZhLoggerBuilder {
        zhLogger.setPrefixFlag(flag)
        return this
    }

    override fun setSuffixFlag(flag: String?): ZhLoggerBuilder {
        zhLogger.setSuffixFlag(flag)
        return this
    }


    override fun build(): ZhLogger {
        zhLogger.clearExpiredFile()
        return zhLogger
    }
}