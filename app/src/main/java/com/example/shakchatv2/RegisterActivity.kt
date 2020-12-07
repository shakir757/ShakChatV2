package com.example.shakchatv2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        button_register.setOnClickListener {
            val email = edit_text_reg_email.text.toString()
            val password = edit_text_reg_password.text.toString()

            if (email.isEmpty() || password.isEmpty()){
                toast("Please, enter email/password")
                return@setOnClickListener
            } else {
                regUser(email, password)
            }
        }

        text_view_have_account.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        button_select_image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    var selectedPhotoUri : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circle_select_photo_reg.setImageBitmap(bitmap)
            button_select_image.alpha = 0f
        }
    }

    private fun regUser(email : String, password : String){
        progress_bar_reg.visibility = View.VISIBLE
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if(!it.isSuccessful) return@addOnCompleteListener
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                toast("Error: $it")
                Log.d("RegActivity", "Error: $it")
            }
    }

    private fun uploadImageToFirebaseStorage(){
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnCompleteListener {
                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                toast("Bro, I have error: $it")
                Log.d("RegActivity", "Error: $it")
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, edit_text_reg_username.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnCompleteListener {
                progress_bar_reg.visibility = View.INVISIBLE
                toast("User is registered in Firebase Database")

                val intent = Intent(this, LatesMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                toast("User is NOT registered in Firebase Database")
                Log.d("RegActivity", "Error: $it")
            }
    }

    private fun toast(text : String){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}