package protocolsupport;

import java.text.MessageFormat;

public enum Environment {

    OVERWORLD(0), NETHER(-1), THE_END(1);

    private final int dimension;

    Environment(int dimension) {
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public int getPEDimension() {
        return ordinal();
    }

    public static Environment getByDimension(int id) {
        switch (id) {
            case 0:
                return OVERWORLD;
            case 1:
                return THE_END;
            case -1:
                return NETHER;
            default:
                throw new IllegalArgumentException(MessageFormat.format("Uknown dim id {0}", id));

        }
    }

    public static Environment getByPEDimension(int id) {
        return values()[id];
    }

}
