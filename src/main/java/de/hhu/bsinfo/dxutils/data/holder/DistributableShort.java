package de.hhu.bsinfo.dxutils.data.holder;

import de.hhu.bsinfo.dxutils.serialization.Exporter;
import de.hhu.bsinfo.dxutils.serialization.Importer;

public class DistributableShort extends DistributableValue<Short> {

    public DistributableShort() {
        super(null);
    }

    public DistributableShort(Short p_value) {
        super(p_value);
    }

    @Override
    public void exportObject(Exporter p_exporter) {
        p_exporter.writeShort(m_value);
    }

    @Override
    public void importObject(Importer p_importer) {
        if (m_value == null) {
            m_value = 0;
        }

        m_value = p_importer.readShort(m_value);
    }

    @Override
    public int sizeofObject() {
        return Short.BYTES;
    }
}
