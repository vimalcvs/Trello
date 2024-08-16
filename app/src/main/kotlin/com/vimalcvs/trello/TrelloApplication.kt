package com.vimalcvs.trello

import android.app.Application
import com.bumptech.glide.Glide
import com.google.android.material.color.DynamicColors

class TrelloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Glide.get(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
