/*
 * Copyright (C) 2018 Heinrich-Heine-Universitaet Duesseldorf, Institute of Computer Science,
 * Department Operating Systems
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package de.hhu.bsinfo.dxutils.dependency;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class DependencyGraphTest {

    private static final String DEP_ONE = "dep1";
    private static final String DEP_TWO = "dep2";
    private static final String DEP_THREE = "dep3";
    private static final String DEP_FOUR = "dep4";

    @Test
    public void stringTest() throws CircularDependencyException {
        DependencyGraph<String> graph = new DependencyGraph<>();

        graph.add(DEP_ONE, DEP_FOUR);
        graph.add(DEP_TWO, DEP_THREE);
        graph.add(DEP_FOUR, DEP_THREE);

        List<String> actual = graph.resolve(DEP_ONE);

        List<String> expected = Arrays.asList(DEP_THREE, DEP_FOUR, DEP_ONE);

        assertEquals(expected, actual);
    }

    @Test
    public void integerTest() throws CircularDependencyException {
        DependencyGraph<Integer> graph = new DependencyGraph<>();

        graph.add(0, 1, 2, 3, 4);
        graph.add(1, 2, 3, 4);
        graph.add(2, 3, 4);
        graph.add(3, 4);

        List<Integer> actual = graph.resolve(0);

        List<Integer> expected = Arrays.asList(4, 3, 2, 1, 0);

        assertEquals(expected, actual);
    }

    @Test(expected = CircularDependencyException.class)
    public void circularTest() throws CircularDependencyException {
        DependencyGraph<Integer> graph = new DependencyGraph<>();

        graph.add(0, 1, 2);
        graph.add(2, 0);

        List<Integer> actual = graph.resolve(0);

        fail();
    }

    @Test
    public void classTest() throws CircularDependencyException {
        DependencyGraph<Class> graph = new DependencyGraph<>();

        graph.add(A.class, E.class, D.class);
        graph.add(B.class, A.class, E.class);
        graph.add(D.class, C.class);
        graph.add(E.class, F.class);

        List<Class> actual = graph.resolve(B.class);

        List<Class> expected = Arrays.asList(C.class, D.class, F.class, E.class, A.class, B.class);

        assertEquals(expected, actual);
    }

    private static class A {}
    private static class B {}
    private static class C {}
    private static class D {}
    private static class E {}
    private static class F {}

}