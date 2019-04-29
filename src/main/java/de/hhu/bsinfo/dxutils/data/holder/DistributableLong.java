package de.hhu.bsinfo.dxutils.data.holder;

import de.hhu.bsinfo.dxutils.serialization.Exporter;
import de.hhu.bsinfo.dxutils.serialization.Importer;

public class DistributableLong extends DistributableValue<Long> {

    public DistributableLong() {
        super(null);
    }

    public DistributableLong(Long p_value) {
        super(p_value);
    }

    @Override
    public void exportObject(Exporter p_exporter) {
        p_exporter.writeLong(m_value);
    }

    @Override
    public void importObject(Importer p_importer) {
        if (m_value == null) {
            m_value = 0L;
        }

        m_value = p_importer.readLong(m_value);
    }

    @Override
    public int sizeofObject() {
        return Long.BYTES;
    }
}
