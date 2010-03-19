package bzb.se.ge  ;

import com4j.*;

/**
 * IPointOnTerrainGE Interface
 */
@IID("{F4F7B301-7C59-4851-BA97-C51F110B590F}")
public interface IPointOnTerrainGE extends Com4jObject {
    /**
     * property Latitude
     */
    @VTID(7)
    double latitude();

    /**
     * property Longitude
     */
    @VTID(8)
    double longitude();

    /**
     * property Altitude
     */
    @VTID(9)
    double altitude();

    /**
     * ProjectedOntoGlobe
     */
    @VTID(10)
    int projectedOntoGlobe();

    /**
     * ZeroElevationExaggeration
     */
    @VTID(11)
    int zeroElevationExaggeration();

}
