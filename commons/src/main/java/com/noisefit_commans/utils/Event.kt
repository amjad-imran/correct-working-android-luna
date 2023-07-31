package com.noisefit_commans.utils

/**
 * Usage
 *   it.getContent()?.let { value->
 *   if(value) Do Stuff
 *   }
 */
class Event<out T>(private val content: T?){
    var hasBeenHandled = false
        private set

    fun getContent(): T?{
        return if(hasBeenHandled){
            null
        }else{
            hasBeenHandled = true
            content
        }
    }
    fun peekContent(): T? = content

}