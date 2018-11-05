package de.hhu.bsinfo.dxutils;

import org.junit.Assert;
import org.junit.Test;

public class ArrayListShortTest {
    @Test
    public void create() {
        ArrayListShort list = new ArrayListShort();

        Assert.assertEquals(0, list.getSize());
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void create2() {
        ArrayListShort list = new ArrayListShort(5);

        Assert.assertEquals(0, list.getSize());
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void create3() {
        ArrayListShort list = new ArrayListShort((short) 100);

        Assert.assertEquals(1, list.getSize());
        Assert.assertFalse(list.isEmpty());
    }

    @Test
    public void create4() {
        ArrayListShort list = ArrayListShort.wrap(new short[] {1, 2, 3, 4});

        Assert.assertEquals(4, list.getSize());
        Assert.assertFalse(list.isEmpty());
    }

    @Test
    public void create5() {
        ArrayListShort list = ArrayListShort.copy(new short[] {1, 2, 3, 4});

        Assert.assertEquals(4, list.getSize());
        Assert.assertFalse(list.isEmpty());
    }

    @Test
    public void add() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 5);
        list.add((short) 10);
        list.add((short) 13);

        Assert.assertEquals(3, list.getSize());
        Assert.assertFalse(list.isEmpty());

        Assert.assertEquals(5, list.get(0));
        Assert.assertEquals(10, list.get(1));
        Assert.assertEquals(13, list.get(2));
    }

    @Test
    public void add2() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 5);
        list.add(2, (short) 10);
        list.add(5, (short) 13);

        Assert.assertEquals(6, list.getSize());
        Assert.assertFalse(list.isEmpty());

        Assert.assertEquals(5, list.get(0));
        Assert.assertEquals(0, list.get(1));
        Assert.assertEquals(10, list.get(2));
        Assert.assertEquals(0, list.get(3));
        Assert.assertEquals(0, list.get(4));
        Assert.assertEquals(13, list.get(5));
    }

    @Test
    public void set() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 5);
        list.add(2, (short) 10);
        list.add(5, (short) 13);

        list.set(1, (short) 15);
        list.set(3, (short) 20);

        Assert.assertEquals(6, list.getSize());
        Assert.assertFalse(list.isEmpty());

        Assert.assertEquals(5, list.get(0));
        Assert.assertEquals(15, list.get(1));
        Assert.assertEquals(10, list.get(2));
        Assert.assertEquals(20, list.get(3));
        Assert.assertEquals(0, list.get(4));
        Assert.assertEquals(13, list.get(5));
    }

    @Test
    public void remove() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 5);
        list.add(2, (short) 10);
        list.add(5, (short) 13);

        Assert.assertEquals(6, list.getSize());
        Assert.assertFalse(list.isEmpty());

        Assert.assertEquals(10, list.remove(2));

        Assert.assertEquals(5, list.getSize());
        Assert.assertFalse(list.isEmpty());

        Assert.assertEquals(5, list.get(0));
        Assert.assertEquals(0, list.get(2));
        Assert.assertEquals(13, list.get(4));
    }

    @Test
    public void addFront() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 5);
        list.add(2, (short) 10);
        list.add(5, (short) 13);
        list.addFront((short) 9);

        Assert.assertEquals(7, list.getSize());
        Assert.assertFalse(list.isEmpty());

        Assert.assertEquals(9, list.get(0));
        Assert.assertEquals(5, list.get(1));
        Assert.assertEquals(0, list.get(2));
        Assert.assertEquals(10, list.get(3));
        Assert.assertEquals(0, list.get(4));
        Assert.assertEquals(0, list.get(5));
        Assert.assertEquals(13, list.get(6));
    }

    @Test
    public void insert() {
        ArrayListShort list = new ArrayListShort();

        list.insert(0, (short) 5);

        Assert.assertEquals(5, list.get(0));
        Assert.assertEquals(1, list.getSize());
    }

    @Test
    public void insert2() {
        ArrayListShort list = new ArrayListShort();

        try {
            list.insert(1, (short) 5);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return;
        }

        Assert.fail("Exception expected");
    }

    @Test
    public void insert3() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 1);
        list.add((short) 2);
        list.add((short) 3);
        list.insert(1, (short) 5);

        Assert.assertEquals(1, list.get(0));
        Assert.assertEquals(5, list.get(1));
        Assert.assertEquals(2, list.get(2));
        Assert.assertEquals(3, list.get(3));
        Assert.assertEquals(4, list.getSize());
    }

    @Test
    public void insert4() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 1);
        list.add((short) 2);
        list.add((short) 3);
        list.insert(0, (short) 5);

        Assert.assertEquals(5, list.get(0));
        Assert.assertEquals(1, list.get(1));
        Assert.assertEquals(2, list.get(2));
        Assert.assertEquals(3, list.get(3));
        Assert.assertEquals(4, list.getSize());
    }

    @Test
    public void insert5() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 1);
        list.add((short) 2);
        list.add((short) 3);
        list.insert(2, (short) 5);

        Assert.assertEquals(1, list.get(0));
        Assert.assertEquals(2, list.get(1));
        Assert.assertEquals(5, list.get(2));
        Assert.assertEquals(3, list.get(3));
        Assert.assertEquals(4, list.getSize());
    }

    @Test
    public void insert6() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 1);
        list.add((short) 2);
        list.add((short) 3);
        list.insert(3, (short) 5);

        Assert.assertEquals(1, list.get(0));
        Assert.assertEquals(2, list.get(1));
        Assert.assertEquals(3, list.get(2));
        Assert.assertEquals(5, list.get(3));
        Assert.assertEquals(4, list.getSize());
    }

    @Test
    public void insert7() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 1);
        list.add((short) 2);
        list.add((short) 3);
        list.insert(3, (short) 5, (short) 6);

        Assert.assertEquals(1, list.get(0));
        Assert.assertEquals(2, list.get(1));
        Assert.assertEquals(3, list.get(2));
        Assert.assertEquals(5, list.get(3));
        Assert.assertEquals(6, list.get(4));
        Assert.assertEquals(5, list.getSize());
    }

    @Test
    public void insert8() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 1);
        list.add((short) 2);
        list.add((short) 3);
        list.insert(0, (short) 5, (short) 6);

        Assert.assertEquals(5, list.get(0));
        Assert.assertEquals(6, list.get(1));
        Assert.assertEquals(1, list.get(2));
        Assert.assertEquals(2, list.get(3));
        Assert.assertEquals(3, list.get(4));
        Assert.assertEquals(5, list.getSize());
    }

    @Test
    public void insert9() {
        ArrayListShort list = new ArrayListShort();

        list.add((short) 1);
        list.add((short) 2);
        list.add((short) 3);
        list.insert(2, (short) 5, (short) 6);

        Assert.assertEquals(1, list.get(0));
        Assert.assertEquals(2, list.get(1));
        Assert.assertEquals(5, list.get(2));
        Assert.assertEquals(6, list.get(3));
        Assert.assertEquals(3, list.get(4));
        Assert.assertEquals(5, list.getSize());
    }

    @Test
    public void insert10() {
        ArrayListShort list = new ArrayListShort();

        list.insert(0, (short) 5, (short) 6);

        Assert.assertEquals(5, list.get(0));
        Assert.assertEquals(6, list.get(1));
        Assert.assertEquals(2, list.getSize());
    }
}
