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

package eu.power_switch.network;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import eu.power_switch.R;
import eu.power_switch.gui.activity.MainActivity;
import eu.power_switch.log.Log;

/**
 * Background thread used to send receiver actions to gateway
 */
public class UDP extends AsyncTask<NetworkPackage, Void, Void> {

    private Context context;

    public UDP(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(NetworkPackage... networkPackages) {
        MainActivity.sendStatusSnackbarBroadcast(context, context.getString(R.string
                .sending), Snackbar.LENGTH_LONG);
        DatagramSocket socket = null;

        for (int i = 0; i < networkPackages.length; i++) {

            try {
                InetAddress host = InetAddress.getByName(networkPackages[i].getHost());
                int port = networkPackages[i].getPort();

                socket = new DatagramSocket(null);
                socket.setReuseAddress(true);
                socket.connect(host, port);

                byte[] messageBuffer = networkPackages[i].getMessage().getBytes();
                DatagramPacket messagePacket = new DatagramPacket(messageBuffer, messageBuffer.length, host, port);
                socket.send(messagePacket);

                Log.d("UDP Sender", "Host: " + host.getHostAddress() + ":" + port
                        + " Message: \"" + new String(messageBuffer) + "\" sent.");

                socket.disconnect();
                socket.close();

                if (i != networkPackages.length - 1) {
                    // Wait before sending the next one
                    if (networkPackages[i].getHost().equals(networkPackages[i + 1].getHost()) && networkPackages[i]
                            .getPort() == networkPackages[i + 1].getPort()) {
                        // if same gateway, wait gateway-specific time
                        Log.d("Waiting Gateway specific time (" + networkPackages[i].getTimeout() + "ms) " +
                                "before sending next signal...");
                        Thread.sleep(networkPackages[i].getTimeout());
                    } else {
                        // else wait for the previous gateway to finish sending the signal
                        Log.d("Waiting for Gateway to finish sending Signal before sending next...");
                        Thread.sleep(1000);
                    }
                }

                MainActivity.sendStatusSnackbarBroadcast(context, context.getString(R.string.sent), Snackbar.LENGTH_SHORT);
            } catch (UnknownHostException e) {
                MainActivity.sendStatusSnackbarBroadcast(context, context.getString(R.string
                        .unknown_host), Snackbar.LENGTH_LONG);
                Log.e("UDP Sender", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    Log.e("UDP Sender", e1);
                }
            } catch (Exception e) {
                MainActivity.sendStatusSnackbarBroadcast(context, context.getString(R.string
                        .unknown_error), Snackbar.LENGTH_LONG);
                Log.e("UDP Sender: Unknown error while sending message in background:", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    Log.e("UDP Sender", e1);
                }
            } finally {
                if (socket != null) {
                    socket.disconnect();
                    socket.close();
                }
            }
        }
        return null;
    }
}
