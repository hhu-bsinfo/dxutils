package de.hhu.bsinfo.dxutils.data.holder;

import java.nio.ByteBuffer;

import de.hhu.bsinfo.dxutils.serialization.Exporter;
import de.hhu.bsinfo.dxutils.serialization.Importer;

public class DistributableByte extends DistributableValue<Byte> {

    public DistributableByte() {
        super(null);
    }

    public DistributableByte(Byte p_value) {
        super(p_value);
    }

    @Override
    public void exportObject(Exporter p_exporter) {
        p_exporter.writeByte(m_value);
    }

    @Override
    public void importObject(Importer p_importer) {
        if (m_value == null) {
            m_value = 0;
        }

        m_value = p_importer.readByte(m_value);
    }

    @Override
    public int sizeofObject() {
        return Byte.BYTES;
    }
}
