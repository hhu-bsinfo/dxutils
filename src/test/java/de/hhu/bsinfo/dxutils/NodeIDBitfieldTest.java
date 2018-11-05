package de.hhu.bsinfo.dxutils;

import org.junit.Assert;
import org.junit.Test;

public class NodeIDBitfieldTest {
    @Test
    public void test1() {
        NodeIDBitfield bitfield = new NodeIDBitfield();

        bitfield.set((short) 10, true);
        Assert.assertTrue(bitfield.get((short) 10));
        bitfield.set((short) 10, false);

        for (int i = 0; i < 0xFFFF; i++) {
            Assert.assertFalse(bitfield.get((short) i));
        }
    }

    @Test
    public void test2() {
        NodeIDBitfield bitfield = new NodeIDBitfield();

        bitfield.set((short) 0xABCD, true);
        Assert.assertTrue(bitfield.get((short) 0xABCD));
        bitfield.set((short) 0xABCD, false);

        for (int i = 0; i < 0xFFFF; i++) {
            Assert.assertFalse(bitfield.get((short) i));
        }
    }

    @Test
    public void test3() {
        NodeIDBitfield bitfield = new NodeIDBitfield();

        for (int i = 0; i < 0xFFFF; i++) {
            if (i % 2 == 0) {
                continue;
            }

            bitfield.set((short) i, true);
        }

        for (int i = 0; i < 0xFFFF; i++) {
            if (i % 2 == 0) {
                Assert.assertFalse(bitfield.get((short) i));
            } else {
                Assert.assertTrue(bitfield.get((short) i));
            }
        }
    }

    @Test
    public void test4() {
        NodeIDBitfield bitfield = new NodeIDBitfield();

        for (int i = 0; i < 0xFFFF; i++) {
            if (i % 2 == 1) {
                continue;
            }

            bitfield.set((short) i, true);
        }

        for (int i = 0; i < 0xFFFF; i++) {
            if (i % 2 == 1) {
                Assert.assertFalse(bitfield.get((short) i));
            } else {
                Assert.assertTrue(bitfield.get((short) i));
            }
        }
    }
}
