package eu.power_switch.shared.event;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Created by Markus on 02.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Value
public class PermissionChangedEvent extends EventBusEvent {

    /**
     * Permission request Code
     */
    int requestCode;

    /**
     * Array of requested permissions
     */
    String[] permissions;

    /**
     * Result per requested permission
     */
    int[] grantResults;

}
