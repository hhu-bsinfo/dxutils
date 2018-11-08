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
    public void testGraph() throws CircularDependencyException {
        DependencyGraph graph = new DependencyGraph();

        graph.addNode(DEP_ONE);
        graph.addNode(DEP_TWO);
        graph.addNode(DEP_THREE);
        graph.addNode(DEP_FOUR);

        graph.addEdge(DEP_ONE, DEP_FOUR);
        graph.addEdge(DEP_TWO, DEP_THREE);
        graph.addEdge(DEP_FOUR, DEP_THREE);

        List<String> actual = Resolver.resolve(graph.getNode(DEP_ONE))
                .stream()
                .map(Resolver.Node::getId)
                .collect(Collectors.toList());

        List<String> expected = Arrays.asList(DEP_THREE, DEP_FOUR, DEP_ONE);

        assertEquals(expected, actual);
    }

}