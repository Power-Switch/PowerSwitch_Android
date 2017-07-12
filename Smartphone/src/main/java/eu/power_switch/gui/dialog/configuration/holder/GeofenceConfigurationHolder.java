package eu.power_switch.gui.dialog.configuration.holder;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import eu.power_switch.action.Action;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.dialog.configuration.ConfigurationHolder;
import eu.power_switch.shared.constants.GeofenceConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Markus on 04.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GeofenceConfigurationHolder extends ConfigurationHolder {

    private Geofence geofence;

    private Long apartmentId;

    private String name;

    private LatLng location;

    private double radius = GeofenceConstants.DEFAULT_GEOFENCE_RADIUS;

    private Bitmap snapshot;

    private List<Action> enterActions = new ArrayList<>();

    private List<Action> exitActions = new ArrayList<>();

    @Override
    public boolean isValid() {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        if (radius == -1) {
            return false;
        }

        if (location == null) {
            return false;
        }

        if (snapshot == null) {
            return false;
        }

        if (enterActions == null || exitActions == null) {
            return false;
        }
        if (enterActions.size() == 0 && exitActions.size() == 0) {
            return false;
        }

        return true;
    }
}
