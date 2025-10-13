package com.lsang.smokingcheck

import android.os.Bundle
import android.text.TextUtils.replace
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.lsang.smokingcheck.databinding.ActivityMainBinding
import com.lsang.smokingcheck.ui.HomeFragment
import com.lsang.smokingcheck.ui.StatsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, HomeFragment())
            }
        }

        b.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.tab_home -> {
                    supportFragmentManager.commit { replace(R.id.fragmentContainer, HomeFragment()) }
                    true
                }
                R.id.tab_stats -> {
                    supportFragmentManager.commit { replace(R.id.fragmentContainer, StatsFragment()) }
                    true
                }
                else -> false
            }
        }
    }
}
