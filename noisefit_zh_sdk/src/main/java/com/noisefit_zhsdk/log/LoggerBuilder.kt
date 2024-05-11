package com.noisefit_zhsdk.log

import android.content.Context

/**
 * Created by Android on 2023/7/4.
 */
class LoggerBuilder(mContext: Context) : AbsLoggerBuilder() {

    private var logger: Logger = Logger(mContext)

    override fun setIsWriteLog(isWriteLog: Boolean): LoggerBuilder {
        logger.setIsWriteLog(isWriteLog)
        return this
    }

    override fun setExpiredDay(expiredDay: Int): LoggerBuilder {
        logger.setExpiredDay(expiredDay)
        return this
    }

    override fun setFileDirPath(dir: String?): LoggerBuilder {
        logger.setFileDirPath(dir)
        return this
    }

    override fun setPrefixFlag(flag: String?): LoggerBuilder {
        logger.setPrefixFlag(flag)
        return this
    }

    override fun setSuffixFlag(flag: String?): LoggerBuilder {
        logger.setSuffixFlag(flag)
        return this
    }


    override fun build(): Logger {
        logger.clearExpiredFile()
        return logger
    }
}