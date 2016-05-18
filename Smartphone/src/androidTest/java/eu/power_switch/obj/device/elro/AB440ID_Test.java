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

package eu.power_switch.obj.device.elro;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;

import eu.power_switch.R;
import eu.power_switch.obj.ReceiverTest;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.obj.receiver.device.elro.AB440ID;

/**
 * Created by Markus on 08.08.2015.
 */
public class AB440ID_Test extends ReceiverTest {

    private static AB440ID receiver;

    @Test
    public void testCodeGeneration00000000() throws Exception {
        LinkedList<Boolean> dips = new LinkedList<>();
        dips.add(false); // 1
        dips.add(false); // 2
        dips.add(false); // 3
        dips.add(false); // 4
        dips.add(false); // 5

        dips.add(false); // 6
        dips.add(false); // 7
        dips.add(false); // 8

        receiver = new AB440ID(getContext(), (long) 0, "Name", dips, (long) 0, new ArrayList<Gateway>());

        Method method = receiver.getClass().getDeclaredMethod("getSignal", argClassesGetSignal);
        method.setAccessible(true);

        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "TXP:0,0,10,5600,350,25,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,3,3,1,1,14;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "TXP:0,0,10,5600,350,25,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,14;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGeneration10000000() throws Exception {
        LinkedList<Boolean> dips = new LinkedList<>();
        dips.add(true); // 1
        dips.add(false); // 2
        dips.add(false); // 3
        dips.add(false); // 4
        dips.add(false); // 5

        dips.add(false); // 6
        dips.add(false); // 7
        dips.add(false); // 8

        receiver = new AB440ID(getContext(), (long) 0, "Name", dips, (long) 0, new ArrayList<Gateway>());

        String methodName = "getSignal";
        Method method = receiver.getClass().getDeclaredMethod(methodName, argClassesGetSignal);
        method.setAccessible(true);
        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "TXP:0,0,10,5600,350,25,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,3,3,1,1,14;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "TXP:0,0,10,5600,350,25,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,14;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGeneration10000100() throws Exception {
        LinkedList<Boolean> dips = new LinkedList<>();
        dips.add(true); // 1
        dips.add(false); // 2
        dips.add(false); // 3
        dips.add(false); // 4
        dips.add(false); // 5

        dips.add(true); // 6
        dips.add(false); // 7
        dips.add(false); // 8

        receiver = new AB440ID(getContext(), (long) 0, "Name", dips, (long) 0, new ArrayList<Gateway>());

        String methodName = "getSignal";
        Method method = receiver.getClass().getDeclaredMethod(methodName, argClassesGetSignal);
        method.setAccessible(true);
        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "TXP:0,0,10,5600,350,25,1,3,1,3,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,3,3,1,1,14;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "TXP:0,0,10,5600,350,25,1,3,1,3,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,3,1,1,3,1,3,1,14;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

    @Test
    public void testCodeGeneration11111111() throws Exception {
        LinkedList<Boolean> dips = new LinkedList<>();
        dips.add(true); // 1
        dips.add(true); // 2
        dips.add(true); // 3
        dips.add(true); // 4
        dips.add(true); // 5

        dips.add(true); // 6
        dips.add(true); // 7
        dips.add(true); // 8

        receiver = new AB440ID(getContext(), (long) 0, "Name", dips, (long) 0, new ArrayList<Gateway>());

        String methodName = "getSignal";
        Method method = receiver.getClass().getDeclaredMethod(methodName, argClassesGetSignal);
        method.setAccessible(true);
        Object[] argObjects = new Object[]{connAir, getContext().getString(R.string.on)};
        String generatedMessage = (String) method.invoke(receiver, argObjects);

        // ON
        String expectedMessage = "TXP:0,0,10,5600,350,25,1,3,1,3,1,3,1,3,1,3,1,3,1,3,3,1,1,3,3,1,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,3,1,1,14;";
        Assert.assertEquals(expectedMessage, generatedMessage);

        argObjects = new Object[]{connAir, getContext().getString(R.string.off)};
        generatedMessage = (String) method.invoke(receiver, argObjects);

        // OFF
        expectedMessage = "TXP:0,0,10,5600,350,25,1,3,1,3,1,3,1,3,1,3,1,3,1,3,3,1,1,3,3,1,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,1,3,3,1,1,3,1,3,1,14;";
        Assert.assertEquals(expectedMessage, generatedMessage);
    }

}
