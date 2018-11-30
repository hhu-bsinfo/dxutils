/*
 * Copyright (C) 2018 Heinrich-Heine-Universitaet Duesseldorf, Institute of Computer Science,
 * Department Operating Systems
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package de.hhu.bsinfo.dxutils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonUtil {
    private static final Logger LOGGER = LogManager.getFormatterLogger(JsonUtil.class);

    private static final Pattern INDEX_PATTERN = Pattern.compile("(\\S+)\\[(\\S+)\\]");

    public static void override(final JsonElement p_root, final Properties p_properties, final String p_propertyKey) {
        override(p_root, p_properties, p_propertyKey, Collections.emptyList());
    }

    public static void override(final JsonElement p_root, final Properties p_properties, final String p_propertyKey,
            final List<String> p_ignoredPrefixes) {
        Enumeration e = p_properties.propertyNames();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            boolean ignorePrefix = p_ignoredPrefixes.stream()
                    .anyMatch(key::startsWith);

            if (!key.startsWith(p_propertyKey) || ignorePrefix) {
                continue;
            }

            // Split property name into tokens
            final String[] tokens = key.split("\\.");

            if (tokens.length < 2) {
                continue;
            }

            final String[] jsonPath = Arrays.copyOfRange(tokens, 1, tokens.length - 1);

            // Traverse all tokens until the json object is found
            JsonElement element = p_root;
            Matcher matcher;
            for (String pathElement : jsonPath) {
                matcher = INDEX_PATTERN.matcher(pathElement);
                if (matcher.find()) {
                    String name = matcher.group(1);
                    String index = matcher.group(2);

                    if (StringUtils.isNumeric(index)) {
                        element = getOrCreateArrayObject(element.getAsJsonObject().get(name), Integer.parseInt(index));
                    } else {
                        element = element.getAsJsonObject().getAsJsonObject(name).get(index);
                    }
                } else {
                    element = element.getAsJsonObject().get(pathElement);
                }

                if (element == null) {
                    break;
                }
            }

            if (element == null) {
                LOGGER.warn("Specified non-existent configuration key {}", String.join(".", jsonPath));
                continue;
            }

            // Get key and value
            final String jsonKey = tokens[tokens.length - 1];
            final String jsonValue = p_properties.getProperty(key);

            // Override value
            JsonObject parent = element.getAsJsonObject();
            if (jsonValue.matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")) {
                // ip address
                LOGGER.debug("Overriding (ip) %s with new value %s", jsonKey, jsonValue);
                parent.addProperty(jsonKey, jsonValue);
            } else if (jsonValue.matches("[-+]?\\d*\\.?\\d+")) {
                // numeric
                LOGGER.debug("Overriding (num) %s with new value %d", jsonKey, Long.parseLong(jsonValue));
                parent.addProperty(jsonKey, Long.parseLong(jsonValue));
            } else {
                // string
                LOGGER.debug("Overriding (str) %s with new value %s", jsonKey, jsonValue);
                parent.addProperty(jsonKey, jsonValue);
            }
        }
    }

    private static JsonObject getOrCreateArrayObject(final JsonElement p_parent, final int p_index) {
        JsonArray array =  p_parent.getAsJsonArray();

        int size = array.size();
        if (p_index > size) {
            throw new IndexOutOfBoundsException();
        }

        JsonObject object;
        if (p_index == size) {
            object = new JsonObject();
            array.add(object);
        } else {
            object = array.get(p_index).getAsJsonObject();
        }

        return object;
    }
}
