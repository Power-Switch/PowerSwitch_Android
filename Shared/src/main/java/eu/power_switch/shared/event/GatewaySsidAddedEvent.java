package eu.power_switch.shared.event;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * Created by Markus on 02.07.2017.
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Value
public class GatewaySsidAddedEvent extends EventBusEvent {

    private List<String> ssids;

}
