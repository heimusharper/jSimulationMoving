/*
 * Copyright (C) 2016 Chirkov Boris <b.v.chirkov@udsu.ru>
 *
 * Project website:       http://eesystem.ru
 * Organization website:  http://rintd.ru
 *
 * --------------------- DO NOT REMOVE THIS NOTICE -----------------------------
 * TCPClient is part of jSimulationMoving.
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

import bus.EBus;
import tools.DangerousFactorOfFireEvent;
import tools.EventHandler;

/**
 * TCP клиент. Ждет внешних команд и сигналы об изменениях показаний сенсоров
 * <p>
 * Created by boris on 29.12.16.
 */
public class TCPClient {

    public TCPClient() {
        EventHandler.registeredOnBus();
        EBus.post(new DangerousFactorOfFireEvent(5));
    }

    // В этом классе инициализируютс события от сети: изменение температуруы,
    // состояние датчика
}
