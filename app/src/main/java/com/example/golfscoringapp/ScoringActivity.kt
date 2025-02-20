package com.example.golfscoringapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Intent

class ScoringActivity : AppCompatActivity() {

    private lateinit var player1Score: EditText
    private lateinit var player2Score: EditText
    private lateinit var player3Score: EditText
    private lateinit var player4Score: EditText
    private lateinit var holeNumber: EditText
    private lateinit var submitScoreButton: Button
    private lateinit var player1Label: TextView
    private lateinit var player2Label: TextView
    private lateinit var player3Label: TextView
    private lateinit var player4Label: TextView
    private lateinit var nextButton: Button

    private val scores = Array(4) { IntArray(18) { 0 } } // Array to store scores for 4 players across 18 holes
    private var currentHole = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scoring)

        // Retrieve data from MainActivity
        val p1Name = intent.getStringExtra("PLAYER_1_NAME") ?: "Player 1"
        val p2Name = intent.getStringExtra("PLAYER_2_NAME") ?: "Player 2"
        val p3Name = intent.getStringExtra("PLAYER_3_NAME") ?: "Player 3"
        val p4Name = intent.getStringExtra("PLAYER_4_NAME") ?: "Player 4"
        val location = intent.getStringExtra("GAME_LOCATION")
        val date = intent.getStringExtra("GAME_DATE")
        val betUnits = intent.getIntExtra("BET_UNITS", 0)

        // Initialize views
        player1Label = findViewById(R.id.player1Label)
        player2Label = findViewById(R.id.player2Label)
        player3Label = findViewById(R.id.player3Label)
        player4Label = findViewById(R.id.player4Label)
        player1Score = findViewById(R.id.player1Score)
        player2Score = findViewById(R.id.player2Score)
        player3Score = findViewById(R.id.player3Score)
        player4Score = findViewById(R.id.player4Score)
        holeNumber = findViewById(R.id.holeNumber)
        submitScoreButton = findViewById(R.id.submitScoreButton)
        nextButton = findViewById(R.id.nextButton)

        // Set player names
        player1Label.text = p1Name
        player2Label.text = p2Name
        player3Label.text = p3Name
        player4Label.text = p4Name

        // Set initial hole number
        holeNumber.setText(currentHole.toString())

        // Submit score button logic
        submitScoreButton.setOnClickListener {
            val holeInput = holeNumber.text.toString().toIntOrNull()
            if (holeInput == null || holeInput !in 1..18) {
                Toast.makeText(this, "Please enter a valid hole number (1-18)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentHole = holeInput

            val scoresValid = listOf(
                player1Score.text.toString().toIntOrNull(),
                player2Score.text.toString().toIntOrNull(),
                player3Score.text.toString().toIntOrNull(),
                player4Score.text.toString().toIntOrNull()
            ).all { it != null && it >= 0 }

            if (!scoresValid) {
                Toast.makeText(this, "Please enter valid scores for all players", Toast.LENGTH_SHORT).show()
            } else {
                scores[0][currentHole - 1] = player1Score.text.toString().toInt()
                scores[1][currentHole - 1] = player2Score.text.toString().toInt()
                scores[2][currentHole - 1] = player3Score.text.toString().toInt()
                scores[3][currentHole - 1] = player4Score.text.toString().toInt()

                Toast.makeText(this, "Scores saved for Hole $currentHole", Toast.LENGTH_SHORT).show()

                // Clear fields for next hole
                player1Score.text.clear()
                player2Score.text.clear()
                player3Score.text.clear()
                player4Score.text.clear()
                if (currentHole < 18) {
                    currentHole++
                    holeNumber.setText(currentHole.toString())
                }
            }
        }

        // Next button logic (placeholder until ResultsActivity is implemented)
        nextButton.setOnClickListener {
            if (scores.all { playerScores -> playerScores.all { it > 0 } }) {
                // Placeholder for navigation to ResultsActivity
                Toast.makeText(this, "All scores entered, ready for results (ResultsActivity not yet implemented)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter scores for all 18 holes", Toast.LENGTH_SHORT).show()
            }
        }
    }
}