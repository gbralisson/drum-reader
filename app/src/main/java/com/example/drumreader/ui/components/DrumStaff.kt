package com.example.drumreader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.floor

data class StaffNote(
    val step: String,
    val octave: Int,
    val duration: Int,
    val xOffset: Float // Absolute position in measures (e.g., 1.5 is middle of 2nd measure)
)

@Composable
fun DrumStaff(
    notes: List<StaffNote>,
    measuresPerLine: Int = 2,
    currentMeasure: Float? = null,
    isSynced: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (notes.isEmpty()) return

    val notesByLine = notes.groupBy { floor(it.xOffset / measuresPerLine).toInt() }
    val maxLineIndex = notesByLine.keys.maxOrNull() ?: 0

    Column(modifier = modifier) {
        for (i in 0..maxLineIndex) {
            val lineNotes = notesByLine[i] ?: emptyList()
            StaffLine(
                notes = lineNotes,
                lineOffset = i * measuresPerLine.toFloat(),
                measuresPerLine = measuresPerLine,
                currentMeasure = currentMeasure,
                isSynced = isSynced,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            if (i < maxLineIndex) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StaffLine(
    notes: List<StaffNote>,
    lineOffset: Float,
    measuresPerLine: Int,
    currentMeasure: Float? = null,
    isSynced: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp)
    ) {
        StaffBackground(
            notes = notes,
            lineOffset = lineOffset,
            measuresPerLine = measuresPerLine,
            modifier = Modifier.fillMaxSize()
        )
        TrackerOverlay(
            currentMeasure = currentMeasure,
            lineOffset = lineOffset,
            measuresPerLine = measuresPerLine,
            isSynced = isSynced,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun StaffBackground(
    notes: List<StaffNote>,
    lineOffset: Float,
    measuresPerLine: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = height / 10f
        val middleY = height / 2f
        val lineSpacing = spacing * 2

        // Draw 5 staff lines
        for (i in -2..2) {
            val y = middleY - (i * lineSpacing)
            drawLine(
                color = Color.Black,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw measure bars
        for (m in 0..measuresPerLine) {
            val x = (m.toFloat() / measuresPerLine) * width
            drawLine(
                color = Color.Black,
                start = Offset(x, middleY - 2 * lineSpacing),
                end = Offset(x, middleY + 2 * lineSpacing),
                strokeWidth = 1.dp.toPx()
            )
        }

        fun getYForIndex(index: Int): Float {
            return middleY - (index - 4) * spacing
        }

        notes.forEach { note ->
            val staffIndex = getStaffIndex(note.step, note.octave)
            val noteY = getYForIndex(staffIndex)
            // Relative X within this line
            val relativeX = (note.xOffset - lineOffset) / measuresPerLine
            val noteX = relativeX * width

            // Draw ledger line for Crash (A5 - index 10)
            if (staffIndex >= 10) {
                // Draw a short ledger line at index 10
                val ledgerY = getYForIndex(10)
                drawLine(
                    color = Color.Black,
                    start = Offset(noteX - spacing * 1.2f, ledgerY),
                    end = Offset(noteX + spacing * 1.2f, ledgerY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            drawCircle(
                color = Color.Black,
                radius = spacing * 0.8f,
                center = Offset(noteX, noteY)
            )

            val stemHeight = spacing * 6
            val isUp = staffIndex < 4
            val stemEnd = if (isUp) noteY - stemHeight else noteY + stemHeight

            drawLine(
                color = Color.Black,
                start = Offset(noteX + spacing * 0.8f, noteY),
                end = Offset(noteX + spacing * 0.8f, stemEnd),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
fun TrackerOverlay(
    currentMeasure: Float?,
    lineOffset: Float,
    measuresPerLine: Int,
    isSynced: Boolean = false,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = height / 10f
        val middleY = height / 2f
        val lineSpacing = spacing * 2

        currentMeasure?.let { measure ->
            if (measure >= lineOffset && measure < lineOffset + measuresPerLine) {
                val relativeX = (measure - lineOffset) / measuresPerLine
                val trackerX = relativeX * width
                drawLine(
                    color = if (isSynced) Color.Green else Color.Red,
                    start = Offset(trackerX, middleY - 3 * lineSpacing),
                    end = Offset(trackerX, middleY + 3 * lineSpacing),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

private fun getStaffIndex(step: String, octave: Int): Int {
    val base = when (step.uppercase()) {
        "C" -> 0
        "D" -> 1
        "E" -> 2
        "F" -> 3
        "G" -> 4
        "A" -> 5
        "B" -> 6
        else -> 0
    }
    return (octave - 4) * 7 + base - 2
}
