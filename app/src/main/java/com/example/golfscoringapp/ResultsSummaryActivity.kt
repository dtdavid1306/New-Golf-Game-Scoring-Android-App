package com.example.golfscoringapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import android.widget.LinearLayout
import android.widget.TextView
import android.content.SharedPreferences
import android.widget.Toast
import com.example.golfscoringapp.GameDetailsActivity // Added import
import com.example.golfscoringapp.ResultsActivity // Added import

// Move HoleResult to a separate file or keep it here if it’s the source
data class HoleResult(
    val holeNumber: Int,
    val player1Score: Int,
    val player2Score: Int,
    val player3Score: Int,
    val player4Score: Int,
    val teamWin: String,
    val individualWins: List<String>
)

class ResultsSummaryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var summaryTotals: TextView
    private lateinit var backButton: MaterialButton
    private lateinit var toGameDetailsButton: MaterialButton
    private lateinit var toScoringButton: MaterialButton
    private lateinit var toResultsButton: MaterialButton
    private lateinit var prefs: SharedPreferences
    private val scores = Array(4) { IntArray(18) { 0 } } // Added scores field

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results_summary)

        // Initialize SharedPreferences
        prefs = getSharedPreferences("GolfScoringPrefs", MODE_PRIVATE)

        // Retrieve data from intent or SharedPreferences with null checks
        val p1Name = intent.getStringExtra("PLAYER_1_NAME") ?: prefs.getString("PLAYER_1_NAME", "Player 1") ?: "Player 1"
        val p2Name = intent.getStringExtra("PLAYER_2_NAME") ?: prefs.getString("PLAYER_2_NAME", "Player 2") ?: "Player 2"
        val p3Name = intent.getStringExtra("PLAYER_3_NAME") ?: prefs.getString("PLAYER_3_NAME", "Player 3") ?: "Player 3"
        val p4Name = intent.getStringExtra("PLAYER_4_NAME") ?: prefs.getString("PLAYER_4_NAME", "Player 4") ?: "Player 4"
        // Load scores into the class field with null checks
        for (playerIndex in 0 until 4) {
            scores[playerIndex] = when (playerIndex) {
                0 -> intent.getIntegerArrayListExtra("SCORES_P1")?.toIntArray() ?: loadScores(playerIndex) ?: IntArray(18) { 0 }
                1 -> intent.getIntegerArrayListExtra("SCORES_P2")?.toIntArray() ?: loadScores(playerIndex) ?: IntArray(18) { 0 }
                2 -> intent.getIntegerArrayListExtra("SCORES_P3")?.toIntArray() ?: loadScores(playerIndex) ?: IntArray(18) { 0 }
                3 -> intent.getIntegerArrayListExtra("SCORES_P4")?.toIntArray() ?: loadScores(playerIndex) ?: IntArray(18) { 0 }
                else -> IntArray(18) { 0 }
            }
        }
        val betUnits = intent.getIntExtra("BET_UNITS", prefs.getInt("BET_UNITS", 0))
        val location = intent.getStringExtra("GAME_LOCATION") ?: prefs.getString("GAME_LOCATION", "") ?: ""
        val date = intent.getStringExtra("GAME_DATE") ?: prefs.getString("GAME_DATE", "") ?: ""

        // Initialize views
        recyclerView = findViewById(R.id.results_summary_recycler)
        summaryTotals = findViewById(R.id.summary_totals)
        backButton = findViewById(R.id.back_button)
        toGameDetailsButton = findViewById(R.id.to_game_details_button)
        toScoringButton = findViewById(R.id.to_scoring_button)
        toResultsButton = findViewById(R.id.to_results_button)

        // Calculate results
        val holeResults = (0 until 18).map { hole ->
            val holeScores = intArrayOf(scores[0][hole], scores[1][hole], scores[2][hole], scores[3][hole])
            val minScore = holeScores.minOrNull() ?: 0
            val individualWins = holeScores.indices.map { index ->
                when {
                    holeScores[index] == minScore && holeScores.count { it == minScore } == 1 -> "Win"
                    holeScores[index] > minScore -> "Loss"
                    else -> "Draw"
                }
            }

            val teamWin = determineTeamWin(hole, scores, betUnits)

            HoleResult(hole + 1, holeScores[0], holeScores[1], holeScores[2], holeScores[3], teamWin, individualWins)
        }

        // Calculate totals
        val totalScores = IntArray(4) { i -> scores[i].sum() }
        val individualWinnings = calculateIndividualWinnings(scores, betUnits)
        val teamWinnings = calculateTeamWinnings(scores, betUnits)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ResultsSummaryAdapter(holeResults, p1Name, p2Name, p3Name, p4Name, totalScores, individualWinnings, teamWinnings, location, date, this)
        recyclerView.adapter = adapter

        // Display footer data in a card
        val totalsCard = MaterialCardView(this).apply {
            radius = 8f
            setCardBackgroundColor(resources.getColor(com.google.android.material.R.color.material_grey_50, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            addView(TextView(this@ResultsSummaryActivity).apply {
                val (totalText, winningsText, gameInfoText) = adapter.getFooterData()
                text = "$totalText\n$winningsText\n$gameInfoText"
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body2)
                setPadding(16, 16, 16, 16)
            })
        }
        summaryTotals = totalsCard.findViewById<TextView>(android.R.id.text1) ?: TextView(this).apply { totalsCard.addView(this) }
        (findViewById<LinearLayout>(android.R.id.content))?.addView(totalsCard)

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

        toResultsButton.setOnClickListener {
            startActivity(Intent(this, ResultsActivity::class.java))
        }

        // Handle configuration changes
        if (savedInstanceState != null) {
            for (playerIndex in 0 until 4) {
                scores[playerIndex] = loadScores(playerIndex)
            }
        }
    }

    private fun determineTeamWin(hole: Int, scores: Array<IntArray>, betUnits: Int): String {
        val set = when {
            hole < 6 -> 0
            hole < 12 -> 1
            else -> 2
        }
        val teamPairs = arrayOf(
            intArrayOf(0, 1, 2, 3), // P1&P2 vs P3&P4 (Holes 1-6)
            intArrayOf(0, 2, 1, 3), // P1&P3 vs P2&P4 (Holes 7-12)
            intArrayOf(0, 3, 1, 2)  // P1&P4 vs P2&P3 (Holes 13-18)
        )
        val team1Scores = intArrayOf(scores[teamPairs[set][0]][hole], scores[teamPairs[set][1]][hole])
        val team2Scores = intArrayOf(scores[teamPairs[set][2]][hole], scores[teamPairs[set][3]][hole])
        val team1Min = team1Scores.minOrNull() ?: return "Draw"
        val team2Min = team2Scores.minOrNull() ?: return "Draw"

        return if (team1Min < team2Min) {
            "${teamPairs[set][0] + 1}&${teamPairs[set][1] + 1}" // e.g., "1&2" for P1&P2
        } else if (team2Min < team1Min) {
            "${teamPairs[set][2] + 1}&${teamPairs[set][3] + 1}" // e.g., "3&4" for P3&P4
        } else {
            val team1Second = team1Scores.filter { it != team1Min }.minOrNull() ?: team1Min
            val team2Second = team2Scores.filter { it != team2Min }.minOrNull() ?: team2Min
            if (team1Second < team2Second) {
                "${teamPairs[set][0] + 1}&${teamPairs[set][1] + 1}"
            } else if (team2Second < team1Second) {
                "${teamPairs[set][2] + 1}&${teamPairs[set][3] + 1}"
            } else {
                "Draw"
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

    private fun loadScores(playerIndex: Int): IntArray? {
        try {
            return IntArray(18) { hole ->
                prefs.getInt("SCORE_P${playerIndex + 1}_H$hole", 0)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading scores: ${e.message}", Toast.LENGTH_SHORT).show()
            return null
        }
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
            scores[playerIndex] = loadScores(playerIndex) ?: IntArray(18) { 0 }
        }
    }
}