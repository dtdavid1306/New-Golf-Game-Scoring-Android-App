package com.example.golfscoringapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.widget.LinearLayout
import android.widget.TextView
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.example.golfscoringapp.GameDetailsActivity // Added import
import com.example.golfscoringapp.ResultsActivity // Added import
import com.example.golfscoringapp.ResultsSummaryActivity // Added import

class ResultsActivity : AppCompatActivity() {

    private lateinit var resultsLayout: LinearLayout
    private lateinit var backButton: MaterialButton
    private lateinit var toGameDetailsButton: MaterialButton
    private lateinit var toScoringButton: MaterialButton
    private lateinit var toSummaryButton: MaterialButton
    private lateinit var prefs: SharedPreferences
    private val scores = Array(4) { IntArray(18) { 0 } } // Added scores field

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // Initialize SharedPreferences
        prefs = getSharedPreferences("GolfScoringPrefs", MODE_PRIVATE)

        // Retrieve data from ScoringActivity or SharedPreferences
        val p1Name = intent.getStringExtra("PLAYER_1_NAME") ?: prefs.getString("PLAYER_1_NAME", "Player 1") ?: "Player 1"
        val p2Name = intent.getStringExtra("PLAYER_2_NAME") ?: prefs.getString("PLAYER_2_NAME", "Player 2") ?: "Player 2"
        val p3Name = intent.getStringExtra("PLAYER_3_NAME") ?: prefs.getString("PLAYER_3_NAME", "Player 3") ?: "Player 3"
        val p4Name = intent.getStringExtra("PLAYER_4_NAME") ?: prefs.getString("PLAYER_4_NAME", "Player 4") ?: "Player 4"
        // Load scores into the class field
        for (playerIndex in 0 until 4) {
            scores[playerIndex] = when (playerIndex) {
                0 -> intent.getIntegerArrayListExtra("SCORES_P1")?.toIntArray() ?: loadScores(playerIndex)
                1 -> intent.getIntegerArrayListExtra("SCORES_P2")?.toIntArray() ?: loadScores(playerIndex)
                2 -> intent.getIntegerArrayListExtra("SCORES_P3")?.toIntArray() ?: loadScores(playerIndex)
                3 -> intent.getIntegerArrayListExtra("SCORES_P4")?.toIntArray() ?: loadScores(playerIndex)
                else -> IntArray(18) { 0 }
            }
        }
        val betUnits = intent.getIntExtra("BET_UNITS", prefs.getInt("BET_UNITS", 0))
        val location = intent.getStringExtra("GAME_LOCATION") ?: prefs.getString("GAME_LOCATION", "") ?: ""
        val date = intent.getStringExtra("GAME_DATE") ?: prefs.getString("GAME_DATE", "") ?: ""

        // Initialize views
        resultsLayout = findViewById(R.id.results_layout)
        backButton = findViewById(R.id.back_button)
        toGameDetailsButton = findViewById(R.id.to_game_details_button)
        toScoringButton = findViewById(R.id.to_scoring_button)
        toSummaryButton = findViewById(R.id.to_summary_button)

        // Calculate winnings
        val individualWinnings = calculateIndividualWinnings(scores, betUnits)
        val teamWinnings = calculateTeamWinnings(scores, betUnits)

        // Save winnings
        saveWinnings(individualWinnings, teamWinnings)

        // Log text data for debugging
        Log.d("ResultsActivity", "Game Details Text: Game Details - Location: $location, Date: $date")
        Log.d("ResultsActivity", "Header Text: Results for $p1Name, $p2Name, $p3Name, $p4Name")
        Log.d("ResultsActivity", "Individual Winnings: $individualWinnings")
        Log.d("ResultsActivity", "Team Winnings: $teamWinnings")

        // Display game details in a card
        val gameDetailsCard = MaterialCardView(this).apply {
            radius = 8f
            setCardBackgroundColor(resources.getColor(com.google.android.material.R.color.material_grey_50, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            addView(TextView(this@ResultsActivity).apply {
                text = "Game Details - Location: $location, Date: $date" // Plain text
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
                setPadding(16, 16, 16, 16)
            })
        }
        resultsLayout.addView(gameDetailsCard)

        // Display results in cards
        val headerCard = MaterialCardView(this).apply {
            radius = 8f
            setCardBackgroundColor(resources.getColor(com.google.android.material.R.color.material_grey_50, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            addView(TextView(this@ResultsActivity).apply {
                text = "Results for $p1Name, $p2Name, $p3Name, $p4Name" // Plain text
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Headline)
                setPadding(16, 16, 16, 16)
            })
        }
        resultsLayout.addView(headerCard)

        // Individual Winnings in a card
        val individualCard = MaterialCardView(this).apply {
            radius = 8f
            setCardBackgroundColor(resources.getColor(com.google.android.material.R.color.material_grey_50, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
            addView(TextView(this@ResultsActivity).apply {
                text = "Individual Game Winnings" // Plain text
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
                setPadding(16, 8, 16, 8)
            })
            listOf(p1Name to individualWinnings[0], p2Name to individualWinnings[1], p3Name to individualWinnings[2], p4Name to individualWinnings[3]).forEach { (name, winning) ->
                addView(TextView(this@ResultsActivity).apply {
                    text = "$name: $winning units" // Plain text
                    setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body1)
                    setPadding(16, 4, 16, 4)
                })
            }
        }
        resultsLayout.addView(individualCard)

        // Team Winnings in a card
        val teamCard = MaterialCardView(this).apply {
            radius = 8f
            setCardBackgroundColor(resources.getColor(com.google.android.material.R.color.material_grey_50, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(TextView(this@ResultsActivity).apply {
                text = "Team Game Winnings" // Plain text
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Subhead)
                setPadding(16, 8, 16, 8)
            })
            listOf(p1Name to teamWinnings[0], p2Name to teamWinnings[1], p3Name to teamWinnings[2], p4Name to teamWinnings[3]).forEach { (name, winning) ->
                addView(TextView(this@ResultsActivity).apply {
                    text = "$name: $winning units" // Plain text
                    setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body1)
                    setPadding(16, 4, 16, 4)
                })
            }
        }
        resultsLayout.addView(teamCard)

        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Navigation buttons
        toGameDetailsButton.setOnClickListener {
            startActivity(Intent(this, GameDetailsActivity::class.java))
        }

        toScoringButton.setOnClickListener {
            startActivity(Intent(this, ScoringActivity::class.java))
        }

        toSummaryButton.setOnClickListener {
            try {
                // Verify data before navigation
                if (p1Name.isEmpty() || p2Name.isEmpty() || p3Name.isEmpty() || p4Name.isEmpty()) {
                    Toast.makeText(this, "Player names cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (scores.any { it.any { it <= 0 } }) {
                    Toast.makeText(this, "All scores must be valid before navigating", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (betUnits <= 0) {
                    Toast.makeText(this, "Bet units must be positive", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                startActivity(Intent(this, ResultsSummaryActivity::class.java).apply {
                    Log.d("ResultsActivity", "Navigating to Summary with data: p1Name=$p1Name, p2Name=$p2Name, p3Name=$p3Name, p4Name=$p4Name")
                    Log.d("ResultsActivity", "Scores: P1=${scores[0].contentToString()}, P2=${scores[1].contentToString()}, P3=${scores[2].contentToString()}, P4=${scores[3].contentToString()}")
                    Log.d("ResultsActivity", "Bet Units: $betUnits, Location: $location, Date: $date")
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
            } catch (e: Exception) {
                Log.e("ResultsActivity", "Error navigating to Summary: ${e.message}", e)
                Toast.makeText(this, "Error navigating to Summary: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Handle configuration changes
        if (savedInstanceState != null) {
            for (playerIndex in 0 until 4) {
                scores[playerIndex] = loadScores(playerIndex)
            }
        }
    }

    private fun calculateIndividualWinnings(scores: Array<IntArray>, betUnits: Int): IntArray {
        val winnings = IntArray(4)
        for (hole in 0 until 18) {
            val holeScores = intArrayOf(scores[0][hole], scores[1][hole], scores[2][hole], scores[3][hole])
            val minScore = holeScores.minOrNull() ?: continue
            val winners = holeScores.indices.filter { holeScores[it] == minScore }
            if (winners.size == 1) { // Only one winner
                winnings[winners[0]] += 3 * betUnits
            }
        }
        return winnings
    }

    private fun calculateTeamWinnings(scores: Array<IntArray>, betUnits: Int): IntArray {
        val winnings = IntArray(4)
        val teamPairs = arrayOf(
            intArrayOf(0, 1, 2, 3), // P1&P2 vs P3&P4 (Holes 1-6)
            intArrayOf(0, 2, 1, 3), // P1&P3 vs P2&P4 (Holes 7-12)
            intArrayOf(0, 3, 1, 2)  // P1&P4 vs P2&P3 (Holes 13-18)
        )

        for (set in teamPairs.indices) {
            val startHole = set * 6
            val endHole = startHole + 5
            for (hole in startHole..endHole) {
                val team1Scores = intArrayOf(scores[teamPairs[set][0]][hole], scores[teamPairs[set][1]][hole])
                val team2Scores = intArrayOf(scores[teamPairs[set][2]][hole], scores[teamPairs[set][3]][hole])
                val team1Min = team1Scores.minOrNull() ?: continue
                val team2Min = team2Scores.minOrNull() ?: continue

                if (team1Min < team2Min) {
                    winnings[teamPairs[set][0]] += 2 * betUnits
                    winnings[teamPairs[set][1]] += 2 * betUnits
                } else if (team2Min < team1Min) {
                    winnings[teamPairs[set][2]] += 2 * betUnits
                    winnings[teamPairs[set][3]] += 2 * betUnits
                } else {
                    val team1Second = team1Scores.filter { it != team1Min }.minOrNull() ?: team1Min
                    val team2Second = team2Scores.filter { it != team2Min }.minOrNull() ?: team2Min
                    if (team1Second < team2Second) {
                        winnings[teamPairs[set][0]] += 2 * betUnits
                        winnings[teamPairs[set][1]] += 2 * betUnits
                    } else if (team2Second < team1Second) {
                        winnings[teamPairs[set][2]] += 2 * betUnits
                        winnings[teamPairs[set][3]] += 2 * betUnits
                    }
                }
            }
        }
        return winnings
    }

    private fun loadScores(playerIndex: Int): IntArray {
        try {
            return IntArray(18) { hole ->
                prefs.getInt("SCORE_P${playerIndex + 1}_H$hole", 0)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading scores: ${e.message}", Toast LENGTH_SHORT).show()
            return IntArray(18) { 0 }
        }
    }

    private fun saveWinnings(individualWinnings: IntArray, teamWinnings: IntArray) {
        try {
            with(prefs.edit()) {
                for (player in 0 until 4) {
                    putInt("INDIVIDUAL_WINNINGS_P${player + 1}", individualWinnings[player])
                    putInt("TEAM_WINNINGS_P${player + 1}", teamWinnings[player])
                }
                apply()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving winnings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveWinnings(calculateIndividualWinnings(scores, prefs.getInt("BET_UNITS", 0)), calculateTeamWinnings(scores, prefs.getInt("BET_UNITS", 0)))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        for (playerIndex in 0 until 4) {
            scores[playerIndex] = loadScores(playerIndex)
        }
    }
}