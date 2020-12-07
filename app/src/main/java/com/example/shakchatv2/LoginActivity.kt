package com.example.shakchatv2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener {
            val email = edit_text_login_email.text.toString()
            val password = edit_text_login_password.text.toString()

            if (email.isEmpty() || password.isEmpty()){
                toast("Please, enter email/password")
                return@setOnClickListener
            } else {
                loginUser(email, password)
            }
        }

        text_view_back_to_register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    private fun loginUser(email : String, password : String){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                val intent = Intent(this, LatesMessagesActivity::class.java)
                startActivity(intent)
                toast("Successfully login user")
            }
            .addOnFailureListener {
                toast("Error login user")
            }
    }

    private fun toast(text : String){
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}