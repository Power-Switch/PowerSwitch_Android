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
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.log.Log;

/**
 * This Class is responsible for sending NetworkPackages that are queued up to be sent
 * <p/>
 * Created by Markus on 29.10.2015.
 */
public class NetworkPackageQueueHandler extends AsyncTask<Void, Void, Void> {

    public static final Object lock = new Object();
    private Context context;
    private DatagramSocket socket;

    public NetworkPackageQueueHandler(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (true) {
            // start working

            Log.d(this, "start working");

            if (NetworkHandler.networkPackagesQueue.size() > 0) {
                processQueue();
            }

            // queue is empty
            Log.d(this, "queue is empty, wait for notify...");

            // Put Thread asleep and wait for wakeup from NetworkHandler
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
                Log.d(this, "waking up");
            }
        }
    }

    private void processQueue() {
        if (NetworkHandler.isWifiAvailable(context) || NetworkHandler.isGprsAvailable(context)) {

            StatusMessageHandler.showStatusMessage(context, context.getString(R.string
                    .sending), Snackbar.LENGTH_INDEFINITE);

            NetworkPackage networkPackage;
            while (NetworkHandler.networkPackagesQueue.size() > 0) {

                synchronized (NetworkHandler.networkPackagesQueue) {
                    networkPackage = NetworkHandler.networkPackagesQueue.get(0);
                }
                try {
                    send(networkPackage);
                    synchronized (NetworkHandler.networkPackagesQueue) {
                        // remove NetworkPackage from queue
                        NetworkHandler.networkPackagesQueue.remove(0);
                    }

//                    if (NetworkHandler.networkPackagesQueue.size() > 0) {
                    // Wait before sending the next one
                    // TODO: cant really access the next element when using iterator
//                        if (networkPackage.getHost().equals(networkPackages[i + 1].getHost()) && networkPackage
//                                .getPort() == networkPackages[i + 1].getPort()) {
//                            // if same gateway, wait gateway-specific time
//                            Log.d("Waiting Gateway specific time (" + networkPackage.getTimeout() + "ms) " +
//                                    "before sending next signal...");
//                            Thread.sleep(networkPackage.getTimeout());
//                        } else {
                    // else wait for the previous gateway to finish sending the signal
                    Log.d("Waiting for Gateway to finish sending Signal before sending next...");
                    Thread.sleep(1000);
//                        }
//                    }
                } catch (UnknownHostException e) {
                    StatusMessageHandler.showStatusMessage(context, context.getString(R.string
                            .unknown_host), Snackbar.LENGTH_LONG);
                    Log.e("UDP Sender", e);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        Log.e("UDP Sender", e1);
                    }
                } catch (Exception e) {
                    StatusMessageHandler.showStatusMessage(context, context.getString(R.string
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

            // queue worked off
            StatusMessageHandler.showStatusMessage(context, context.getString(R.string.sent), Snackbar.LENGTH_SHORT);
        } else {
            synchronized (NetworkHandler.networkPackagesQueue) {
                // remove all NetworkPackage from queue and abort
                NetworkHandler.networkPackagesQueue.clear();
            }

            StatusMessageHandler.showStatusMessage(context, context.getString(R.string
                    .missing_network_connection), Snackbar.LENGTH_LONG);
        }
    }

    private void send(NetworkPackage networkPackage) throws Exception {
        InetAddress host = InetAddress.getByName(networkPackage.getHost());
        int port = networkPackage.getPort();

        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        socket.connect(host, port);

        byte[] messageBuffer = networkPackage.getMessage().getBytes();
        DatagramPacket messagePacket = new DatagramPacket(messageBuffer, messageBuffer.length, host, port);
        socket.send(messagePacket);

        Log.d("UDP Sender", "Host: " + host.getHostAddress() + ":" + port
                + " Message: \"" + new String(messageBuffer) + "\" sent.");

        socket.disconnect();
        socket.close();
    }
}
