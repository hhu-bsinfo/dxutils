package de.hhu.bsinfo.dxutils.data;

import java.nio.ByteBuffer;

import org.junit.Test;

import de.hhu.bsinfo.dxutils.data.holder.DistributableInteger;
import de.hhu.bsinfo.dxutils.data.holder.DistributableString;
import de.hhu.bsinfo.dxutils.serialization.ByteBufferImExporter;

import static org.junit.Assert.*;

public class DistributableStructuresTest {

    private final static int BUFFER_SIZE = 1024 * 1024;

    @Test
    public void testHashMap() {
        DistributableHashMap<String, String> expected =
                new DistributableHashMap<>(DistributableString::new, DistributableString::new);

        expected.put("key1", "value1");
        expected.put("key2", "value2");
        expected.put("key3", "value3");

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ByteBufferImExporter converter = new ByteBufferImExporter(buffer);
        converter.exportObject(expected);

        buffer.position(0);
        DistributableHashMap<String, String> actual = new DistributableHashMap<>();
        converter.importObject(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void testHashSet() {
        DistributableHashSet<String> expected = new DistributableHashSet<>(DistributableString::new);

        expected.add("element1");
        expected.add("element2");
        expected.add("element3");

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ByteBufferImExporter converter = new ByteBufferImExporter(buffer);
        converter.exportObject(expected);

        buffer.position(0);
        DistributableHashSet<String> actual = new DistributableHashSet<>();
        converter.importObject(actual);

        assertEquals(expected, actual);
    }

    @Test
    public void testArrayList() {
        DistributableArrayList<String> expected = new DistributableArrayList<>(DistributableString::new);

        expected.add("element1");
        expected.add("element2");
        expected.add("element3");

        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ByteBufferImExporter converter = new ByteBufferImExporter(buffer);
        converter.exportObject(expected);

        buffer.position(0);
        DistributableArrayList<String> actual = new DistributableArrayList<>();
        converter.importObject(actual);

        assertEquals(expected, actual);
    }

}