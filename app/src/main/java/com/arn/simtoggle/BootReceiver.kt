package com.arn.simtoggle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import com.arn.simtoggle.MainActivity.Companion.toggleSim

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            val telephonyManager by lazy { context.getSystemService(AppCompatActivity.TELEPHONY_SERVICE) as TelephonyManager }
            val prefs = SimTogglePrefs(context)

            if (!prefs.setOnBoot)
                return

            if (prefs.powerState0 != null)
                telephonyManager.toggleSim(0, prefs.powerState0!!)

            if (prefs.powerState1 != null)
                telephonyManager.toggleSim(1, prefs.powerState1!!)

        }
    }
}