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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyGraph<T> {

    private final Map<T, Node<T>> m_nodes = new HashMap<>();

    @SafeVarargs
    public final void add(final @NotNull T p_dependent, final T... p_dependencies) {
        Node<T> from = putIfAbsent(p_dependent);
        Arrays.stream(p_dependencies)
                .filter(Objects::nonNull)
                .forEach(to -> addEdge(from, to));
    }

    private void addEdge(final @NotNull Node<T> p_from, final @NotNull T p_to) {
        p_from.addEdge(putIfAbsent(p_to));
    }

    private Node<T> putIfAbsent(final @NotNull T p_element) {
        if (!m_nodes.containsKey(p_element)) {
            Node<T> node = new Node<>(p_element);
            m_nodes.put(p_element, node);
            return node;
        }

        return m_nodes.get(p_element);
    }

    /**
     * Determines the order in which dependencies must be loaded.
     *
     * @param p_root The root node.
     * @return An ordered list containing the dependencies to load.
     * @throws CircularDependencyException If a circular dependency was detected.
     */
    public List<T> resolve(final @NotNull T p_root) throws CircularDependencyException {
        Node<T> root = m_nodes.get(p_root);
        if (root == null) {
            return Collections.emptyList();
        }

        List<Node> resolved = new ArrayList<>();
        Set<Node> seen = new HashSet<>();

        resolve(root, resolved, seen);

        return resolved.stream().map(Node<T>::getIdentifier).collect(Collectors.toList());
    }

    private static <T> void resolve(final @NotNull Node<T> p_root, final @NotNull List<Node> p_resolved, final @NotNull Set<Node> p_seen) throws CircularDependencyException {
        p_seen.add(p_root);
        for (Node<T> node : p_root.getEdges()) {
            // Check if this node was already resolved
            if (p_resolved.contains(node)) {
                continue;
            }

            // Check if this node was already seen to prevent circular dependencies
            if (p_seen.contains(node)) {
                throw new CircularDependencyException(String.format("Circular dependency between %s and %s detected", p_root.getIdentifier(), node.getIdentifier()));
            }

            resolve(node, p_resolved, p_seen);
        }
        p_resolved.add(p_root);
    }
}
