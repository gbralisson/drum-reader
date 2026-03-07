# Drumreader

**Drumreader** is a specialized Android application designed for drummers and electronic percussion enthusiasts. It provides a real-time bridge between MIDI-capable drum kits (specifically optimized for **Alesis** hardware) and mobile devices, enabling performance monitoring and data conversion.

## Key Features
*   **Real-time MIDI Connectivity:** Seamlessly scans and connects to electronic drum kits via the Android MIDI API.
*   **Drum Hit Processing:** Intelligently parses MIDI Note On messages to identify specific kit components (Kick, Snare, Toms, etc.) along with their strike velocity.
*   **Dynamic Playback Tracker:** A reactive visual indicator that moves across the drum staff in real-time. It dynamically changes color from **red to green** when a hit is correctly synchronized with the notation, providing immediate performance feedback.
*   **Intelligent Sync Engine:** Timing logic that verifies hits against expected note positions. It supports **chord synchronization**, requiring all simultaneous notes (e.g., Kick and Hi-Hat) to be played within a precise window for a successful sync.
*   **Synchronized Audio Engine:** Integrated `SoundPool` playback that triggers drum samples (or synthesized beeps as fallback) exactly when the tracker crosses note positions.
*   **MusicXML to JSON Conversion:** Includes a high-performance conversion utility powered by **Jackson** and **Aalto XML**, capable of transforming standard MusicXML notation into structured JSON data.
*   **Modern Android Architecture:** Built using **Jetpack Compose** for a reactive UI and following **MVVM** and **Repository** patterns.

## Standard Drum Notation Mapping
The application follows standard percussion clef mapping for rendering MusicXML on a 5-line staff. Each drum component is mapped to a specific `display-step` and `display-octave`:

| Drum Component | MusicXML Step | Octave | Staff Position |
| :--- | :--- | :--- | :--- |
| **Crash Cymbal** | A | 5 | Above the staff (Ledger line) |
| **Hi-Hat (Closed/Open)** | G | 5 | Above the top line (Space) |
| **Ride Cymbal** | F | 5 | Top line |
| **High Tom** | E | 5 | 4th space |
| **Mid Tom** | D | 5 | 4th line |
| **Snare Drum** | C | 5 | 3rd space |
| **Low / Floor Tom** | A | 4 | 2nd space |
| **Kick Drum** | F | 4 | 1st space |

### Notation Logic
*   **The Staff:** Refers to the set of 5 horizontal lines and the 4 spaces between them (counted from bottom to top).
*   **Vertical Alignment:** Notes marked as chords in MusicXML are rendered at the same horizontal position, representing simultaneous hits.
*   **Stems:** The application dynamically adjusts stem direction based on the staff position (typically stems up for cymbals/snare and stems down for kick/floor toms).
*   **Multi-line Staff:** The notation automatically wraps to a new line every 2 measures to ensure readability on mobile screens.

## Technical Stack
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Audio/Playback:** SoundPool, ToneGenerator, Coroutines (for timing)
*   **MIDI API:** Android MIDI Service
*   **Data Processing:** Jackson Dataformat XML, Aalto XML (StAX)
*   **Architecture:** MVVM + Repository
