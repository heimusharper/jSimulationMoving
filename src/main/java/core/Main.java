/*******************************************************************************
 * Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * Main is part of jSimulationMoving.
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
package core;


import bus.DBus;
import json.extendetGeometry.BIMExt;
import json.extendetGeometry.BIMLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.Moving;

/**
 * Точка входа
 * Created by boris on 09.12.16.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws InstantiationException, IllegalAccessException {
        // Загрузка структуры здания
        // BIM
        ClassLoader mainClassLoader = Main.class.getClassLoader();
        BIMLoader<BIMExt> bimLoader = new BIMLoader<>(mainClassLoader.
                getResourceAsStream("Stand-v1.2.json"), BIMExt.class);

        // Загрузка данных в шину
        DBus.setRawJson(bimLoader.getRawJson());

        Moving m = new Moving(bimLoader.getBim());
        m.run();
        
        // TCP_SERVER
        /*log.info("Start tcp server");
        new TCPServer().start();*/
    }
}
