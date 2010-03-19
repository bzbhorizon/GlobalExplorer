package bzb.se.ge  ;

import com4j.*;

public enum AltitudeModeGE implements ComEnum {
    RelativeToGroundAltitudeGE(1),
    AbsoluteAltitudeGE(2),
    ;

    private final int value;
    AltitudeModeGE(int value) { this.value=value; }
    public int comEnumValue() { return value; }
}
