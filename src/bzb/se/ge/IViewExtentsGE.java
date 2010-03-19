package bzb.se.ge  ;

import com4j.*;

/**
 * IViewExtentsGE Interface
 */
@IID("{865AB2C1-38C5-492B-8B71-AC73F5A7A43D}")
public interface IViewExtentsGE extends Com4jObject {
    /**
     * property North
     */
    @VTID(7)
    double north();

    /**
     * property South
     */
    @VTID(8)
    double south();

    /**
     * property East
     */
    @VTID(9)
    double east();

    /**
     * property West
     */
    @VTID(10)
    double west();

}
