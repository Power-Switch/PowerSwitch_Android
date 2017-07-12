package eu.power_switch.network;

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
}
