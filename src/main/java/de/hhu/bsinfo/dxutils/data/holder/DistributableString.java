package de.hhu.bsinfo.dxutils.data.holder;

import de.hhu.bsinfo.dxutils.serialization.Exporter;
import de.hhu.bsinfo.dxutils.serialization.Importer;
import de.hhu.bsinfo.dxutils.serialization.ObjectSizeUtil;

public class DistributableString extends DistributableValue<String> {

    public DistributableString() {
        super(null);
    }

    public DistributableString(String p_value) {
        super(p_value);
    }

    @Override
    public void exportObject(Exporter p_exporter) {
        p_exporter.writeString(m_value);
    }

    @Override
    public void importObject(Importer p_importer) {
        m_value = p_importer.readString(m_value);
    }

    @Override
    public int sizeofObject() {
        return ObjectSizeUtil.sizeofString(m_value);
    }
}
