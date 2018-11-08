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

import static org.junit.Assert.*;

public class ResolverTest {

    private static final String DEP_ONE = "dep1";
    private static final String DEP_TWO = "dep2";
    private static final String DEP_THREE = "dep3";
    private static final String DEP_FOUR = "dep4";

    @Test
    public void resolve() throws CircularDependencyException {
        Resolver.Node dep1 = new Resolver.Node(DEP_ONE);
        Resolver.Node dep2 = new Resolver.Node(DEP_TWO);
        Resolver.Node dep3 = new Resolver.Node(DEP_THREE);
        Resolver.Node dep4 = new Resolver.Node(DEP_FOUR);

        /**
         * +----+     +----+     +----+     +----+
         * |dep3| +-> |dep1| <-+ |dep2| <-+ |dep4|
         * +----+     +----+     +----+     +----+
         */

        dep3.addEdge(dep1);
        dep2.addEdge(dep1);
        dep4.addEdge(dep2);

        List<Resolver.Node> actual = Resolver.resolve(dep4);
        List<Resolver.Node> expected = Arrays.asList(dep1, dep2, dep4);

        assertEquals(expected, actual);
    }

    @Test(expected = CircularDependencyException.class)
    public void resolveCircular() throws CircularDependencyException {
        Resolver.Node dep1 = new Resolver.Node(DEP_ONE);
        Resolver.Node dep2 = new Resolver.Node(DEP_TWO);
        Resolver.Node dep3 = new Resolver.Node(DEP_THREE);
        Resolver.Node dep4 = new Resolver.Node(DEP_FOUR);

        /**
         * +----+     +----+     +----+     +----+
         * |dep3| <-> |dep1| <-+ |dep2| <-+ |dep4|
         * +----+     +----+     +----+     +----+
         */

        dep3.addEdge(dep1);
        dep2.addEdge(dep1);
        dep4.addEdge(dep2);
        dep1.addEdge(dep3);

        List<Resolver.Node> actual = Resolver.resolve(dep4);
    }
}