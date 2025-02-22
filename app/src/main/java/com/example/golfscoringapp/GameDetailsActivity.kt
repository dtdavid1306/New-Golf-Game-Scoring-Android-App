package com.example.golfscoringapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.golfscoringapp.ResultsActivity // Added import

class GameDetailsActivity : AppCompatActivity() {

    private lateinit var player1NameLayout: TextInputLayout
    private lateinit var player2NameLayout: TextInputLayout
    private lateinit var player3NameLayout: TextInputLayout
    private lateinit var player4NameLayout: TextInputLayout
    private lateinit var gameLocationLayout: TextInputLayout
    private lateinit var gameDateLayout: TextInputLayout
    private lateinit var betUnitsLayout: TextInputLayout
    private lateinit var nextButton: MaterialButton
    private lateinit var toScoringButton: MaterialButton
    private lateinit var toResultsButton: MaterialButton
    private lateinit var toSummaryButton: MaterialButton
    private lateinit var restartButton: MaterialButton
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_details)

        // Initialize SharedPreferences
        prefs = getSharedPreferences("GolfScoringPrefs", MODE_PRIVATE)

        // Initialize views with Material Design components
        player1NameLayout = findViewById(R.id.player1_name_layout)
        player2NameLayout = findViewById(R.id.player2_name_layout)
        player3NameLayout = findViewById(R.id.player3_name_layout)
        player4NameLayout = findViewById(R.id.player4_name_layout)
        gameLocationLayout = findViewById(R.id.game_location_layout)
        gameDateLayout = findViewById(R.id.game_date_layout)
        betUnitsLayout = findViewById(R.id.bet_units_layout)
        nextButton = findViewById(R.id.next_button)
        toScoringButton = findViewById(R.id.to_scoring_button)
        toResultsButton = findViewById(R.id.to_results_button)
        toSummaryButton = findViewById(R.id.to_summary_button)
        restartButton = findViewById(R.id.restart_button)

        // Load saved data with error handling
        loadGameDetails()

        nextButton.setOnClickListener {
            if (validateInput()) {
                saveGameDetails()
                val intent = Intent(this, ScoringActivity::class.java).apply {
                    putExtra("PLAYER_1_NAME", player1NameLayout.editText?.text.toString().ifEmpty { "Player 1" })
                    putExtra("PLAYER_2_NAME", player2NameLayout.editText?.text.toString().ifEmpty { "Player 2" })
                    putExtra("PLAYER_3_NAME", player3NameLayout.editText?.text.toString().ifEmpty { "Player 3" })
                    putExtra("PLAYER_4_NAME", player4NameLayout.editText?.text.toString().ifEmpty { "Player 4" })
                    putExtra("GAME_LOCATION", gameLocationLayout.editText?.text.toString().ifEmpty { "" })
                    putExtra("GAME_DATE", gameDateLayout.editText?.text.toString().ifEmpty { "" })
                    putExtra("BET_UNITS", betUnitsLayout.editText?.text.toString().toIntOrNull() ?: 1) // Default to 1 if invalid
                }
                startActivity(intent)
            }
        }

        toScoringButton.setOnClickListener {
            startActivity(Intent(this, ScoringActivity::class.java))
        }

        toResultsButton.setOnClickListener {
            startActivity(Intent(this, ResultsActivity::class.java))
        }

        toSummaryButton.setOnClickListener {
            startActivity(Intent(this, ResultsSummaryActivity::class.java))
        }

        // Restart Game button with confirmation dialog
        restartButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Restart Game")
                .setMessage("Are you sure you want to restart? All current data will be lost.")
                .setPositiveButton("Yes") { dialog: android.content.DialogInterface, which: Int ->
                    clearAllData()
                    clearFields()
                    Toast.makeText(this, "Game restarted. Enter new details.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No") { dialog: android.content.DialogInterface, which: Int ->
                    dialog.dismiss()
                }
                .show()
        }

        // Handle configuration changes
        if (savedInstanceState != null) {
            loadGameDetails()
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        if (player1NameLayout.editText?.text.isNullOrEmpty()) {
            player1NameLayout.error = "Player 1 name is required"
            isValid = false
        } else {
            player1NameLayout.error = null
        }
        if (player2NameLayout.editText?.text.isNullOrEmpty()) {
            player2NameLayout.error = "Player 2 name is required"
            isValid = false
        } else {
            player2NameLayout.error = null
        }
        if (player3NameLayout.editText?.text.isNullOrEmpty()) {
            player3NameLayout.error = "Player 3 name is required"
            isValid = false
        } else {
            player3NameLayout.error = null
        }
        if (player4NameLayout.editText?.text.isNullOrEmpty()) {
            player4NameLayout.error = "Player 4 name is required"
            isValid = false
        } else {
            player4NameLayout.error = null
        }
        val betUnitsText = betUnitsLayout.editText?.text.toString()
        if (betUnitsText.isEmpty() || betUnitsText.toIntOrNull() ?: 0 <= 0) {
            betUnitsLayout.error = "Bet units must be a positive number"
            isValid = false
        } else {
            betUnitsLayout.error = null
        }
        return isValid
    }

    private fun saveGameDetails() {
        try {
            with(prefs.edit()) {
                putString("PLAYER_1_NAME", player1NameLayout.editText?.text.toString())
                putString("PLAYER_2_NAME", player2NameLayout.editText?.text.toString())
                putString("PLAYER_3_NAME", player3NameLayout.editText?.text.toString())
                putString("PLAYER_4_NAME", player4NameLayout.editText?.text.toString())
                putString("GAME_LOCATION", gameLocationLayout.editText?.text.toString())
                putString("GAME_DATE", gameDateLayout.editText?.text.toString())
                putInt("BET_UNITS", betUnitsLayout.editText?.text.toString().toIntOrNull() ?: 1)
                apply()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving game details: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadGameDetails() {
        try {
            player1NameLayout.editText?.setText(prefs.getString("PLAYER_1_NAME", "") ?: "")
            player2NameLayout.editText?.setText(prefs.getString("PLAYER_2_NAME", "") ?: "")
            player3NameLayout.editText?.setText(prefs.getString("PLAYER_3_NAME", "") ?: "")
            player4NameLayout.editText?.setText(prefs.getString("PLAYER_4_NAME", "") ?: "")
            gameLocationLayout.editText?.setText(prefs.getString("GAME_LOCATION", "") ?: "")
            gameDateLayout.editText?.setText(prefs.getString("GAME_DATE", "") ?: "")
            betUnitsLayout.editText?.setText(prefs.getInt("BET_UNITS", 1).toString())
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading game details: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearAllData() {
        try {
            with(prefs.edit()) {
                clear()
                apply()
            }
            Toast.makeText(this, "All data cleared successfully.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error clearing data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        player1NameLayout.editText?.text?.clear()
        player2NameLayout.editText?.text?.clear()
        player3NameLayout.editText?.text?.clear()
        player4NameLayout.editText?.text?.clear()
        gameLocationLayout.editText?.text?.clear()
        gameDateLayout.editText?.text?.clear()
        betUnitsLayout.editText?.text?.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveGameDetails()
    }
}