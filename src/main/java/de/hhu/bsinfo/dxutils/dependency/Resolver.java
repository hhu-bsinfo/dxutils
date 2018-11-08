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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Resolver {

    /**
     * Determines the order dependencies must be loaded in.
     *
     * @param p_root The root node.
     * @return An ordered list containing the dependencies to load.
     * @throws CircularDependencyException If a circular dependency was detected.
     */
    public static List<Node> resolve(final @NotNull Node p_root) throws CircularDependencyException {
        List<Node> resolved = new ArrayList<>();
        List<Node> seen = new ArrayList<>();
        resolve(p_root, resolved, seen);
        return resolved;
    }

    private static void resolve(final @NotNull Node p_root, final @NotNull List<Node> p_resolved, final @NotNull List<Node> p_seen) throws CircularDependencyException {
        p_seen.add(p_root);
        for (Node node : p_root.getEdges()) {
            // Check if this node was already resolved
            if (p_resolved.contains(node)) {
                continue;
            }

            // Check if this node was already seen to prevent circular dependencies
            if (p_seen.contains(node)) {
                throw new CircularDependencyException(String.format("Circular dependency between %s and %s detected", p_root.getId(), node.getId()));
            }

            resolve(node, p_resolved, p_seen);
        }
        p_resolved.add(p_root);
    }

    public static class Node {

        private final String m_id;

        private final List<Node> m_edges;

        public Node(final @NotNull String p_id) {
            m_id = p_id;
            m_edges = new ArrayList<>();
        }

        public void addEdge(final @NotNull Node p_node) {
            m_edges.add(p_node);
        }

        public String getId() {
            return m_id;
        }

        public List<Node> getEdges() {
            return m_edges;
        }

        @Override
        public boolean equals(Object p_o) {
            if (this == p_o) return true;
            if (p_o == null || getClass() != p_o.getClass()) return false;
            Node node = (Node) p_o;
            return m_id.equals(node.m_id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(m_id);
        }
    }

}
