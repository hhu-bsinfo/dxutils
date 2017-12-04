/*
 * Copyright (C) 2017 Heinrich-Heine-Universitaet Duesseldorf, Institute of Computer Science, Department Operating Systems
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package de.hhu.bsinfo.dxutils.reflect.dt;

import java.math.BigInteger;

/**
 * Implementation of a long parser.
 *
 * @author Stefan Nothaas, stefan.nothaas@hhu.de, 26.01.2016
 */
public class DataTypeParserLong implements DataTypeParser {
    @Override
    public java.lang.String getTypeIdentifer() {
        return "long";
    }

    @Override
    public Class<?> getClassToConvertTo() {
        return Long.class;
    }

    @Override
    public Object parse(final java.lang.String p_str) {
        try {
            if (p_str.length() > 1) {
                String tmp = p_str.substring(0, 2);
                // oh java...no unsigned, why?
                if ("0x".equals(tmp)) {
                    return new BigInteger(p_str.substring(2), 16).longValue();
                } else if ("0b".equals(tmp)) {
                    return new BigInteger(p_str.substring(2), 2).longValue();
                } else if ("0o".equals(tmp)) {
                    return new BigInteger(p_str.substring(2), 8).longValue();
                }
            }

            return java.lang.Long.parseLong(p_str);
        } catch (final NumberFormatException ignored) {
            return 0L;
        }
    }
}