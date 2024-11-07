package com.example.munch_cmpt362

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.munch_cmpt362.group.GroupFragment

class MainActivity : AppCompatActivity() {
    private val groupTAG = "group tag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)


        // Start the GROUP fragment
        if(supportFragmentManager.findFragmentByTag(groupTAG) == null) {
            val groupFragment = GroupFragment()
            supportFragmentManager.beginTransaction().add(android.R.id.content, groupFragment, groupTAG)
                .commit()
        }
    }
}