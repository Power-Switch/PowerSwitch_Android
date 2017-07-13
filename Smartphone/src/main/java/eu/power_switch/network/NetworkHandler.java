package eu.power_switch.network;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.obj.gateway.Gateway;

/**
 * Created by Markus on 13.07.2017.
 */
public interface NetworkHandler {

    boolean isInternetConnected();

    boolean isWifiConnected();

    boolean isEthernetConnected();

    boolean isGprsConnected();

    boolean isNetworkConnected();

    String getConnectedWifiSSID();

    void send(NetworkResponseCallback responseCallback, ArrayList<NetworkPackage> networkPackages);

    void send(ArrayList<NetworkPackage> networkPackages);

    void send(NetworkResponseCallback responseCallback, NetworkPackage... networkPackages);

    void send(NetworkPackage... networkPackages);

    List<Gateway> searchGateways();

}
