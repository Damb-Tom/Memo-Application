package tom.memo

import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import android.content.Intent
import android.support.v4.app.FragmentActivity
import android.view.View
import com.google.firebase.auth.FirebaseUser


class LoginActivity : FragmentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private var stayLoggedIn: Boolean = false

    // See if the user has validated their email, if they have it'll send them to the Main Activity
    private fun checkEmailVerification(): Boolean {
        user = auth.currentUser!!
        var emailVerified: Boolean = user.isEmailVerified

        // Move to Main Activity
        if(emailVerified) {
            Toast.makeText(baseContext, "Successfully Logged-In!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            return true
        }
        return false
    }

    private fun validateEmail() {
        if(!checkEmailVerification()){
            user.sendEmailVerification()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Verification Email sent to ${user.email}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(baseContext, "Verification failed!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // FireBase authentication object is used here for the Login and Password Reset
        auth = FirebaseAuth.getInstance()

        // Get Stored Local Data for the Stay Logged In checkbox
        var settings = applicationContext.getSharedPreferences("USER_DATA", 0)
        stayLoggedIn = settings.getBoolean("stayLoggedIn", false)
        stay_logged_in_check.isChecked = stayLoggedIn


        // Store Local Data for the Stay Logged In checkbox
        stay_logged_in_check.setOnCheckedChangeListener { _, isChecked ->
            stayLoggedIn = isChecked

            val editor = settings.edit()
            editor.putBoolean("stayLoggedIn", stayLoggedIn)
            editor.apply()
        }

        // Login Button Pressed
        login_button.setOnClickListener {
            val email = email_field.text.toString()
            val password = password_field.text.toString()

            // Ensure Email and Password fields aren't empty / null
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(baseContext, "Please enter an Email and Password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Enable Progress Bar
            progress_bar_login.visibility = View.VISIBLE

            // Process FireBase Login Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        progress_bar_login.visibility = View.INVISIBLE
                        validateEmail()
                    } else {
                        Toast.makeText(baseContext, "Invalid Username or Password, try again!",
                            Toast.LENGTH_SHORT).show()
                        password_field.setText("")
                    }
            }
        }

        // Forgot Password Button Pressed
        forgot_password_button.setOnClickListener {
            val email = email_field.text.toString()
            if(email.isEmpty()) {
                Toast.makeText(baseContext, "Please enter an Email!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Process FireBase Reset Password Email
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(baseContext, "Password reset email has been sent!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(baseContext, "An account doesn't exist with that email!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
