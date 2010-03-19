package bzb.se.ge  ;

import com4j.*;

/**
 * ICameraInfoGE Interface
 */
@IID("{08D46BCD-AF56-4175-999E-6DDC3771C64E}")
public interface ICameraInfoGE extends Com4jObject {
    /**
     * property FocusPointLatitude
     */
    @VTID(7)
    double focusPointLatitude();

    /**
     * property FocusPointLatitude
     */
    @VTID(8)
    void focusPointLatitude(
        double pLat);

    /**
     * property FocusPointLongitude
     */
    @VTID(9)
    double focusPointLongitude();

    /**
     * property FocusPointLongitude
     */
    @VTID(10)
    void focusPointLongitude(
        double pLon);

    /**
     * property FocusPointAltitude
     */
    @VTID(11)
    double focusPointAltitude();

    /**
     * property FocusPointAltitude
     */
    @VTID(12)
    void focusPointAltitude(
        double pAlt);

    /**
     * property AltitudeModeGE
     */
    @VTID(13)
    bzb.se.ge.AltitudeModeGE focusPointAltitudeMode();

    /**
     * property AltitudeModeGE
     */
    @VTID(14)
    void focusPointAltitudeMode(
        bzb.se.ge.AltitudeModeGE pAltMode);

    /**
     * property Range
     */
    @VTID(15)
    double range();

    /**
     * property Range
     */
    @VTID(16)
    void range(
        double pRange);

    /**
     * property Tilt
     */
    @VTID(17)
    double tilt();

    /**
     * property Tilt
     */
    @VTID(18)
    void tilt(
        double pTilt);

    /**
     * property Azimuth
     */
    @VTID(19)
    double azimuth();

    /**
     * property Azimuth
     */
    @VTID(20)
    void azimuth(
        double pAzimuth);

}
