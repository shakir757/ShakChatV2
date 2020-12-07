package com.example.shakchatv2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"
        fetchUsers()
    }

    private fun fetchUsers(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()

                snapshot.children.forEach {
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))
                    } else {
                        Toast.makeText(applicationContext, "null", Toast.LENGTH_SHORT).show()
                    }
                }
                
                adapter.setOnItemClickListener { item, view ->
                    val userItem = item as UserItem

                    val intent = Intent(view.context, ChatLogActivity::class.java)
                    intent.putExtra("UserData", userItem.user)
                    startActivity(intent)
                }

                recycler_view_new_message.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NewMessageActivity, "Error: $error", Toast.LENGTH_SHORT).show()
                Log.d("NewMessagesD", "Error: $error")
            }
        })
    }
}

class UserItem(val user: User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int){
        viewHolder.itemView.text_view_new_message_username.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.image_view_new_message_person)
    }

    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}

/*
class CustomAdapter: RecyclerView.Adapter<ViewHolder>{
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }
}*/