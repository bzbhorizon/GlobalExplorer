package bzb.se.ge  ;

import com4j.*;

/**
 * IFeatureGE Interface
 */
@IID("{92547B06-0007-4820-B76A-C84E402CA709}")
public interface IFeatureGE extends Com4jObject {
    /**
     * property Name
     */
    @VTID(7)
    java.lang.String name();

    /**
     * property Visibility
     */
    @VTID(8)
    int visibility();

    /**
     * property Visibility
     */
    @VTID(9)
    void visibility(
        int pVal);

    /**
     * property HasView
     */
    @VTID(10)
    int hasView();

    /**
     * property Highlighted
     */
    @VTID(11)
    int highlighted();

    /**
     * method Highlight
     */
    @VTID(12)
    void highlight();

    /**
     * method GetParent
     */
    @VTID(13)
    bzb.se.ge.IFeatureGE getParent();

    /**
     * method GetChildren
     */
    @VTID(14)
    bzb.se.ge.IFeatureCollectionGE getChildren();

    @VTID(14)
    @ReturnValue(defaultPropertyThrough={bzb.se.ge.IFeatureCollectionGE.class})
    bzb.se.ge.IFeatureGE getChildren(
        int index);

    /**
     * property TimeInterval
     */
    @VTID(15)
    bzb.se.ge.ITimeIntervalGE timeInterval();

}
