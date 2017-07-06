package eu.power_switch.gui.dialog.configuration.holder;

import android.text.TextUtils;

import java.util.List;

import eu.power_switch.gui.dialog.configuration.ConfigurationHolder;
import eu.power_switch.obj.Room;
import eu.power_switch.obj.Scene;
import eu.power_switch.obj.SceneItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by Markus on 03.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SceneConfigurationHolder extends ConfigurationHolder {

    private Scene scene;

    private String name;

    private List<Room> checkedReceivers;

    private List<SceneItem> sceneItems;

    @Override
    public boolean isValid() {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        if (checkedReceivers == null || checkedReceivers.isEmpty()) {
            return false;
        }

        if (sceneItems == null || sceneItems.isEmpty()) {
            return false;
        }

        return true;
    }

}
