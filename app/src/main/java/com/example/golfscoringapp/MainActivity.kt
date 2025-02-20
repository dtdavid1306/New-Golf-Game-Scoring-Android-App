package com.example.golfscoringapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    // Declare views here to use them throughout the class
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
            // Here you would typically validate inputs, save data, and navigate to the next screen.
            // For now, we'll just show a toast to indicate that the button is working.

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
                Toast.makeText(this, "Details Saved! Proceeding to Next Step...", Toast.LENGTH_SHORT).show()
                // Here you would typically start the next activity for scoring or save data to a database
            }
        }
    }
}