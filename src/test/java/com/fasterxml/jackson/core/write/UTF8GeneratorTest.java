package com.fasterxml.jackson.core.write;

import java.io.*;

import com.fasterxml.jackson.core.*;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import com.fasterxml.jackson.core.filter.FilteringGeneratorDelegate;
import com.fasterxml.jackson.core.filter.JsonPointerBasedFilter;
import com.fasterxml.jackson.core.filter.TokenFilter.Inclusion;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.UTF8JsonGenerator;

import static org.junit.jupiter.api.Assertions.*;

class UTF8GeneratorTest extends JUnit5TestBase
{
    private final JsonFactory JSON_F = new JsonFactory();

    private final JsonFactory JSON_MAX_NESTING_1 = JsonFactory.builder()
            .streamWriteConstraints(StreamWriteConstraints.builder().maxNestingDepth(1).build())
            .build();

    @Test
    void utf8Issue462() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IOContext ioc = testIOContext();
        JsonGenerator gen = new UTF8JsonGenerator(ioc, 0, null, bytes, '"');
        String str = "Natuurlijk is alles gelukt en weer een tevreden klant\uD83D\uDE04";
        int length = 4000 - 38;

        for (int i = 1; i <= length; ++i) {
            gen.writeNumber(1);
        }
        gen.writeString(str);
        gen.flush();
        gen.close();

        // Also verify it's parsable?
        JsonParser p = JSON_F.createParser(bytes.toByteArray());
        for (int i = 1; i <= length; ++i) {
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
        }
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(str, p.getText());
        assertNull(p.nextToken());
        p.close();
    }

    @Test
    /**
     * Test pour s'assurer que le constructeur de UTF8JsonGenerator avec le buffer a le comportement attendu
     * @throws Exception
     */
    void utf8JsonGeneratorBufferConstructor() throws Exception
    {
        // Arrange
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        IOContext ioc = testIOContext();
        int outputOffset = 0;
        String str = "Test string";
        int length = 10;
        byte[] buffer = ioc.allocWriteEncodingBuffer();

        // Act
        JsonGenerator gen = new UTF8JsonGenerator(ioc, 0, null, bytes, '"', buffer, outputOffset, true);
        for (int i = 1; i <= length; ++i) {
            gen.writeNumber(1);
        }
        gen.writeString(str);
        gen.flush();
        gen.close();
        JsonParser p = JSON_F.createParser(bytes.toByteArray());

        // Assert
        for (int i = 1; i <= length; ++i) {
            assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
        }
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(str, p.getText());
        assertNull(p.nextToken());
        p.close();
        assertInstanceOf(UTF8JsonGenerator.class, gen);
        assertEquals(gen.getOutputBuffered(), outputOffset);
    }

    @Test
    void nestingDepthWithSmallLimit() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (JsonGenerator gen = JSON_MAX_NESTING_1.createGenerator(bytes)) {
            gen.writeStartObject();
            gen.writeFieldName("array");
            gen.writeStartArray();
            fail("expected StreamConstraintsException");
        } catch (StreamConstraintsException sce) {
            String expected = "Document nesting depth (2) exceeds the maximum allowed (1, from `StreamWriteConstraints.getMaxNestingDepth()`)";
            assertEquals(expected, sce.getMessage());
        }
    }

    @Test
    void writeStartObjectWithParameter() throws Exception
    {
        // Arrange
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JsonGenerator gen = JSON_F.createGenerator(bytes);
        Object testObject = new Object();
        String testFieldName = "test_name";
        String testFieldValue = "test_value";

        // Act
        gen.writeStartObject(testObject);
        gen.writeFieldName(testFieldName);
        gen.writeString(testFieldValue);
        gen.writeEndObject();
        gen.flush();
        gen.close();

        // Assert
        JsonParser p = JSON_F.createParser(bytes.toByteArray());
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(testFieldName, p.getText());
        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(testFieldValue, p.getText());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    void writeStartArrayWithParameter() throws Exception
    {
        // Arrange
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JsonGenerator gen = JSON_F.createGenerator(bytes);
        Object testObject = new Object();
        String testFieldName = "array";
        int intValue = 2;

        // Act
        gen.writeStartObject();
        gen.writeFieldName(testFieldName);
        gen.writeStartArray(testObject);
        gen.writeNumber(intValue);
        gen.writeEndArray();
        gen.writeEndObject();
        gen.flush();
        gen.close();

        // Assert
        JsonParser p = JSON_F.createParser(bytes.toByteArray());
        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(testFieldName, p.getText());
        assertToken(JsonToken.START_ARRAY, p.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(intValue, p.getIntValue());
        assertToken(JsonToken.END_ARRAY, p.nextToken());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    void writeRawTest() throws Exception
    {
        // Arrange
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JsonGenerator gen = JSON_F.createGenerator(bytes);
        String test = "ÿ";

        // Act
        gen.writeRaw(test.charAt(0));
        gen.flush();
        gen.close();

        // Assert
        JsonParser p = JSON_F.createParser(bytes.toByteArray());
        assertEquals(null, p.getText());
    }

    @Test
    void writeRawArrayTest() throws Exception
    {
        // Arrange
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        JsonGenerator gen = JSON_F.createGenerator(bytes);
        String test = "test special character ÿ";

        // Act
        gen.writeRaw(test.toCharArray(), 0, 24);
        gen.flush();
        gen.close();

        // Assert
        JsonParser p = JSON_F.createParser(bytes.toByteArray());
        assertEquals(null, p.getText());
    }

    @Test
    void nestingDepthWithSmallLimitNestedObject() throws Exception
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (JsonGenerator gen = JSON_MAX_NESTING_1.createGenerator(bytes)) {
            gen.writeStartObject();
            gen.writeFieldName("object");
            gen.writeStartObject();
            fail("expected StreamConstraintsException");
        } catch (StreamConstraintsException sce) {
            String expected = "Document nesting depth (2) exceeds the maximum allowed (1, from `StreamWriteConstraints.getMaxNestingDepth()`)";
            assertEquals(expected, sce.getMessage());
        }
    }

    // for [core#115]
    @Test
    void surrogatesWithRaw() throws Exception
    {
        final String VALUE = q("\ud83d\ude0c");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator g = JSON_F.createGenerator(out);
        g.writeStartArray();
        g.writeRaw(VALUE);
        g.writeEndArray();
        g.close();

        final byte[] JSON = out.toByteArray();

        JsonParser jp = JSON_F.createParser(JSON);
        assertToken(JsonToken.START_ARRAY, jp.nextToken());
        assertToken(JsonToken.VALUE_STRING, jp.nextToken());
        String str = jp.getText();
        assertEquals(2, str.length());
        assertEquals((char) 0xD83D, str.charAt(0));
        assertEquals((char) 0xDE0C, str.charAt(1));
        assertToken(JsonToken.END_ARRAY, jp.nextToken());
        jp.close();
    }

    @Test
    void filteringWithEscapedChars() throws Exception
    {
        final String SAMPLE_WITH_QUOTES = "\b\t\f\n\r\"foo\"\u0000";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        @SuppressWarnings("resource")
        JsonGenerator g = JSON_F.createGenerator(out);

        FilteringGeneratorDelegate gen = new FilteringGeneratorDelegate(g,
                new JsonPointerBasedFilter("/escapes"),
                Inclusion.INCLUDE_ALL_AND_PATH,
                false // multipleMatches
        );

        //final String JSON = "{'a':123,'array':[1,2],'escapes':'\b\t\f\n\r\"foo\"\u0000'}";

        gen.writeStartObject();

        gen.writeFieldName("a");
        gen.writeNumber(123);

        gen.writeFieldName("array");
        gen.writeStartArray();
        gen.writeNumber((short) 1);
        gen.writeNumber((short) 2);
        gen.writeEndArray();

        gen.writeFieldName("escapes");

        final byte[] raw = utf8Bytes(SAMPLE_WITH_QUOTES);
        gen.writeUTF8String(raw, 0, raw.length);

        gen.writeEndObject();
        gen.close();

        JsonParser p = JSON_F.createParser(out.toByteArray());

        assertToken(JsonToken.START_OBJECT, p.nextToken());
        assertToken(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("escapes", p.currentName());

        assertToken(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(SAMPLE_WITH_QUOTES, p.getText());
        assertToken(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }
}
