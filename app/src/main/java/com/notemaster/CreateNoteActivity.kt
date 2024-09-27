package com.notemaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        db = FirebaseFirestore.getInstance()

        val titleEditText: EditText = findViewById(R.id.titleEditText)
        val contentEditText: EditText = findViewById(R.id.contentEditText)
        val saveButton: Button = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (content.isEmpty()) {
                Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentDate = System.currentTimeMillis()

            val note = hashMapOf(
                "title" to title,
                "content" to content,
                "date" to currentDate
            )

            db.collection("notes")
                .add(note)
                .addOnSuccessListener { documentReference ->
                    val newNote = Note(
                        id = documentReference.id,
                        title = title,
                        content = content,
                        date = currentDate
                    )

                    val resultIntent = Intent()
                    resultIntent.putExtra("NEW_NOTE", newNote)
                    setResult(RESULT_OK, resultIntent)
                    finish()  // Close the activity and return to MainActivity
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to add note: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
