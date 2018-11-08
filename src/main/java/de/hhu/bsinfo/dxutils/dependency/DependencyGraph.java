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

import java.util.HashMap;
import java.util.Map;

public class DependencyGraph {

    private final Map<String, Resolver.Node> m_nodeMap;

    public DependencyGraph() {
        m_nodeMap = new HashMap<>();
    }

    public void addNode(final @NotNull String p_id) {
        if (m_nodeMap.containsKey(p_id)) {
            return;
        }

        m_nodeMap.put(p_id, new Resolver.Node(p_id));
    }

    public void addEdge(final @NotNull String p_from, final @NotNull String p_to) {
        if (!m_nodeMap.containsKey(p_from) || !m_nodeMap.containsKey(p_to)) {
            return;
        }

        m_nodeMap.get(p_from).addEdge(m_nodeMap.get(p_to));
    }

    @Nullable
    public Resolver.Node getNode(final @NotNull String p_id) {
        return m_nodeMap.get(p_id);
    }
}
