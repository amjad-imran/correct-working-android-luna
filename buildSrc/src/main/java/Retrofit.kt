object Retrofit {

    private const val retrofitVersion = "2.9.0"
    val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    val converter = "com.squareup.retrofit2:converter-gson:$retrofitVersion"

    private const val loggingInterceptorVersion = "4.5.0"
    val okttp3Interceptor =
        "com.squareup.okhttp3:logging-interceptor:$loggingInterceptorVersion"

    val brcypt = "org.mindrot:jbcrypt:0.4"
}