package eu.power_switch.shared;

/**
 * Brand constants
 * <p>
 * Created by Markus on 06.07.2017.
 */
public enum Brand {
    BAT("BAT"), BRENNENSTUHL("Brennenstuhl"), ELRO("Elro"), HAMA("Hama"), INTERTECHNO("Intertechno"), INTERTEK("Intertek"), MUMBI("Mumbi"), POLLIN_ELECTRONIC(
            "Pollin Electronic"), REV("REV"), ROHRMOTOR24("Rohrmotor 24"), UNITEC("Unitec"), UNIVERSAL("Universal"), VIVANCO("Vivanco");

    private String name;

    Brand(String name) {
        this.name = name;
    }

    /**
     * Get enum from string representation
     *
     * @param name name of enum
     *
     * @return enum
     */
    public static Brand getEnum(String name) {
        for (Brand v : values()) {
            if (v.toString()
                    .equalsIgnoreCase(name)) {
                return v;
            }
        }

        return valueOf(name);
    }

    /**
     * Get Name of this Model
     *
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (name != null) {
            return getName();
        } else {
            return super.toString();
        }
    }
}
