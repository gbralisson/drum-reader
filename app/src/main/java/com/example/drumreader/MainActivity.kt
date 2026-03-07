package com.example.drumreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.drumreader.midi.MidiConnectionManager
import com.example.drumreader.model.ConnectionStatus
import com.example.drumreader.repository.ConnectionRepositoryImpl
import com.example.drumreader.repository.DrumRepositoryImpl
import com.example.drumreader.ui.components.DrumStaff
import com.example.drumreader.ui.components.StaffNote
import com.example.drumreader.ui.theme.DrumReaderTheme
import com.example.drumreader.utils.DrumSoundPlayer
import com.example.drumreader.utils.MusicXmlConverter
import com.example.drumreader.viewmodel.ConnectionViewModel
import com.example.drumreader.viewmodel.DrumViewModel
import com.example.drumreader.viewmodel.DrumViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var factory: DrumViewModelFactory
    private lateinit var connectionViewModel: ConnectionViewModel
    private lateinit var drumViewModel: DrumViewModel
    private lateinit var soundPlayer: DrumSoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val xmlSample = """
            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <score-partwise version="3.1">
              <part-list>
                <score-part id="P1"><part-name>Drums</part-name></score-part>
              </part-list>
              <part id="P1">
                <measure number="1">
                  <attributes>
                    <divisions>2</divisions>
                    <key><fifths>0</fifths></key>
                    <time><beats>4</beats><beat-type>4</beat-type></time>
                    <clef><sign>percussion</sign><line>2</line></clef>
                  </attributes>
                  <!-- Measure 1: Basic Rock Beat (Kick, Snare, Hi-Hat) -->
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><chord/><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><chord/><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                </measure>
                <measure number="2">
                  <!-- Measure 2: Ride Cymbal Pattern (F5) -->
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><chord/><unpitched><display-step>F</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>F</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><chord/><unpitched><display-step>F</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>F</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                </measure>
                <measure number="3">
                  <!-- Measure 3: Crash Cymbal on beat 1 (A5) -->
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><chord/><unpitched><display-step>A</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><chord/><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                </measure>
                <measure number="4">
                  <!-- Measure 4: Tom Fill (E5, D5, A4) -->
                  <note><unpitched><display-step>E</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>D</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>A</display-step><display-octave>4</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                </measure>
                <measure number="5">
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                </measure>
                <measure number="6">
                  <note><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>G</display-step><display-octave>5</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>4</duration><type>half</type></note>
                  <note><chord/><unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched><duration>4</duration><type>half</type></note>
                </measure>
                <measure number="7">
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>1</duration><type>eighth</type></note>
                  <note><unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                  <note><unpitched><display-step>C</display-step><display-octave>5</display-octave></unpitched><duration>2</duration><type>quarter</type></note>
                </measure>
                <measure number="8">
                  <note><unpitched><display-step>A</display-step><display-octave>5</display-octave></unpitched><duration>8</duration><type>whole</type></note>
                  <note><chord/><unpitched><display-step>F</display-step><display-octave>4</display-octave></unpitched><duration>8</duration><type>whole</type></note>
                </measure>
              </part>
            </score-partwise>
        """.trimIndent()

        MusicXmlConverter.convertToJson(xmlSample)
        val staffNotes = MusicXmlConverter.parseJsonToStaffNotes()

        val midiConnectionManager = MidiConnectionManager(this)
        factory = DrumViewModelFactory(
            ConnectionRepositoryImpl(midiConnectionManager),
            DrumRepositoryImpl(midiConnectionManager)
        )
        connectionViewModel = ViewModelProvider(this, factory)[ConnectionViewModel::class.java]
        drumViewModel = ViewModelProvider(this, factory)[DrumViewModel::class.java]
        
        soundPlayer = DrumSoundPlayer(this)
        drumViewModel.initPlayback(staffNotes,
            MusicXmlConverter.lastMeasureCount.toFloat(),
            soundPlayer)

        enableEdgeToEdge()
        setContent {
            DrumReaderTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White
                ) { innerPadding ->
                    ConnectionScreen(
                        connectionViewModel = connectionViewModel,
                        drumViewModel = drumViewModel,
                        initialStaffNotes = staffNotes,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) connectionViewModel.disconnect()
    }
}

@Composable
fun ConnectionScreen(
    connectionViewModel: ConnectionViewModel,
    drumViewModel: DrumViewModel,
    initialStaffNotes: List<StaffNote>,
    modifier: Modifier = Modifier
) {
    val connectionStatus by connectionViewModel.connectionStatus.collectAsState()
    val debugMessages by connectionViewModel.debugMessages.collectAsState()
    val midiMessages by drumViewModel.midiMessages.collectAsState()
    val currentMeasure by drumViewModel.currentMeasure.collectAsState()
    val isPlaying by drumViewModel.isPlaying.collectAsState()
    val isSynced by drumViewModel.isSynced.collectAsState()

    ConnectionScreenContent(
        connectionStatus = connectionStatus,
        debugMessages = debugMessages,
        midiMessages = midiMessages,
        staffNotes = initialStaffNotes,
        currentMeasure = currentMeasure,
        isPlaying = isPlaying,
        isSynced = isSynced,
        onConnect = { deviceName -> connectionViewModel.connect(deviceName) },
        onTogglePlayback = { drumViewModel.togglePlayback() },
        onResetPlayback = { drumViewModel.resetPlayback() },
        modifier = modifier
    )
}

@Composable
fun ConnectionScreenContent(
    connectionStatus: ConnectionStatus,
    debugMessages: List<String>,
    midiMessages: List<String>,
    staffNotes: List<StaffNote>,
    currentMeasure: Float?,
    isPlaying: Boolean,
    isSynced: Boolean,
    onConnect: (String) -> Unit,
    onTogglePlayback: () -> Unit,
    onResetPlayback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = when (connectionStatus) {
                    is ConnectionStatus.Connected -> "Connected to ${connectionStatus.deviceName}"
                    is ConnectionStatus.Connecting -> "Connecting..."
                    is ConnectionStatus.Disconnected -> "Disconnected"
                    is ConnectionStatus.Error -> connectionStatus.message
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onTogglePlayback) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isPlaying) "Pause" else "Play")
                }
                Spacer(Modifier.width(16.dp))
                Button(onClick = onResetPlayback) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = "Reset")
                    Spacer(Modifier.width(8.dp))
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Drum Notation", style = MaterialTheme.typography.labelLarge)
            DrumStaff(
                notes = staffNotes,
                measuresPerLine = 2,
                currentMeasure = currentMeasure,
                isSynced = isSynced,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            //Spacer(modifier = Modifier.height(24.dp))
            //LogBox(title = "Connectivity Debug Log:", messages = debugMessages, modifier = Modifier.height(150.dp))
            
            //Spacer(modifier = Modifier.height(16.dp))
            //LogBox(title = "MIDI Messages:", messages = midiMessages, modifier = Modifier.height(250.dp))
        }

        IconButton(
            onClick = { onConnect("Alesis Nitro") },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            val iconColor = when (connectionStatus) {
                is ConnectionStatus.Connected -> Color.Green
                is ConnectionStatus.Connecting -> Color(0xFFFFA500) // Orange
                is ConnectionStatus.Disconnected -> Color.Gray
                is ConnectionStatus.Error -> Color.Red
            }
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = "Connect to MIDI Device",
                tint = iconColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectionScreenPreview() {
    DrumReaderTheme {
        ConnectionScreenContent(
            connectionStatus = ConnectionStatus.Disconnected,
            debugMessages = listOf(
                "Scanning for MIDI devices...",
                "Found 1 MIDI device",
                "Found Alesis device: Alesis Nitro",
                "MIDI device opened successfully"
            ),
            midiMessages = listOf(
                "Hit: Kick (Vel: 100)",
                "Hit: Snare (Vel: 80)"
            ),
            staffNotes = listOf(
                StaffNote("F", 4, 1, 0.0f),
                StaffNote("C", 5, 1, 0.25f),
                StaffNote("F", 4, 1, 0.5f),
                StaffNote("C", 5, 1, 0.75f)
            ),
            currentMeasure = 0.5f,
            isPlaying = false,
            isSynced = false,
            onConnect = {},
            onTogglePlayback = {},
            onResetPlayback = {}
        )
    }
}
