package de.hhu.bsinfo.dxutils.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import de.hhu.bsinfo.dxutils.data.holder.DistributableValue;
import de.hhu.bsinfo.dxutils.serialization.ClassUtil;
import de.hhu.bsinfo.dxutils.serialization.Distributable;
import de.hhu.bsinfo.dxutils.serialization.Exporter;
import de.hhu.bsinfo.dxutils.serialization.Importer;
import de.hhu.bsinfo.dxutils.serialization.ObjectSizeUtil;

/**
 * A distributable ArrayList using generic types.
 *
 * @param <E> The value's type.
 *
 * @author Filip Krakowski, krakowski@uni-duesseldorf.de
 */
public class DistributableHashSet<E> extends HashSet<E> implements Distributable {

    private int m_size;
    private DistributableValue<E> m_valueHolder;
    private String m_valueHolderClass;

    public DistributableHashSet() {}

    public DistributableHashSet(int p_initialCapacity, Supplier<DistributableValue<E>> p_valueSupplier) {
        super(p_initialCapacity);
        m_valueHolder = p_valueSupplier.get();
        m_valueHolderClass = m_valueHolder.getClass().getName();
    }

    public DistributableHashSet(Supplier<DistributableValue<E>> p_valueSupplier) {
        m_valueHolder = p_valueSupplier.get();
        m_valueHolderClass = m_valueHolder.getClass().getName();
    }

    public DistributableHashSet(@NotNull Collection<? extends E> p_source,
            Supplier<DistributableValue<E>> p_valueSupplier) {
        super(p_source);
        m_valueHolder = p_valueSupplier.get();
        m_valueHolderClass = m_valueHolder.getClass().getName();
    }

    @Override
    public void exportObject(Exporter p_exporter) {
        p_exporter.writeString(m_valueHolderClass);

        p_exporter.writeInt(size());

        for (final E element : this) {
            p_exporter.exportObject(wrapValue(element));
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void importObject(Importer p_importer) {
        m_valueHolderClass = p_importer.readString(m_valueHolderClass);

        if (m_valueHolder == null) {
            m_valueHolder = ClassUtil.createInstance(m_valueHolderClass);
        }

        m_size = p_importer.readInt(m_size);

        for (int i = 0; i < m_size; i ++) {
            p_importer.importObject(m_valueHolder);
            add(m_valueHolder.getValue());
        }
    }

    @Override
    public int sizeofObject() {
        int size = Integer.BYTES + ObjectSizeUtil.sizeofString(m_valueHolderClass);

        for (final E element : this) {
            size += wrapValue(element).sizeofObject();
        }

        return size;
    }

    private DistributableValue<E> wrapValue(final E p_value) {
        m_valueHolder.setValue(p_value);
        return m_valueHolder;
    }
}
