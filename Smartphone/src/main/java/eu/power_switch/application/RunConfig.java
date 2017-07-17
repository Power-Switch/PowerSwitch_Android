package eu.power_switch.application;

/**
 * Created by Markus on 17.07.2017.
 */
public class RunConfig {

    public Mode mode;

    public RunConfig(Mode mode) {
        this.mode = mode;
    }


    public enum Mode {
        NORMAL,
        DEMO
    }
}
