package com.example.golfscoringapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.Spinner
import android.widget.TextView
import android.content.SharedPreferences
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.golfscoringapp.GameDetailsActivity // Added import
import com.example.golfscoringapp.ResultsActivity // Added import
import com.example.golfscoringapp.ResultsSummaryActivity // Added import

class ScoringActivity : AppCompatActivity() {

    private lateinit var player1ScoreLayout: TextInputLayout
    private lateinit var player2ScoreLayout: TextInputLayout
    private lateinit var player3ScoreLayout: TextInputLayout
    private lateinit var player4ScoreLayout: TextInputLayout
    private lateinit var holeSpinner: Spinner
    private lateinit var updateScoreButton: MaterialButton
    private lateinit var confirmScoresButton: MaterialButton
    private lateinit var player1Label: TextView
    private lateinit var player2Label: TextView
    private lateinit var player3Label: TextView
    private lateinit var player4Label: TextView
    private lateinit var backButton: MaterialButton
    private lateinit var toGameDetailsButton: MaterialButton
    private lateinit var toResultsButton: MaterialButton
    private lateinit var toSummaryButton: MaterialButton
    private lateinit var prefs: SharedPreferences

    private val scores = Array(4) { IntArray(18) { 0 } }
    private var allHolesScored = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoring)

        // Initialize SharedPreferences
        prefs = getSharedPreferences("GolfScoringPrefs", MODE_PRIVATE)

        // Retrieve data from GameDetailsActivity or SharedPreferences
        val p1Name = intent.getStringExtra("PLAYER_1_NAME") ?: prefs.getString("PLAYER_1_NAME", "Player 1") ?: "Player 1"
        val p2Name = intent.getStringExtra("PLAYER_2_NAME") ?: prefs.getString("PLAYER_2_NAME", "Player 2") ?: "Player 2"
        val p3Name = intent.getStringExtra("PLAYER_3_NAME") ?: prefs.getString("PLAYER_3_NAME", "Player 3") ?: "Player 3"
        val p4Name = intent.getStringExtra("PLAYER_4_NAME") ?: prefs.getString("PLAYER_4_NAME", "Player 4") ?: "Player 4"
        val location = intent.getStringExtra("GAME_LOCATION") ?: prefs.getString("GAME_LOCATION", "") ?: ""
        val date = intent.getStringExtra("GAME_DATE") ?: prefs.getString("GAME_DATE", "") ?: ""
        val betUnits = intent.getIntExtra("BET_UNITS", prefs.getInt("BET_UNITS", 0))

        // Load saved scores
        loadScores()

        // Initialize views
        player1Label = findViewById(R.id.player1Label)
        player2Label = findViewById(R.id.player2Label)
        player3Label = findViewById(R.id.player3Label)
        player4Label = findViewById(R.id.player4Label)
        player1ScoreLayout = findViewById(R.id.player1_score_layout)
        player2ScoreLayout = findViewById(R.id.player2_score_layout)
        player3ScoreLayout = findViewById(R.id.player3_score_layout)
        player4ScoreLayout = findViewById(R.id.player4_score_layout)
        holeSpinner = findViewById(R.id.holeSpinner)
        updateScoreButton = findViewById(R.id.updateScoreButton)
        confirmScoresButton = findViewById(R.id.confirmScoresButton)
        backButton = findViewById(R.id.back_button)
        toGameDetailsButton = findViewById(R.id.to_game_details_button)
        toResultsButton = findViewById(R.id.to_results_button)
        toSummaryButton = findViewById(R.id.to_summary_button)

        // Set player names
        player1Label.text = p1Name
        player2Label.text = p2Name
        player3Label.text = p3Name
        player4Label.text = p4Name

        // Set up spinner for hole selection
        ArrayAdapter.createFromResource(
            this@ScoringActivity, // Explicitly specify Context type
            R.array.hole_numbers,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item)
            holeSpinner.adapter = adapter
        }

        // Set initial hole and display current scores (only non-zero scores)
        updateHoleDisplay()

        // Check if all holes are scored
        checkAllHolesScored()

        holeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateHoleDisplay()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Update score button logic with auto-advance and input validation
        updateScoreButton.setOnClickListener {
            if (validateScores()) {
                val selectedHole = holeSpinner.selectedItemPosition + 1
                val scoreInputs = listOf(
                    player1ScoreLayout.editText?.text.toString().toIntOrNull(),
                    player2ScoreLayout.editText?.text.toString().toIntOrNull(),
                    player3ScoreLayout.editText?.text.toString().toIntOrNull(),
                    player4ScoreLayout.editText?.text.toString().toIntOrNull()
                )

                scores[0][selectedHole - 1] = scoreInputs[0]!!
                scores[1][selectedHole - 1] = scoreInputs[1]!!
                scores[2][selectedHole - 1] = scoreInputs[2]!!
                scores[3][selectedHole - 1] = scoreInputs[3]!!

                saveScores()
                Toast.makeText(this, "Scores updated for Hole $selectedHole", Toast.LENGTH_SHORT).show()

                // Clear fields after update
                player1ScoreLayout.editText?.text?.clear()
                player2ScoreLayout.editText?.text?.clear()
                player3ScoreLayout.editText?.text?.clear()
                player4ScoreLayout.editText?.text?.clear()

                // Auto-advance to next hole, up to Hole 18
                if (selectedHole < 18) {
                    holeSpinner.setSelection(selectedHole) // Move to next hole
                }
                checkAllHolesScored()
            }
        }

        // Confirm scores button logic with confirmation dialog
        confirmScoresButton.setOnClickListener {
            if (scores.all { playerScores -> playerScores.all { it > 0 } }) {
                AlertDialog.Builder(this)
                    .setTitle("Confirm Scores")
                    .setMessage("Are you sure you want to confirm these scores? This will finalize the game and proceed to results.")
                    .setPositiveButton("Yes") { dialog: android.content.DialogInterface, which: Int ->
                        saveGameDetails(p1Name, p2Name, p3Name, p4Name, location, date, betUnits)
                        val intent = Intent(this, ResultsActivity::class.java).apply {
                            putExtra("PLAYER_1_NAME", p1Name)
                            putExtra("PLAYER_2_NAME", p2Name)
                            putExtra("PLAYER_3_NAME", p3Name)
                            putExtra("PLAYER_4_NAME", p4Name)
                            putIntegerArrayListExtra("SCORES_P1", scores[0].toList().toCollection(ArrayList()))
                            putIntegerArrayListExtra("SCORES_P2", scores[1].toList().toCollection(ArrayList()))
                            putIntegerArrayListExtra("SCORES_P3", scores[2].toList().toCollection(ArrayList()))
                            putIntegerArrayListExtra("SCORES_P4", scores[3].toList().toCollection(ArrayList()))
                            putExtra("BET_UNITS", betUnits)
                            putExtra("GAME_LOCATION", location)
                            putExtra("GAME_DATE", date)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("No") { dialog: android.content.DialogInterface, which: Int ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                Toast.makeText(this, "Please enter scores for all 18 holes before confirming", Toast.LENGTH_SHORT).show()
            }
        }

        // Back button to return to GameDetailsActivity
        backButton.setOnClickListener {
            saveScores()
            finish()
        }

        // Navigation buttons with persistence and confirmation for results/summary
        toGameDetailsButton.setOnClickListener {
            saveScores()
            startActivity(Intent(this, GameDetailsActivity::class.java))
        }

        toResultsButton.setOnClickListener {
            saveScores()
            if (allHolesScored) {
                startActivity(Intent(this, ResultsActivity::class.java).apply {
                    putExtra("PLAYER_1_NAME", p1Name)
                    putExtra("PLAYER_2_NAME", p2Name)
                    putExtra("PLAYER_3_NAME", p3Name)
                    putExtra("PLAYER_4_NAME", p4Name)
                    putIntegerArrayListExtra("SCORES_P1", scores[0].toList().toCollection(ArrayList()))
                    putIntegerArrayListExtra("SCORES_P2", scores[1].toList().toCollection(ArrayList()))
                    putIntegerArrayListExtra("SCORES_P3", scores[2].toList().toCollection(ArrayList()))
                    putIntegerArrayListExtra("SCORES_P4", scores[3].toList().toCollection(ArrayList()))
                    putExtra("BET_UNITS", betUnits)
                    putExtra("GAME_LOCATION", location)
                    putExtra("GAME_DATE", date)
                })
            } else {
                Toast.makeText(this, "Please confirm scores for all 18 holes before navigating to Results", Toast.LENGTH_SHORT).show()
            }
        }

        toSummaryButton.setOnClickListener {
            saveScores()
            if (allHolesScored) {
                startActivity(Intent(this, ResultsSummaryActivity::class.java).apply {
                    putExtra("PLAYER_1_NAME", p1Name)
                    putExtra("PLAYER_2_NAME", p2Name)
                    putExtra("PLAYER_3_NAME", p3Name)
                    putExtra("PLAYER_4_NAME", p4Name)
                    putIntegerArrayListExtra("SCORES_P1", scores[0].toList().toCollection(ArrayList()))
                    putIntegerArrayListExtra("SCORES_P2", scores[1].toList().toCollection(ArrayList()))
                    putIntegerArrayListExtra("SCORES_P3", scores[2].toList().toCollection(ArrayList()))
                    putIntegerArrayListExtra("SCORES_P4", scores[3].toList().toCollection(ArrayList()))
                    putExtra("BET_UNITS", betUnits)
                    putExtra("GAME_LOCATION", location)
                    putExtra("GAME_DATE", date)
                })
            } else {
                Toast.makeText(this, "Please confirm scores for all 18 holes before navigating to Summary", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle configuration changes
        if (savedInstanceState != null) {
            loadScores()
            updateHoleDisplay()
            checkAllHolesScored()
        }
    }

    private fun validateScores(): Boolean {
        var isValid = true
        listOf(player1ScoreLayout, player2ScoreLayout, player3ScoreLayout, player4ScoreLayout).forEachIndexed { index, layout ->
            val scoreText = layout.editText?.text.toString()
            val score = scoreText.toIntOrNull()
            if (score == null || score !in 1..10) {
                layout.error = "Score must be between 1 and 10"
                isValid = false
            } else {
                layout.error = null
            }
        }
        return isValid
    }

    private fun updateHoleDisplay() {
        val selectedHole = holeSpinner.selectedItemPosition + 1
        // Only pre-populate with non-zero scores; leave blank (empty) if score is 0
        player1ScoreLayout.editText?.setText(if (scores[0][selectedHole - 1] > 0) scores[0][selectedHole - 1].toString() else "")
        player2ScoreLayout.editText?.setText(if (scores[1][selectedHole - 1] > 0) scores[1][selectedHole - 1].toString() else "")
        player3ScoreLayout.editText?.setText(if (scores[2][selectedHole - 1] > 0) scores[2][selectedHole - 1].toString() else "")
        player4ScoreLayout.editText?.setText(if (scores[3][selectedHole - 1] > 0) scores[3][selectedHole - 1].toString() else "")
    }

    private fun checkAllHolesScored() {
        allHolesScored = scores.all { playerScores -> playerScores.all { it > 0 } }
        updateScoreButton.visibility = if (allHolesScored) View.GONE else View.VISIBLE
        confirmScoresButton.visibility = if (allHolesScored) View.VISIBLE else View.GONE
    }

    private fun saveScores() {
        try {
            with(prefs.edit()) {
                for (player in 0 until 4) {
                    for (hole in 0 until 18) {
                        putInt("SCORE_P${player + 1}_H$hole", scores[player][hole])
                    }
                }
                apply()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving scores: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadScores() {
        try {
            for (player in 0 until 4) {
                for (hole in 0 until 18) {
                    scores[player][hole] = prefs.getInt("SCORE_P${player + 1}_H$hole", 0)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading scores: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveGameDetails(p1Name: String, p2Name: String, p3Name: String, p4Name: String, location: String, date: String, betUnits: Int) {
        try {
            with(prefs.edit()) {
                putString("PLAYER_1_NAME", p1Name)
                putString("PLAYER_2_NAME", p2Name)
                putString("PLAYER_3_NAME", p3Name)
                putString("PLAYER_4_NAME", p4Name)
                putString("GAME_LOCATION", location)
                putString("GAME_DATE", date)
                putInt("BET_UNITS", betUnits)
                apply()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving game details: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveScores()
        saveGameDetails(
            player1Label.text.toString(),
            player2Label.text.toString(),
            player3Label.text.toString(),
            player4Label.text.toString(),
            prefs.getString("GAME_LOCATION", "") ?: "",
            prefs.getString("GAME_DATE", "") ?: "",
            prefs.getInt("BET_UNITS", 0)
        )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        loadScores()
        updateHoleDisplay()
        checkAllHolesScored()
    }
}