/*******************************************************************************
 * Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * Prop is part of jSimulationMoving.
 *
 * jSimulationMoving is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jSimulationMoving is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jSimulationMoving. If not, see <http://www.gnu.org/licenses/>.
 * -----------------------------------------------------------------------------
 *
 * This code is in BETA; some features are incomplete and the code
 * could be written better.
 ******************************************************************************/

package tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс чтения настроек <br>
 * Настройки хранятся в файле <i>resources/config.properties</i> <br>
 * <p>
 * Created by boris on 20.12.16.
 */
public class Prop {
    private static final Logger log = LoggerFactory.getLogger(Prop.class);

    // Название файла с настройками
    private static final String FILE_NAME = "config.properties";
    // Порт, на котором работает сервер
    private static int    serverPort;
    // Порт, на котором работает сервер
    private static int    clientPort;
    // Порт, на котором работает сервер
    private static String clientHost;

    /*
     * Чтение файла с настройками происходит один раз при первом вызове
     * класса Prop
     */
    static {
        try (final InputStream is = Prop.class.getClassLoader()
                .getResourceAsStream(FILE_NAME)) {

            final Properties property = new Properties();
            property.load(is);

            serverPort = Integer.parseInt(
                    property.getProperty(Fields.RINTD_TCP_SERVER_PORT));
            clientPort = Integer.parseInt(
                    property.getProperty(Fields.RINTD_TCP_CLIENT_PORT));
            clientHost = property.getProperty(Fields.RINTD_TCP_CLIENT_HOST);
            log.info("Successful read properties from {}", FILE_NAME);
        } catch (IOException e) {
            log.error("Fail! File not found {}", FILE_NAME, e);
        }
    }

    /**
     * @return Порт, на котором будет работать сервер
     */
    public static int getServerPort() {
        return serverPort;
    }

    public static String getClientHost() {
        return clientHost;
    }

    public static int getClientPort() {
        return clientPort;
    }

    /**
     * Класс, в котором описаны поля файла с настройками
     */
    private class Fields {
        private static final String RINTD_TCP_SERVER_PORT = "rintd.tcp.server.port";
        private static final String RINTD_TCP_CLIENT_PORT =
                "rintd.tcp.client" + ".port";
        private static final String RINTD_TCP_CLIENT_HOST =
                "rintd.tcp.client" + ".host";
    }
}
