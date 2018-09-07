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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import static org.junit.Assert.*;

public class JsonUtilTest {

    private static final String CONFIG_FILE = "config.json";

    private static final Gson GSON = new GsonBuilder().create();

    @Test
    public void overrideObject() throws IOException {
        JsonElement element = getJsonConfig();

        Properties properties = new Properties();
        properties.setProperty("dxram.m_config.m_componentConfigs.ZookeeperBootComponentConfig.m_path", "/abc");

        JsonUtil.override(element, properties, "dxram");

        JsonPrimitive value = element.getAsJsonObject()
                .getAsJsonObject("m_config")
                .getAsJsonObject("m_componentConfigs")
                .getAsJsonObject("ZookeeperBootComponentConfig")
                .getAsJsonPrimitive("m_path");

        assertEquals("/abc", value.getAsString());
    }

    @Test
    public void overrideArray() throws IOException {
        JsonElement element = getJsonConfig();

        final String applicationClass = "de.hhu.bsinfo.Application";
        final int initOrderId = 42;
        Properties properties = new Properties();
        properties.setProperty("dxram.m_config.m_serviceConfigs[ApplicationServiceConfig].m_autoStart[0].m_className", applicationClass);
        properties.setProperty("dxram.m_config.m_serviceConfigs[ApplicationServiceConfig].m_autoStart[0].m_initOderId", String.valueOf(initOrderId));

        JsonUtil.override(element, properties, "dxram");

        JsonPrimitive className = element.getAsJsonObject()
                .getAsJsonObject("m_config")
                .getAsJsonObject("m_serviceConfigs")
                .getAsJsonObject("ApplicationServiceConfig")
                .getAsJsonArray("m_autoStart")
                .get(0).getAsJsonObject()
                .getAsJsonPrimitive("m_className");

        JsonPrimitive initId = element.getAsJsonObject()
                .getAsJsonObject("m_config")
                .getAsJsonObject("m_serviceConfigs")
                .getAsJsonObject("ApplicationServiceConfig")
                .getAsJsonArray("m_autoStart")
                .get(0).getAsJsonObject()
                .getAsJsonPrimitive("m_initOderId");

        assertEquals(applicationClass, className.getAsString());
        assertEquals(initOrderId, initId.getAsInt());
    }

    @Test
    public void overrideIgnorePrefix() throws IOException {
        JsonElement config = getJsonConfig();

        final String ip = "8.8.8.8";
        Properties properties = new Properties();
        properties.setProperty("dxram.m_config.m_engineConfig.m_address.m_ip", ip);
        properties.setProperty("dxram.config", "/some/path");

        JsonUtil.override(config, properties, "dxram");

        JsonPrimitive value = config.getAsJsonObject()
                .getAsJsonObject("m_config")
                .getAsJsonObject("m_engineConfig")
                .getAsJsonObject("m_address")
                .getAsJsonPrimitive("m_ip");

        assertEquals(ip, value.getAsString());
    }

    private JsonElement getJsonConfig() {
        InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE);
        Reader reader = new InputStreamReader(in);
        return GSON.fromJson(reader, JsonElement.class);
    }
}