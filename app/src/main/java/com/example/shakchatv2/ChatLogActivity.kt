package com.example.shakchatv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<ViewHolder>()
    companion object {
        var user: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recycler_view_chat_log.adapter = adapter

        user = intent.getParcelableExtra<User>("UserData")

        if (user != null) {
            supportActionBar?.title = user!!.username
        } else {
            supportActionBar?.title = "error user"
        }

        listenForMessages()

        button_send_message.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatesMessagesActivity.currentUser ?: return
                        adapter.add(ChatToItem(chatMessage.text, currentUser))
                    } else {
                        adapter.add(ChatFromItem(chatMessage.text, user!!))
                    }
                }

                recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage(){
        val text = edit_text_message.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid

        if (fromId == null) return
        if (toId == null) return

        // Получаем сообщение с обоих сторон
        val reference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$fromId/$toId").push()

        val toReference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$toId/$fromId").push()


        // Добавить методы .addSuccess и .addFailure
        val chatMessage = ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                edit_text_message.text.clear()
                recycler_view_chat_log.scrollToPosition(adapter.itemCount - 1)
            }

        toReference.setValue(chatMessage)

        // Последнее сообщение
        val latestMessageFromRef = FirebaseDatabase.getInstance()
            .getReference("latest-messages/$fromId/$toId")
        latestMessageFromRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance()
            .getReference("latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }
}

class ChatFromItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_view_chat_from.text = text

        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.image_view_chat_from)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.text_view_chat_to.text = text

        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.image_view_chat_to)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}