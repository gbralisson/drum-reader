package com.example.drumreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.drumreader.midi.MidiConnectionManager
import com.example.drumreader.model.ConnectionStatus
import com.example.drumreader.repository.ConnectionRepositoryImpl
import com.example.drumreader.repository.DrumRepositoryImpl
import com.example.drumreader.ui.theme.DrumReaderTheme
import com.example.drumreader.utils.MusicXmlConverter
import com.example.drumreader.viewmodel.ConnectionViewModel
import com.example.drumreader.viewmodel.DrumViewModel
import com.example.drumreader.viewmodel.DrumViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var factory: DrumViewModelFactory
    private lateinit var connectionViewModel: ConnectionViewModel
    private lateinit var drumViewModel: DrumViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val xmlSample = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<!DOCTYPE score-partwise PUBLIC\n" +
                "    \"-//Recordare//DTD MusicXML 3.1 Partwise//EN\"\n" +
                "    \"http://www.musicxml.org/dtds/partwise.dtd\">\n" +
                "<score-partwise version=\"3.1\">\n" +
                "  <part-list>\n" +
                "    <score-part id=\"P1\">\n" +
                "      <part-name>Drums</part-name>\n" +
                "    </score-part>\n" +
                "  </part-list>\n" +
                "  <part id=\"P1\">\n" +
                "    <measure number=\"1\">\n" +
                "      <attributes>\n" +
                "        <divisions>1</divisions>\n" +
                "        <key>\n" +
                "          <fifths>0</fifths>\n" +
                "        </key>\n" +
                "        <time>\n" +
                "          <beats>4</beats>\n" +
                "          <beat-type>4</beat-type>\n" +
                "        </time>\n" +
                "        <clef>\n" +
                "          <sign>percussion</sign>\n" +
                "          <line>2</line>\n" +
                "        </clef>\n" +
                "      </attributes>\n" +
                "      <!-- Kick Drum on Beat 1 -->\n" +
                "      <note>\n" +
                "        <unpitched>\n" +
                "          <display-step>F</display-step>\n" +
                "          <display-octave>4</display-octave>\n" +
                "        </unpitched>\n" +
                "        <duration>1</duration>\n" +
                "        <instrument id=\"P1-I36\"/>\n" +
                "        <voice>1</voice>\n" +
                "        <type>quarter</type>\n" +
                "        <stem>down</stem>\n" +
                "      </note>\n" +
                "      <!-- Snare Drum on Beat 2 -->\n" +
                "      <note>\n" +
                "        <unpitched>\n" +
                "          <display-step>C</display-step>\n" +
                "          <display-octave>5</display-octave>\n" +
                "        </unpitched>\n" +
                "        <duration>1</duration>\n" +
                "        <instrument id=\"P1-I39\"/>\n" +
                "        <voice>1</voice>\n" +
                "        <type>quarter</type>\n" +
                "        <stem>up</stem>\n" +
                "      </note>\n" +
                "      <!-- Kick Drum on Beat 3 -->\n" +
                "      <note>\n" +
                "        <unpitched>\n" +
                "          <display-step>F</display-step>\n" +
                "          <display-octave>4</display-octave>\n" +
                "        </unpitched>\n" +
                "        <duration>1</duration>\n" +
                "        <instrument id=\"P1-I36\"/>\n" +
                "        <voice>1</voice>\n" +
                "        <type>quarter</type>\n" +
                "        <stem>down</stem>\n" +
                "      </note>\n" +
                "      <!-- Snare Drum on Beat 4 -->\n" +
                "      <note>\n" +
                "        <unpitched>\n" +
                "          <display-step>C</display-step>\n" +
                "          <display-octave>5</display-octave>\n" +
                "        </unpitched>\n" +
                "        <duration>1</duration>\n" +
                "        <instrument id=\"P1-I39\"/>\n" +
                "        <voice>1</voice>\n" +
                "        <type>quarter</type>\n" +
                "        <stem>up</stem>\n" +
                "      </note>\n" +
                "    </measure>\n" +
                "  </part>\n" +
                "</score-partwise>"

        val jsonResult = MusicXmlConverter.convertToPrettyJson(xmlSample)
        println(jsonResult)

        val midiConnectionManager = MidiConnectionManager(this)
        factory = DrumViewModelFactory(
            ConnectionRepositoryImpl(midiConnectionManager),
            DrumRepositoryImpl(midiConnectionManager)
        )
        connectionViewModel = ViewModelProvider(this, factory)[ConnectionViewModel::class.java]
        drumViewModel = ViewModelProvider(this, factory)[DrumViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            DrumReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ConnectionScreen(
                        connectionViewModel = connectionViewModel,
                        drumViewModel = drumViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(isFinishing) connectionViewModel.disconnect()
    }
}

@Composable
fun ConnectionScreen(
    connectionViewModel: ConnectionViewModel,
    drumViewModel: DrumViewModel,
    modifier: Modifier = Modifier
) {
    val connectionStatus by connectionViewModel.connectionStatus.collectAsState()
    val debugMessages by connectionViewModel.debugMessages.collectAsState()
    val midiMessages by drumViewModel.midiMessages.collectAsState()

    ConnectionScreenContent(
        connectionStatus = connectionStatus,
        debugMessages = debugMessages,
        midiMessages = midiMessages,
        onConnect = { deviceName -> connectionViewModel.connect(deviceName) },
        modifier = modifier
    )
}

/**
 * Stateless version of the Connection Screen with debug and midi log displays.
 */
@Composable
fun ConnectionScreenContent(
    connectionStatus: ConnectionStatus,
    debugMessages: List<String>,
    midiMessages: List<String>,
    onConnect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = when (connectionStatus) {
                is ConnectionStatus.Connected -> "Status: Connected to ${connectionStatus.deviceName}"
                is ConnectionStatus.Connecting -> "Status: Connecting..."
                is ConnectionStatus.Disconnected -> "Status: Disconnected"
                is ConnectionStatus.Error -> "Status: Error - ${connectionStatus.message}"
            },
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onConnect("Alesis Nitro") },
            enabled = connectionStatus !is ConnectionStatus.Connecting && connectionStatus !is ConnectionStatus.Connected
        ) {
            Text(text = "Connect to Alesis")
        }

        Spacer(modifier = Modifier.height(24.dp))
        LogBox(title = "Connectivity Debug Log:", messages = debugMessages, modifier = Modifier.weight(0.25f))
        
        Spacer(modifier = Modifier.height(16.dp))
        LogBox(title = "MIDI Messages:", messages = midiMessages, modifier = Modifier.weight(0.75f))
    }
}

@Composable
fun LogBox(title: String, messages: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.05f),
            shape = MaterialTheme.shapes.medium
        ) {
            val listState = rememberLazyListState()

            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages) { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
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
            onConnect = {}
        )
    }
}
