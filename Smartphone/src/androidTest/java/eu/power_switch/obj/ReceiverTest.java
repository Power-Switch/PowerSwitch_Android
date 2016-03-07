/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.obj;

import eu.power_switch.ApplicationTest;
import eu.power_switch.obj.gateway.BrematicGWY433;
import eu.power_switch.obj.gateway.ConnAir;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.gateway.ITGW433;

/**
 * Created by Markus on 07.03.2016.
 */
public abstract class ReceiverTest extends ApplicationTest {

    protected static Gateway connAir = new ConnAir((long) 0, true, "Gateway", "v0.1", "address", 0);
    protected static Gateway itgw = new ITGW433((long) 0, true, "Gateway", "v0.1", "address", 0);
    protected static Gateway brematicGWY433 = new BrematicGWY433((long) 0, true, "Gateway", "v0.1", "address", 0);

    protected Gateway[] gateways = new Gateway[]{connAir, itgw, brematicGWY433};

    protected Class<?>[] argClassesGetSignal = new Class[]{Gateway.class, String.class};

}
