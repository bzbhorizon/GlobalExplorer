package bzb.se.ge  ;

import com4j.*;

public enum TimeTypeGE implements ComEnum {
    TimeNegativeInfinityGE(-1),
    TimeFiniteGE(0),
    TimePositiveInfinityGE(1),
    ;

    private final int value;
    TimeTypeGE(int value) { this.value=value; }
    public int comEnumValue() { return value; }
}
