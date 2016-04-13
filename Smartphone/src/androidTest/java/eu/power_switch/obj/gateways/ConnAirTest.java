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

package eu.power_switch.obj.gateways;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import eu.power_switch.obj.gateway.ConnAir;

public class ConnAirTest {
    /***
     * This test is not really useful because it test a constant.
     * But we use it to test if our testframework works.
     *
     * @throws Exception
     */
    @Test
    public void testGetModelAsString() throws Exception {
        ConnAir connAir = new ConnAir((long) 0, true, "Gateway", "Firmware", "10.10.10.10", 1234, "wanAddress", 49880, Collections.<String>emptySet());
        String model = connAir.getModel();
        Assert.assertEquals("ConnAir", model);
    }
}