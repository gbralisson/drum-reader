package com.example.drumreader.model

/**
 * Alesis Drum Kit MIDI mapping configuration.
 * This class provides standard MIDI note mappings for Alesis electronic drum kits
 * (like the Nitro, Surge, or Strike Series).
 */
sealed class AlesisDrumKit(val midiNote: Int, val name: String) {
    // Pads
    object SnareCenter : AlesisDrumKit(38, "Snare Center")
    object SnareRim : AlesisDrumKit(40, "Snare Rim")

    object Tom1Center : AlesisDrumKit(48, "Tom 1 Center")
    object Tom1Rim : AlesisDrumKit(50, "Tom 1 Rim")

    object Tom2Center : AlesisDrumKit(45, "Tom 2 Center")
    object Tom2Rim : AlesisDrumKit(47, "Tom 2 Rim")

    object Tom3Center : AlesisDrumKit(43, "Tom 3 Center")
    object Tom3Rim : AlesisDrumKit(58, "Tom 3 Rim")

    object Tom4Center : AlesisDrumKit(41, "Tom 4 Center")

    object Kick : AlesisDrumKit(36, "Kick Drum")

    // Cymbals
    object HiHatOpen : AlesisDrumKit(46, "Hi-Hat Open")
    object HiHatClosed : AlesisDrumKit(42, "Hi-Hat Closed")
    object HiHatPedal : AlesisDrumKit(44, "Hi-Hat Pedal")
    object HiHatSplash : AlesisDrumKit(21, "Hi-Hat Splash")

    object Crash1Center : AlesisDrumKit(49, "Crash 1")
    object Crash1Edge : AlesisDrumKit(55, "Crash 1 Edge")

    object Crash2Center : AlesisDrumKit(57, "Crash 2")
    object China : AlesisDrumKit(52, "Crash 2 Edge")

    object RideCenter : AlesisDrumKit(51, "Ride Bow")
    object RideBell : AlesisDrumKit(53, "Ride Bell")
    object RideEdge : AlesisDrumKit(59, "Ride Edge")

    companion object {
        /**
         * Returns the drum component name based on the MIDI note received.
         */
        fun fromMidiNote(note: Int): String {
            return when (note) {
                36 -> Kick.name
                38 -> SnareCenter.name
                40 -> SnareRim.name
                42 -> HiHatClosed.name
                44 -> HiHatPedal.name
                46 -> HiHatOpen.name
                48 -> Tom1Center.name
                50 -> Tom1Rim.name
                45 -> Tom2Center.name
                47 -> Tom2Rim.name
                43 -> Tom3Center.name
                58 -> Tom3Rim.name
                41 -> Tom4Center.name
                49 -> Crash1Center.name
                55 -> Crash1Edge.name
                57 -> Crash2Center.name
                52 -> China.name
                51 -> RideCenter.name
                53 -> RideBell.name
                59 -> RideEdge.name
                21 -> HiHatSplash.name
                else -> "Unknown ($note)"
            }
        }
    }
}