package tom.memo

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_pre_login.*

class PreLoginActivity : FragmentActivity() {

    private lateinit var auth: FirebaseAuth
    private var stayLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_login)

        // Object used for validating the current user
        auth = FirebaseAuth.getInstance()

        // Load Colour Setting from previous session
        var settings = applicationContext.getSharedPreferences("USER_DATA", 0)
        stayLoggedIn = settings.getBoolean("stayLoggedIn", false)

        // Change to the Main Activity if the user is already logged in and has the stay logged in tick box checked
        if (auth.currentUser != null && stayLoggedIn) {
            startActivity(Intent(this@PreLoginActivity, MainActivity::class.java))
        }

        // Move to the login activity
        registration_button.setOnClickListener {
            startActivity(Intent(this@PreLoginActivity, LoginActivity::class.java))
        }

        // Move to the sign up activity
        signup_button.setOnClickListener {
            startActivity(Intent(this@PreLoginActivity, RegistrationActivity::class.java))
        }
    }
}
