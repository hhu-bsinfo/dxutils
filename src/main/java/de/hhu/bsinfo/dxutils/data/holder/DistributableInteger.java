package de.hhu.bsinfo.dxutils.data.holder;

import de.hhu.bsinfo.dxutils.serialization.Exporter;
import de.hhu.bsinfo.dxutils.serialization.Importer;

public class DistributableInteger extends DistributableValue<Integer> {

    public DistributableInteger() {
        super(null);
    }

    public DistributableInteger(Integer p_value) {
        super(p_value);
    }

    @Override
    public void exportObject(Exporter p_exporter) {
        p_exporter.writeInt(m_value);
    }

    @Override
    public void importObject(Importer p_importer) {
        if (m_value == null) {
            m_value = 0;
        }

        m_value = p_importer.readInt(m_value);
    }

    @Override
    public int sizeofObject() {
        return Integer.BYTES;
    }
}
