package de.hhu.bsinfo.dxutils.dependency;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Node<T> {

    private final T m_identifier;

    private final Set<Node<T>> m_edges;

    public Node(final @NotNull T p_content) {
        m_identifier = p_content;
        m_edges = new HashSet<>();
    }

    public void addEdge(final @NotNull Node<T> p_node) {
        m_edges.add(p_node);
    }

    public T getIdentifier() {
        return m_identifier;
    }

    public Set<Node<T>> getEdges() {
        return Collections.unmodifiableSet(m_edges);
    }

    public int edgeCount() {
        return m_edges.size();
    }

    @Override
    public boolean equals(Object p_o) {
        if (this == p_o) return true;
        if (p_o == null || getClass() != p_o.getClass()) return false;
        Node node = (Node) p_o;
        return m_identifier.equals(node.m_identifier);
    }

    @Override
    public int hashCode() {
        return m_identifier.hashCode();
    }
}
