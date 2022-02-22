package com.arn.simtoggle

import android.content.Context
import hu.autsoft.krate.SimpleKrate
import hu.autsoft.krate.booleanPref
import hu.autsoft.krate.default.withDefault

class SimTogglePrefs(context: Context) : SimpleKrate(context) {
    var powerState0 by booleanPref("power_state1")
    var powerState1 by booleanPref("power_state2")

    var setOnBoot by booleanPref("set_on_boot").withDefault(false)
}