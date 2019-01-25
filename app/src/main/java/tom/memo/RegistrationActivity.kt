package tom.memo

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_registration.*
import com.google.firebase.auth.UserProfileChangeRequest



class RegistrationActivity : FragmentActivity() {

    private lateinit var auth: FirebaseAuth

    // Apply Display Name change on the user
    private fun updateDisplayName(name: String){
        val user = auth.currentUser
        if(user != null) {
            user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())
        }else{
            Toast.makeText(baseContext, "Error adding a Display Name!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // FireBase authentication object is used here for the Registration
        auth = FirebaseAuth.getInstance()

        // Registration Button Pressed
        registration_button.setOnClickListener {
            var name = displayname_field.text.toString()
            var email = email_field.text.toString()
            var password1 = password_field1.text.toString()
            var password2 = password_field2.text.toString()

            // Validate Input Fields to ensure they aren't empty / null
            if(email.isEmpty() || password1.isEmpty() || password2.isEmpty() || name.isEmpty()){
                Toast.makeText(baseContext, "Please enter an Display Name, Email and Password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password1 != password2){
                Toast.makeText(baseContext, "Passwords don't match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Process FireBase Registration Authentication
            auth.createUserWithEmailAndPassword(email, password1)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        updateDisplayName(name)
                        Toast.makeText(baseContext, "Account has been created, please Login!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegistrationActivity, LoginActivity::class.java))
                    } else {
                        Toast.makeText(baseContext, "Error creating account!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
