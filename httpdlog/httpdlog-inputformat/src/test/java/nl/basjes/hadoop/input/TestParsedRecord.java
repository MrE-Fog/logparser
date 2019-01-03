/*
 * Apache HTTPD & NGINX Access log parsing made easy
 * Copyright (C) 2011-2019 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.basjes.hadoop.input;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.Writable;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestParsedRecord {

    // Copied from https://stackoverflow.com/questions/13288214/how-to-unit-test-hadoop-writable
    public static byte[] serialize(Writable writable) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOut = null;
        try {
            dataOut = new DataOutputStream(out);
            writable.write(dataOut);
            return out.toByteArray();
        } finally {
            IOUtils.closeQuietly(dataOut);
        }
    }

    public static <T extends Writable> T asWritable(byte[] bytes, Class<T> clazz) throws IOException, IllegalAccessException, InstantiationException {
        T result;
        DataInputStream dataIn = null;
        try {
            result = clazz.newInstance();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            dataIn = new DataInputStream(in);
            result.readFields(dataIn);
        } finally {
            IOUtils.closeQuietly(dataIn);
        }
        return result;
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "ObjectEqualsNull", "EqualsWithItself"})
    @Test
    public void testParsedRecordSerialization() throws IOException, InstantiationException, IllegalAccessException {
        ParsedRecord record = new ParsedRecord();

        // Set and verify
        setAllValues(record);
        checkAllValues(record);

        byte[] serializedBytes = serialize(record);
        ParsedRecord deserialized = asWritable(serializedBytes, ParsedRecord.class);

        // Compare both before and after records
        checkAllValues(record);
        checkAllValues(deserialized);
        assertTrue("Equals failed!", record.equals(deserialized));
        assertTrue("Equals failed!", record.equals(record));
        assertFalse("Equals failed!", record.equals(null));
        assertFalse("Equals failed!", record.equals(this));
        assertEquals("Hashcode is different!", record.hashCode(), deserialized.hashCode());
        record.clear();
    }

    private void setAllValues(ParsedRecord record) {
        record.set("String A", "42");
        record.set("String B", "42");
        record.set("String C", "42");
        record.set("String D", "42");

        record.set("Long A", 42L);
        record.set("Long B", 42L);
        record.set("Long C", 42L);
        record.set("Long D", 42L);

        record.set("Double A", 42D);
        record.set("Double B", 42D);
        record.set("Double C", 42D);
        record.set("Double D", 42D);

        record.declareRequestedFieldname("Multi_A.*");
        record.setMultiValueString("Multi_A.1", "Foo");
        record.setMultiValueString("Multi_A.2", "Bar");

        record.declareRequestedFieldname("Multi_B.*");
        record.setMultiValueString("Multi_B.1", "Foo");
        record.setMultiValueString("Multi_B.2", "Bar");

        record.declareRequestedFieldname("Multi_C.*");
        record.setMultiValueString("Multi_C.1", "Foo");
        record.setMultiValueString("Multi_C.2", "Bar");
    }

    private void checkAllValues(ParsedRecord record) {
        assertEquals("String A", "42",          record.getString("String A"));
        assertEquals("String B", "42",          record.getString("String B"));
        assertEquals("String C", "42",          record.getString("String C"));
        assertEquals("String D", "42",          record.getString("String D"));
        assertEquals("Long A",   (Long)42L,     record.getLong("Long A"));
        assertEquals("Long B",   (Long)42L,     record.getLong("Long B"));
        assertEquals("Long C",   (Long)42L,     record.getLong("Long C"));
        assertEquals("Long D",   (Long)42L,     record.getLong("Long D"));
        assertEquals("Double A", (Double)42D,   record.getDouble("Double A"));
        assertEquals("Double B", (Double)42D,   record.getDouble("Double B"));
        assertEquals("Double C", (Double)42D,   record.getDouble("Double C"));
        assertEquals("Double D", (Double)42D,   record.getDouble("Double D"));

        assertEquals("Multi_A.* --> 1", "Foo",  record.getStringSet("Multi_A.*").get("1"));
        assertEquals("Multi_A.* --> 2", "Bar",  record.getStringSet("Multi_A.*").get("2"));

        assertEquals("Multi_B.* --> 1", "Foo",  record.getStringSet("Multi_B.*").get("1"));
        assertEquals("Multi_B.* --> 2", "Bar",  record.getStringSet("Multi_B.*").get("2"));

        assertEquals("Multi_C.* --> 1", "Foo",  record.getStringSet("Multi_C.*").get("1"));
        assertEquals("Multi_C.* --> 2", "Bar",  record.getStringSet("Multi_C.*").get("2"));
    }

}
