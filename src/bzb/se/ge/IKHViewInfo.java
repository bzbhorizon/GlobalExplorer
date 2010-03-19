package bzb.se.ge  ;

import com4j.*;

/**
 * IKHViewInfo Interface
 */
@IID("{45F89E39-7A46-4CA4-97E3-8C5AA252531C}")
public interface IKHViewInfo extends Com4jObject {
    /**
     * property latitude
     */
    @VTID(7)
    double latitude();

    /**
     * property latitude
     */
    @VTID(8)
    void latitude(
        double pLat);

    /**
     * property longitude
     */
    @VTID(9)
    double longitude();

    /**
     * property longitude
     */
    @VTID(10)
    void longitude(
        double pLon);

    /**
     * property range
     */
    @VTID(11)
    double range();

    /**
     * property range
     */
    @VTID(12)
    void range(
        double pRange);

    /**
     * property tilt
     */
    @VTID(13)
    double tilt();

    /**
     * property tilt
     */
    @VTID(14)
    void tilt(
        double pTilt);

    /**
     * property azimuth
     */
    @VTID(15)
    double azimuth();

    /**
     * property azimuth
     */
    @VTID(16)
    void azimuth(
        double pAzimuth);

}
