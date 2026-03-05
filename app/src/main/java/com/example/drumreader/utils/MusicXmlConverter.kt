package com.example.drumreader.utils

import com.example.drumreader.ui.components.StaffNote
import com.fasterxml.aalto.stax.InputFactoryImpl
import com.fasterxml.aalto.stax.OutputFactoryImpl
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object MusicXmlConverter {

    private val xmlMapper = XmlMapper(XmlFactory(InputFactoryImpl(), OutputFactoryImpl())).apply {
        registerKotlinModule()
    }

    private val jsonMapper = ObjectMapper().apply {
        registerKotlinModule()
    }

    private var lastConvertedJson: String? = null
    var lastMeasureCount: Int = 0
        private set

    /**
     * Converts a MusicXML string into a JSON string.
     *
     * @param musicXml The MusicXML content as a String.
     * @return The converted JSON as a String.
     */
    fun convertToJson(musicXml: String) {
        try {
            val node: JsonNode = xmlMapper.readTree(musicXml)
            val json = jsonMapper.writeValueAsString(node)
            lastConvertedJson = json
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Converts a MusicXML string into a pretty-printed JSON string.
     */
    fun convertToPrettyJson(musicXml: String): String {
        return try {
            val node: JsonNode = xmlMapper.readTree(musicXml)
            jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)
        } catch (e: Exception) {
            e.printStackTrace()
            "{ \"error\": \"Conversion failed: ${e.message}\" }"
        }
    }

    fun parseJsonToStaffNotes(): List<StaffNote> {
        val json = lastConvertedJson ?: return emptyList()
        val notes = mutableListOf<StaffNote>()
        try {
            val root = jsonMapper.readTree(json)
            val part = root.path("part")
            val measures = if (part.isArray) {
                part.get(0).path("measure")
            } else {
                part.path("measure")
            }

            val measureList = if (measures.isArray) {
                (0 until measures.size()).map { measures.get(it) }
            } else {
                listOf(measures)
            }
            
            lastMeasureCount = measureList.size

            var divisions = 1

            measureList.forEachIndexed { index, measureNode ->
                val newDivisions = measureNode.path("attributes").path("divisions").asInt(0)
                if (newDivisions > 0) divisions = newDivisions

                val noteNode = measureNode.path("note")
                val noteList = if (noteNode.isArray) {
                    (0 until noteNode.size()).map { noteNode.get(it) }
                } else {
                    listOf(noteNode)
                }

                var measureTicks = 0
                noteList.forEach { note ->
                    val isChord = note.has("chord")
                    val unpitched = note.path("unpitched")
                    val step = unpitched.path("display-step").asText()
                    val octave = unpitched.path("display-octave").asInt()
                    val duration = note.path("duration").asInt()

                    val xPos = if (isChord) {
                        (index.toFloat()) + (measureTicks - duration).toFloat() / (divisions * 4)
                    } else {
                        val pos = (index.toFloat()) + measureTicks.toFloat() / (divisions * 4)
                        measureTicks += duration
                        pos
                    }

                    if (step.isNotEmpty()) {
                        notes.add(StaffNote(step, octave, duration, xPos))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return notes
    }
}
