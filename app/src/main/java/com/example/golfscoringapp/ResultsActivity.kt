package com.example.golfscoringapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class ResultsActivity : AppCompatActivity() {

    private lateinit var resultsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        resultsText = findViewById(R.id.resultsText)

        // Retrieve data from ScoringActivity
        val p1Name = intent.getStringExtra("PLAYER_1_NAME") ?: "Player 1"
        val p2Name = intent.getStringExtra("PLAYER_2_NAME") ?: "Player 2"
        val p3Name = intent.getStringExtra("PLAYER_3_NAME") ?: "Player 3"
        val p4Name = intent.getStringExtra("PLAYER_4_NAME") ?: "Player 4"
        val scores = intent.getSerializableExtra("SCORES") as? Array<IntArray> ?: Array(4) { IntArray(18) { 0 } }
        val betUnits = intent.getIntExtra("BET_UNITS", 0)

        // Calculate winnings
        val individualWinnings = calculateIndividualWinnings(scores, betUnits)
        val teamWinnings = calculateTeamWinnings(scores, betUnits)

        // Combine results
        val results = StringBuilder()
        results.append("Results for $p1Name, $p2Name, $p3Name, $p4Name\n\n")
        results.append("Individual Game Winnings:\n")
        results.append("$p1Name: ${individualWinnings[0]} units\n")
        results.append("$p2Name: ${individualWinnings[1]} units\n")
        results.append("$p3Name: ${individualWinnings[2]} units\n")
        results.append("$p4Name: ${individualWinnings[3]} units\n\n")
        results.append("Team Game Winnings:\n")
        results.append("$p1Name: ${teamWinnings[0]} units\n")
        results.append("$p2Name: ${teamWinnings[1]} units\n")
        results.append("$p3Name: ${teamWinnings[2]} units\n")
        results.append("$p4Name: ${teamWinnings[3]} units\n")

        resultsText.text = results.toString()
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
                    val team1Second = team1Scores.maxOrNull() ?: continue
                    val team2Second = team2Scores.maxOrNull() ?: continue
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
}