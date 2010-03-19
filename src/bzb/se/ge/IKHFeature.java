package bzb.se.ge  ;

import com4j.*;

/**
 * IKHFeature Interface
 */
@IID("{07F46615-1857-40CF-9AA9-872C9858E769}")
public interface IKHFeature extends Com4jObject {
    /**
     * property visibility
     */
    @VTID(7)
    int visibility();

    /**
     * property visibility
     */
    @VTID(8)
    void visibility(
        int pVal);

    /**
     * property hasView
     */
    @VTID(9)
    int hasView();

}
