package com.notemaster

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class EditNoteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var noteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        db = FirebaseFirestore.getInstance()
        noteId = intent.getStringExtra("NOTE_ID")

        val titleEditText: TextInputEditText = findViewById(R.id.editNoteTitle)
        val contentEditText: TextInputEditText = findViewById(R.id.editNoteContent)
        val saveButton: MaterialButton = findViewById(R.id.saveNoteButton)

        if (noteId != null) {
            db.collection("notes").document(noteId!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        titleEditText.setText(document.getString("title"))
                        contentEditText.setText(document.getString("content"))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load note: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        saveButton.setOnClickListener {
            val updatedTitle = titleEditText.text.toString().trim()
            val updatedContent = contentEditText.text.toString().trim()

            if (updatedTitle.isNotEmpty() && updatedContent.isNotEmpty()) {
                val noteMap = mapOf("title" to updatedTitle, "content" to updatedContent)
                db.collection("notes").document(noteId!!)
                    .update(noteMap)
                    .addOnSuccessListener {
                        val updatedNote = Note(
                            id = noteId!!,
                            title = updatedTitle,
                            content = updatedContent,
                            date = System.currentTimeMillis() // Update the date
                        )

                        val resultIntent = Intent()
                        resultIntent.putExtra("UPDATED_NOTE", updatedNote)
                        setResult(RESULT_OK, resultIntent)
                        finish()  // Close the activity and return to MainActivity
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update note: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Both fields must be filled out", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
