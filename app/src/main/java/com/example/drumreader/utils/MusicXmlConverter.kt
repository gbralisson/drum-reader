package com.example.drumreader.utils

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

    /**
     * Converts a MusicXML string into a JSON string.
     *
     * @param musicXml The MusicXML content as a String.
     * @return The converted JSON as a String.
     */
    fun convertToJson(musicXml: String): String {
        return try {
            val node: JsonNode = xmlMapper.readTree(musicXml)
            jsonMapper.writeValueAsString(node)
        } catch (e: Exception) {
            e.printStackTrace()
            "{ \"error\": \"Conversion failed: ${e.message}\" }"
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
}