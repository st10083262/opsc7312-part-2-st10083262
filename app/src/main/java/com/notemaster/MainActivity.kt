package com.notemaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private val REQUEST_CODE_ADD_NOTE = 1
    private val REQUEST_CODE_EDIT_NOTE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Set up Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        // Set up ActionBarDrawerToggle (hamburger icon)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.notesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        notesAdapter = NotesAdapter(emptyList(), this)
        recyclerView.adapter = notesAdapter

        // Fetch notes from Firestore
        fetchNotes()

        // Set up Floating Action Button to create a new note
        val addNoteFab: ExtendedFloatingActionButton = findViewById(R.id.addNoteFab)
        addNoteFab.setOnClickListener {
            val intent = Intent(this, CreateNoteActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)  // Start CreateNoteActivity
        }
    }

    // Fetch notes from Firestore
    private fun fetchNotes() {
        db.collection("notes")
            .get()
            .addOnSuccessListener { result ->
                val notesList = result.map { document ->
                    Note(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        content = document.getString("content") ?: "",
                        date = document.getLong("date") ?: System.currentTimeMillis()
                    )
                }
                notesAdapter.updateNotes(notesList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load notes: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("MainActivity", "Error fetching notes", exception)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_ADD_NOTE -> {
                    val newNote = data.getParcelableExtra<Note>("NEW_NOTE")
                    newNote?.let {
                        addNoteToRecyclerView(it)
                    }
                }
                REQUEST_CODE_EDIT_NOTE -> {
                    val updatedNote = data.getParcelableExtra<Note>("UPDATED_NOTE")
                    updatedNote?.let {
                        updateNoteInRecyclerView(it)
                    }
                }
            }
        }
    }

    // Function to add new note to RecyclerView
    private fun addNoteToRecyclerView(note: Note) {
        val updatedList = notesAdapter.notes.toMutableList()
        updatedList.add(0, note)  // Add the new note at the top of the list
        notesAdapter.updateNotes(updatedList)
        recyclerView.scrollToPosition(0)
    }

    // Function to update existing note in RecyclerView
    private fun updateNoteInRecyclerView(note: Note) {
        val updatedList = notesAdapter.notes.toMutableList()
        val index = updatedList.indexOfFirst { it.id == note.id }
        if (index != -1) {
            updatedList[index] = note
            notesAdapter.updateNotes(updatedList)
        }
    }

    // Handle Navigation Item Clicks
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val homeIntent = Intent(this, MainActivity::class.java)
                startActivity(homeIntent)  // Start MainActivity
            }
            R.id.nav_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)  // Start SettingsActivity
            }
            R.id.nav_image_search -> {
                val imageSearchIntent = Intent(this, ImageSearchActivity::class.java)
                startActivity(imageSearchIntent)  // Start ImageSearchActivity
            }
            R.id.nav_logout -> {
                FirebaseAuth.getInstance().signOut()  // Logout from Firebase, if applicable
                val loginIntent = Intent(this, LoginActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(loginIntent)  // Start LoginActivity
                finish()  // Close the current activity
            }
        }
        drawerLayout.closeDrawers()  // Close the drawer after selection
        return true
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(findViewById(R.id.nav_view))) {
            drawerLayout.closeDrawer(findViewById(R.id.nav_view))
        } else {
            super.onBackPressed()
        }
    }
}
