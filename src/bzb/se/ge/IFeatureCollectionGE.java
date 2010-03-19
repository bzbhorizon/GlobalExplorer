package bzb.se.ge  ;

import com4j.*;

/**
 * IFeatureCollectionGE Interface
 */
@IID("{851D25E7-785F-4DB7-95F9-A0EF7E836C44}")
public interface IFeatureCollectionGE extends Com4jObject,Iterable<Com4jObject> {
    /**
     * property _NewEnum
     */
    @VTID(7)
    java.util.Iterator<Com4jObject> iterator();

    /**
     * property Item
     */
    @VTID(8)
    @DefaultMethod
    bzb.se.ge.IFeatureGE item(
        int index);

    /**
     * property Count
     */
    @VTID(9)
    int count();

}
