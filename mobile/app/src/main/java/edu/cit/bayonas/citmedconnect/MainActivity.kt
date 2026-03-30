package edu.cit.bayonas.citmedconnect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.cit.bayonas.citmedconnect.ui.LoginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Redirect to Login Activity
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
