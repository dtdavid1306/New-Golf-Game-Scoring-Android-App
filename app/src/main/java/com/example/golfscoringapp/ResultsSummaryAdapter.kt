package com.example.golfscoringapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.text.HtmlCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class ResultsSummaryAdapter(
    private val holeResults: List<HoleResult>,
    private val p1Name: String,
    private val p2Name: String,
    private val p3Name: String,
    private val p4Name: String,
    private val totalScores: IntArray,
    private val individualWinnings: IntArray,
    private val teamWinnings: IntArray,
    private val location: String,
    private val date: String,
    private val context: Context
) : RecyclerView.Adapter<ResultsSummaryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val holeNumber: TextView = itemView.findViewById(R.id.hole_number)
        val p1Score: TextView = itemView.findViewById(R.id.p1_score)
        val p2Score: TextView = itemView.findViewById(R.id.p2_score)
        val p3Score: TextView = itemView.findViewById(R.id.p3_score)
        val p4Score: TextView = itemView.findViewById(R.id.p4_score)
        val teamWin: TextView = itemView.findViewById(R.id.team_win)
        val individualWins: TextView = itemView.findViewById(R.id.individual_wins)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_results_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = holeResults[position]
        holder.holeNumber.text = "Hole ${result.holeNumber}"
        holder.p1Score.text = result.player1Score.toString()
        holder.p2Score.text = result.player2Score.toString()
        holder.p3Score.text = result.player3Score.toString()
        holder.p4Score.text = result.player4Score.toString()
        holder.teamWin.text = result.teamWin

        // Style team win with color-coded feedback
        holder.teamWin.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body1)
        holder.teamWin.setTextColor(
            when (result.teamWin) {
                "Draw" -> MaterialColors.getColor(holder.teamWin, com.google.android.material.R.attr.colorOnSurface)
                else -> MaterialColors.getColor(holder.teamWin, com.google.android.material.R.attr.colorPrimary)
            }
        )

        // Style individual wins with color-coded HTML feedback
        val winColor = MaterialColors.getColor(holder.individualWins, com.google.android.material.R.attr.colorPrimary) // Green for wins
        val lossColor = MaterialColors.getColor(holder.individualWins, com.google.android.material.R.attr.colorError) // Red for losses
        val drawColor = MaterialColors.getColor(holder.individualWins, com.google.android.material.R.attr.colorOnSurface) // Gray for draws

        val styledWins = result.individualWins.joinToString(", ") { win ->
            when (win) {
                "Win" -> "<font color='#${Integer.toHexString(winColor and 0xFFFFFF)}'>$win</font>"
                "Loss" -> "<font color='#${Integer.toHexString(lossColor and 0xFFFFFF)}'>$win</font>"
                "Draw" -> "<font color='#${Integer.toHexString(drawColor and 0xFFFFFF)}'>$win</font>"
                else -> win
            }
        }
        holder.individualWins.text = HtmlCompat.fromHtml(
            "$p1Name: $styledWins",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        holder.individualWins.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body1)

        // Enhance card styling (if item_results_summary.xml uses MaterialCardView)
        val card = holder.itemView as? MaterialCardView
        card?.let {
            it.radius = 8f
            it.setCardBackgroundColor(context.resources.getColor(com.google.android.material.R.color.material_grey_50, context.theme))
            it.cardElevation = 4f
        }
    }

    override fun getItemCount(): Int = holeResults.size

    fun getFooterData(): Triple<String, String, String> {
        val totalText = "Total Scores - $p1Name: ${totalScores[0]}, $p2Name: ${totalScores[1]}, $p3Name: ${totalScores[2]}, $p4Name: ${totalScores[3]}"
        val winningsText = "Winnings - $p1Name: ${individualWinnings[0] + teamWinnings[0]}, $p2Name: ${individualWinnings[1] + teamWinnings[1]}, $p3Name: ${individualWinnings[2] + teamWinnings[2]}, $p4Name: ${individualWinnings[3] + teamWinnings[3]} units"
        val gameInfoText = "Location: $location, Date: $date"
        return Triple(totalText, winningsText, gameInfoText)
    }
}