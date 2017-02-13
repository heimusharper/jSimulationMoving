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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.Moving;
import tools.Plotting;

import java.util.ArrayList;

/**
 * Точка входа
 * Created by boris on 09.12.16.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args)
            throws InstantiationException, IllegalAccessException, InterruptedException {
        String fileName = "UdSU_c6sD_devc.csv";
        /*double[] densities = new double[] { 0.01, 0.05, 0.1, 0.2, 0.5 };*/
        double[] densities = new double[] { 0.2 };
        boolean isFire = true;
        ArrayList<String> doors = new ArrayList<>();
        /*doors.add("A");/**/
        /*doors.add("B");/**/
        /*doors.add("C");/**/
        /*doors.add("D");/**/
        /*doors.add("E");/**/
        /*doors.add("F");/**/
        doors.add("G");/**/

        Plotting plot = new Plotting(fileName, isFire);
        for (double density : densities) {
            new Moving(fileName, density, isFire, plot).run();
        }
        /*plot.genGeneralPlotFile();*/
        plot.genTransitionPlotFile(doors);

        // TCP_SERVER
        /*log.info("Starting tcp server");
        TCPServer server = new TCPServer();
        server.start();*/

        // TCP_CLIENT
        /*log.info("Starting tcp client");
        new TCPClient();*/
    }
}
