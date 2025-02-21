package com.example.golfscoringapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var player1Name: EditText
    private lateinit var player2Name: EditText
    private lateinit var player3Name: EditText
    private lateinit var player4Name: EditText
    private lateinit var gameLocation: EditText
    private lateinit var gameDate: EditText
    private lateinit var betUnits: EditText
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_details)

        // Find views by their IDs
        player1Name = findViewById(R.id.player1Name)
        player2Name = findViewById(R.id.player2Name)
        player3Name = findViewById(R.id.player3Name)
        player4Name = findViewById(R.id.player4Name)
        gameLocation = findViewById(R.id.gameLocation)
        gameDate = findViewById(R.id.gameDate)
        betUnits = findViewById(R.id.betUnits)
        nextButton = findViewById(R.id.nextButton)

        // Set up click listener for the next button
        nextButton.setOnClickListener {
            val p1Name = player1Name.text.toString().trim()
            val p2Name = player2Name.text.toString().trim()
            val p3Name = player3Name.text.toString().trim()
            val p4Name = player4Name.text.toString().trim()
            val location = gameLocation.text.toString().trim()
            val date = gameDate.text.toString().trim()
            val units = betUnits.text.toString().trim()

            if (p1Name.isEmpty() || p2Name.isEmpty() || p3Name.isEmpty() || p4Name.isEmpty() || location.isEmpty() || date.isEmpty() || units.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val betUnitsValue = units.toIntOrNull()
                if (betUnitsValue == null || betUnitsValue <= 0) {
                    Toast.makeText(this, "Bet units must be a positive number", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Details Saved! Proceeding to Scoring...", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ScoringActivity::class.java).apply {
                        putExtra("PLAYER_1_NAME", p1Name)
                        putExtra("PLAYER_2_NAME", p2Name)
                        putExtra("PLAYER_3_NAME", p3Name)
                        putExtra("PLAYER_4_NAME", p4Name)
                        putExtra("GAME_LOCATION", location)
                        putExtra("GAME_DATE", date)
                        putExtra("BET_UNITS", betUnitsValue)
                    }
                    startActivity(intent)
                }
            }
        }
    }
}