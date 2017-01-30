/*
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
 */

package tcp;

import bus.Eventable;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulation.Moving;
import tools.ChangePeopleEvent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.System.arraycopy;
import static tools.Prop.getServerPort;

/**
 * TCP сервер для организации независимого GUI <br>
 * Created by boris on 20.12.16.
 */
public class TCPServer extends Thread {

    private static final Logger log = LoggerFactory.getLogger(TCPServer.class);

    /**
     * Allocates a new {@code Thread} object. This constructor has the same
     * effect as {@linkplain #Thread(ThreadGroup, Runnable, String) Thread}
     * {@code (null, null, gname)}, where {@code gname} is a newly generated
     * name. Automatically generated names are of the form
     * {@code "Thread-"+}<i>n</i>, where <i>n</i> is an integer.
     */
    public TCPServer() {
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
        ServerSocket serverSocket = null; // Открываем сокет
        try {
            serverSocket = new ServerSocket(getServerPort());
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
                activeSocket = serverSocket.accept();
                log.info("Received a connection from {}", activeSocket);
            } catch (IOException e) {
                log.error("Fail!", e);
            }

            // Стартуем новыйе поток для клиента
            try {
                new ClientHandler(activeSocket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Обработчик подключенного клиента.
     * Имеет в своем составе два метода: чтение из потока и запись данных в поток
     */
    private class ClientHandler extends Thread implements Eventable {
        private BufferedOutputStream bos;
        private BufferedInputStream  bis;
        private Socket               clientSocket;
        /**
         * Буфер байт, которые говорят о количестве байт, которые следует читать
         */
        private byte headerData[] = new byte[4];

        private ClientHandler(Socket clientSocket) throws IOException {
            setClientSocket(clientSocket);
            bos = new BufferedOutputStream(getClientSocket().getOutputStream());
            bis = new BufferedInputStream(getClientSocket().getInputStream());
            register(this);
        }

        private Socket getClientSocket() {
            return clientSocket;
        }

        private void setClientSocket(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        /**
         * Отправка данных клиенту (Запись данных в поток)
         *
         * @throws IOException смотри описание {@link IOException}
         */
        @Subscribe private void sendData(ChangePeopleEvent handler) throws IOException {
            if (getClientSocket().isClosed() || !getClientSocket().isConnected()) return;

            String json = new Gson().toJson(handler);
            byte[] data = new byte[Integer.BYTES + json.length()];
            byte[] headerSize = Ints.toByteArray(json.length());

            arraycopy(headerSize, 0, data, 0, headerSize.length);
            arraycopy(json.getBytes(), 0, data, headerSize.length, json.getBytes().length);

            bos.write(data);
            bos.flush();
        }

        /**
         * Чтение данных из потока
         *
         * @throws IOException смотри описание {@link IOException}
         */
        private void readData() throws IOException {
            if (bis.available() == 0) return; // Проверяем наличие байт на чтение
            if (bis.read(headerData) != headerData.length) return; // Считываем первые 4 байта
            int sizeBuffer = Ints.fromByteArray(headerData); // Получаем размер пакета
            byte resultsBuffer[] = new byte[sizeBuffer]; // Буфер данных

            do {
                int r = bis.read(resultsBuffer); // количество прочитанных байт
                // Если количество прочитанных байт меньше, чем размер пакета, то продолжаем считывание
                if (r < sizeBuffer) continue;
                // Интерпретируем байты
                final String data = new String(resultsBuffer, 0, r);
                if (data.contains("start")) {
                    Moving moving = new Moving();
                    moving.start();
                }
                break;
            } while (true);
        }

        @Override public void run() {
            log.info("Started a new thread for client");
            while (!clientSocket.isClosed() && clientSocket.isConnected()) {
                try {
                    readData();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}