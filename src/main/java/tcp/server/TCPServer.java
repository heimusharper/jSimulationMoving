/*******************************************************************************
 * Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * TCPServer is part of jSimulationMoving.
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

package tcp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.DBus;
import tools.Prop;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TCP сервер для организации независимого GUI <br>
 * Created by boris on 20.12.16.
 */
public class TCPServer extends Thread {

    private static final Logger log = LoggerFactory.getLogger(TCPServer.class);

    private final int SERVER_PORT; // Порт, на котором висит сервер

    /**
     * Allocates a new {@code Thread} object. This constructor has the same
     * effect as {@linkplain #Thread(ThreadGroup, Runnable, String) Thread}
     * {@code (null, null, gname)}, where {@code gname} is a newly generated
     * name. Automatically generated names are of the form
     * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
     */
    public TCPServer() {
        SERVER_PORT = Prop.getServerPort();
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override public void run() {
        // Открываем сокет
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
        } catch (IOException e) {
            log.error("Fail!", e);
        }
        log.info("Server started at {}", serverSocket);

        // Ждем подключение
        //noinspection InfiniteLoopStatement
        while (true) {
            log.info("Waiting for a connection...");

            Socket activeSocket = null;
            try {
                activeSocket =
                        serverSocket != null ? serverSocket.accept() : null;
                log.info("Received a connection from {}", activeSocket);
            } catch (IOException e) {
                log.error("Fail!", e);
            }

            final Socket finalActiveSocket = activeSocket;
            Runnable runnable = () -> handleClientRequest(finalActiveSocket);

            // Стартуем новыйе поток для клиента
            new Thread(runnable).start();
            log.info("Start a new thread for client {}",
                    finalActiveSocket != null ?
                            finalActiveSocket.getInetAddress() :
                            null);
        }
    }

    /**
     * Обработчик запросов клиента
     *
     * @param socket - активный сокет, октрытый для клиента
     */
    private static void handleClientRequest(final Socket socket) {
        try (final BufferedReader socketReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
                final BufferedWriter socketWriter = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream()))) {

            String inMsg;
            while ((inMsg = socketReader.readLine()) != null) {
                log.info("Received from client: {}", inMsg);

                if (inMsg.equals("geom")) {
                    socketWriter.write(DBus.getRawJson());
                    log.info("Send to client json");
                } else {
                    socketWriter.write(inMsg);
                }
                socketWriter.write("\n");
                socketWriter.flush();
            }
            socket.close();
            log.info("Close connection with {}", socket.getInetAddress());
        } catch (Exception e) {
            log.error("Fail!", e);
        }
    }
}
