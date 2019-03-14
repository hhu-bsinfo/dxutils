package de.hhu.bsinfo.dxutils.dependency;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DependencyManager<T> {

    private final DependencyGraph<Class<? extends T>> m_dependencyGraph = new DependencyGraph<>();

    private final Predicate<Class<? extends T>> m_defaultPredicate = in -> true;

    public void register(final Class<? extends T> p_class) {
        Class[] dependencies = getDependencies(p_class).toArray(new Class[0]);
        m_dependencyGraph.add(p_class, dependencies);
    }

    public List<Class<? extends T>> getOrderedDependencies() {
        return getOrderedDependencies(m_defaultPredicate);
    }

    public List<Class<? extends T>> getOrderedDependencies(Class<? extends T> p_superclass) {
        return getOrderedDependencies(new ClassPredicate<>(p_superclass));
    }

    public List<Class<? extends T>> getOrderedDependencies(Predicate<Class<? extends T>> p_filter) {
        return m_dependencyGraph.values().stream()
                .flatMap(dependency -> m_dependencyGraph.resolve(dependency).stream())
                .filter(p_filter)
                .distinct()
                .collect(Collectors.toList());
    }

    static List<Class> getDependencies(final Class p_class) {
        return Arrays.stream(p_class.getDeclaredFields())
                .filter(field -> field.getAnnotation(Dependency.class) != null)
                .map(Field::getType)
                .collect(Collectors.toList());
    }

    private static class ClassPredicate<T> implements Predicate<Class<? extends T>> {

        private final Class<?> m_superclass;

        public ClassPredicate(Class<?> m_superclass) {
            this.m_superclass = m_superclass;
        }

        @Override
        public boolean test(Class<? extends T> p_class) {
            return m_superclass.isAssignableFrom(p_class);
        }
    }
}
