package de.hhu.bsinfo.dxutils.data.holder;

import de.hhu.bsinfo.dxutils.serialization.Distributable;

public abstract class DistributableValue<T> implements Distributable {

    protected T m_value;

    DistributableValue(final T p_initialValue) {
        m_value = p_initialValue;
    }

    public T getValue() {
        return m_value;
    }

    public void setValue(final T p_value) {
        m_value = p_value;
    }
}
