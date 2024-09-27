package com.notemaster

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(var notes: List<Note>, private val context: Context) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.bind(note)

        // Handle Edit Note Click
        holder.editNoteIcon.setOnClickListener {
            val intent = Intent(context, EditNoteActivity::class.java)
            intent.putExtra("NOTE_ID", note.id)
            context.startActivity(intent)
        }

        // Handle Delete Note Click
        holder.deleteNoteIcon.setOnClickListener {
            showDeleteConfirmationDialog(note.id, position)
        }
    }

    override fun getItemCount(): Int = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    // ViewHolder class for each note item
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.noteTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.noteContent)
        private val dateTextView: TextView = itemView.findViewById(R.id.noteDate)  // Date TextView
        val editNoteIcon: ImageView = itemView.findViewById(R.id.editNoteIcon)
        val deleteNoteIcon: ImageView = itemView.findViewById(R.id.deleteNoteIcon)

        // Bind data to the views
        fun bind(note: Note) {
            titleTextView.text = note.title
            contentTextView.text = note.content

            // Format and set the note date
            val date = Date(note.date)  // Assumes note.date is a long (timestamp)
            val formatter = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()) // Example: June 15, 2024
            dateTextView.text = formatter.format(date)
        }
    }

    // Function to show delete confirmation dialog
    private fun showDeleteConfirmationDialog(noteId: String?, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Note")
        builder.setMessage("Are you sure you want to delete this note?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            deleteNote(noteId, position)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // Function to delete the note from Firestore and update the RecyclerView
    private fun deleteNote(noteId: String?, position: Int) {
        if (noteId != null) {
            FirebaseFirestore.getInstance().collection("notes")
                .document(noteId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show()
                    // Remove the note from the list and notify the adapter
                    val updatedList = notes.toMutableList()
                    updatedList.removeAt(position)
                    notes = updatedList
                    notifyItemRemoved(position)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Failed to delete note: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
