package bzb.se.ge  ;

import com4j.*;

/**
 * IKHViewExtents Interface
 */
@IID("{D05D6E91-72DA-4654-B8A7-BCBD3B87E3B6}")
public interface IKHViewExtents extends Com4jObject {
    /**
     * property north
     */
    @VTID(7)
    double north();

    /**
     * property south
     */
    @VTID(8)
    double south();

    /**
     * property east
     */
    @VTID(9)
    double east();

    /**
     * property west
     */
    @VTID(10)
    double west();

}
