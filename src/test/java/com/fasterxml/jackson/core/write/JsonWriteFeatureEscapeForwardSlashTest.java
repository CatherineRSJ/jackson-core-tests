package com.fasterxml.jackson.core.write;

import java.io.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since 2.17
 */
class JsonWriteFeatureEscapeForwardSlashTest
{
    @Test
    void dontEscapeForwardSlash() throws Exception {
        final JsonFactory jsonF = JsonFactory.builder()
                .disable(JsonWriteFeature.ESCAPE_FORWARD_SLASHES)
                .build();
        final String expJson = "{\"url\":\"http://example.com\"}";

        _testWithStringWriter(jsonF, expJson);
        _testWithByteArrayOutputStream(jsonF, expJson); // Also test with byte-backed output
    }

    @Test
    void escapeForwardSlash() throws Exception {
        final JsonFactory jsonF = JsonFactory.builder()
                .enable(JsonWriteFeature.ESCAPE_FORWARD_SLASHES)
                .build();
        final String expJson = "{\"url\":\"http:\\/\\/example.com\"}";

        _testWithStringWriter(jsonF, expJson);
        _testWithByteArrayOutputStream(jsonF, expJson); // Also test with byte-backed output
    }

    private void _testWithStringWriter(JsonFactory jsonF, String expJson) throws Exception {
        // Given
        Writer jsonWriter = new StringWriter();
        // When
        try (JsonGenerator generator = jsonF.createGenerator(jsonWriter)) {
            _writeDoc(generator);
        }
        // Then
        assertEquals(expJson, jsonWriter.toString());
    }

    private void _testWithByteArrayOutputStream(JsonFactory jsonF, String expJson) throws Exception {
        // Given
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // When
        try (JsonGenerator generator = jsonF.createGenerator(bytes)) {
            _writeDoc(generator);
        }
        // Then
        assertEquals(expJson, bytes.toString());
    }

    private void _writeDoc(JsonGenerator generator) throws Exception
    {
        generator.writeStartObject(); // start object
        generator.writeStringField("url", "http://example.com");
        generator.writeEndObject(); // end object
    }

    //Ajout de deux tests TP1-3913

    @Test //L'intention de ce test est de vérifier que la collecte des états par défauts des fonctionnalités est le bon (avec le bitmask)
    void testCollectDefaults() {
        //Arrange: exceptionnellement, il n'y a pas de setup spécial ou spécifique puisqu'on teste une méthode statique qui se concentre sur des fonctionnalités
        //prédéfinies dans le "enum" (fichier JsonWriteFeature). 
    
        //Act
        int defaultFlags = JsonWriteFeature.collectDefaults();
    
        //Assert
        int expectedFlags = 19;
        assertEquals(expectedFlags, defaultFlags, "The collectDefaults method should return the correct default flags.");
    }

    @Test //L'intention de ce test est de vérifier l'activation (enabled) d'une fonctionnalité donnée est bel et bien présente.
    void testEnabledIn() {
        //Arrange
        JsonWriteFeature feature = JsonWriteFeature.ESCAPE_FORWARD_SLASHES;
        int flagsWithFeatureEnabled = feature.getMask();  //Retourner le mask associée à la fonctionnalité
        int flagsWithoutFeatureEnabled = 0;  

        //Act
        boolean isEnabled = feature.enabledIn(flagsWithFeatureEnabled);
        boolean isDisabled = feature.enabledIn(flagsWithoutFeatureEnabled);

        //Assert
        assertTrue(isEnabled, "Feature should be enabled when the corresponding flag is set.");
        assertFalse(isDisabled, "Feature should be disabled when the flag is not set.");
    }

}
