package com.noisefit_zhsdk.log

/**
 * Created by Android on 2023/7/4.
 */
abstract class AbsLoggerBuilder {
    //设置是否需要写日志
    abstract fun setIsWriteLog(isWriteLog: Boolean):AbsLoggerBuilder
    //设置日志过期时间
    abstract fun setExpiredDay(expiredDay: Int):AbsLoggerBuilder
    //设置日志路径
    abstract fun setFileDirPath(dir:String?):AbsLoggerBuilder
    //设置文件前缀
    abstract fun setPrefixFlag(flag: String?):AbsLoggerBuilder
    //设置文件后缀
    abstract fun setSuffixFlag(flag: String?):AbsLoggerBuilder
    //获取最终产品
    abstract fun build(): Logger
}