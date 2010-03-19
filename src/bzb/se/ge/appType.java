package bzb.se.ge  ;

import com4j.*;

public enum appType implements ComEnum {
    GE_EC(0),
    GE_Pro(1),
    GE_Plus(2),
    GE_Free(5),
    GE_Plugin(6),
    UNKNOWN(255),
    ;

    private final int value;
    appType(int value) { this.value=value; }
    public int comEnumValue() { return value; }
}
