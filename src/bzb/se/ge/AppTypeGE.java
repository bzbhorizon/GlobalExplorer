package bzb.se.ge  ;

import com4j.*;

public enum AppTypeGE implements ComEnum {
    EnterpriseClientGE(0),
    ProGE(1),
    PlusGE(2),
    FreeGE(5),
    UnknownGE(255),
    ;

    private final int value;
    AppTypeGE(int value) { this.value=value; }
    public int comEnumValue() { return value; }
}
