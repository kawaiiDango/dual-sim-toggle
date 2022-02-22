package com.arn.simtoggle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.arn.simtoggle.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val telephonyManager by lazy { getSystemService(TELEPHONY_SERVICE) as TelephonyManager }
    private val subscriptionManager by lazy { getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                populateSimInfo()
                permissionGranted = true
            }
        }

    private val prefs by lazy { SimTogglePrefs(this) }
    private var permissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.MODIFY_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Systemize this app", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            Toast.makeText(this, "Need phone permission", Toast.LENGTH_SHORT).show()

        } else {
            populateSimInfo()
            permissionGranted = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.setOnBoot.isChecked && permissionGranted) {
            prefs.powerState0 = binding.btnSim0.isChecked
            prefs.powerState1 = binding.btnSim1.isChecked
        }
    }

    private fun populateSimInfo() {
        binding.setOnBoot.isChecked = prefs.setOnBoot
        binding.setOnBoot.setOnCheckedChangeListener { _, isChecked ->
            prefs.setOnBoot = isChecked
        }

        val buttons = mutableListOf(binding.btnSim0, binding.btnSim1)

        buttons.forEachIndexed { index, button ->
            val subscriptionInfo =
                subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(index)
            if (subscriptionInfo != null) {
                button.text = subscriptionInfo.displayName
                button.isChecked = true

                button.setOnClickListener {
                    telephonyManager.toggleSim(subscriptionInfo.simSlotIndex, false)
                    button.isChecked = false
                }
            } else {
                button.text = getString(R.string.sim_n, index)
                button.isChecked = false

                button.setOnClickListener {
                    telephonyManager.toggleSim(index, true)
                    button.isChecked = true
                }
            }
        }
    }

    companion object {

        fun TelephonyManager.toggleSim(slot: Int, powerOn: Boolean) {
            val state = if (powerOn) 1 else 0
            javaClass.getDeclaredMethod(
                "setSimPowerStateForSlot",
                Int::class.java,
                Int::class.java
            ).apply {
                isAccessible = true
                invoke(this@toggleSim, slot, state)
            }
        }
    }
}