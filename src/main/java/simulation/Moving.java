/******************************************************************************
 Copyright (C) 2016 Kolodkin Vladimir
 
 Project website:       http://eesystem.ru
 Organization website:  http://rintd.ru

 --------------------- DO NOT REMOVE THIS NOTICE ------------------------------
 This file is part of SimulationMoving.

 SimulationMoving is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 SimulationMoving is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with SimulationMoving. If not, see <http://www.gnu.org/licenses/>.
******************************************************************************/

package simulation;

import json.extendetGeometry.BIMExt;
import json.extendetGeometry.RoomExt;

public class Moving {
    
    private BIMExt bim;
    
    public Moving(BIMExt bim) {
        this.bim = bim;
    }
    
    public void start() {
        for (RoomExt r : bim.getRooms()) {
            System.out.println(r);
        } 
    }

}
