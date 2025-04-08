package com.oreo.util.moengage.inapp

import com.moengage.inapp.listeners.OnClickActionListener
import com.moengage.inapp.model.ClickData
import com.moengage.inapp.model.actions.NavigationAction
import com.noisefit.session.SessionManager
import com.noisefit_commans.utils.LOGS


class ClickActionCallback(val sessionManager: SessionManager) : OnClickActionListener {

    override fun onClick(clickData: ClickData): Boolean {

        if(clickData.action is NavigationAction){
            val action = (clickData.action as NavigationAction).navigationUrl

            sessionManager.moengageNavigateTo(action)


            LOGS.d("moengage_test action $action")
        }
        LOGS.d("moengage_test onClick() $clickData")
        // return true if the application is handling else false
        return false
    }
}