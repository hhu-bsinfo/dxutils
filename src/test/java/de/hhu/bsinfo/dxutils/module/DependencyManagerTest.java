package de.hhu.bsinfo.dxutils.module;

import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class DependencyManagerTest {

    @Test
    public void getOrderedDependencies() {
        DependencyManager<Object> dependencyManager = new DependencyManager<>();

        dependencyManager.register(A.class);
        dependencyManager.register(B.class);
        dependencyManager.register(C.class);
        dependencyManager.register(D.class);
        dependencyManager.register(E.class);
        dependencyManager.register(F.class);
        dependencyManager.register(G.class);
        dependencyManager.register(H.class);

        final List<Class<?>> ordered = dependencyManager.getOrderedDependencies();
        Stream.of(A.class, B.class, C.class, D.class, E.class, F.class, G.class, H.class)
                .forEach(current -> DependencyManager.getDependencies(current)
                    .forEach(dependency -> assertAfter(dependency, current, ordered)));
    }

    private static void assertAfter(Class<?> p_before, Class<?> p_after, List<Class<?>> p_classes) {
        int beforeIndex = p_classes.indexOf(p_before);
        int afterIndex = p_classes.indexOf(p_after);

        assertTrue(beforeIndex >= 0);
        assertTrue(afterIndex >= 0);
        assertTrue(beforeIndex < afterIndex);
    }

    private static class A {
        @Dependency
        private B b;
    }

    private static class B {
        @Dependency
        private D d;

        @Dependency
        private E e;
    }

    private static class C {
        @Dependency
        private D d;

        @Dependency
        private H h;
    }

    private static class D {
        @Dependency
        private E e;


    }

    private static class E {

    }

    private static class F {
        @Dependency
        private E e;

        @Dependency
        private C c;
    }

    private static class G {
        @Dependency
        private A a;

        @Dependency
        private D d;

        @Dependency
        private B b;
    }

    private static class H {
        @Dependency
        private E e;

        @Dependency
        private B b;

        @Dependency
        private G g;
    }
}