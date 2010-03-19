package bzb.se.ge  ;

import com4j.*;

/**
 * ITimeGE Interface
 */
@IID("{E39391AE-51C0-4FBD-9042-F9C5B6094445}")
public interface ITimeGE extends Com4jObject {
    /**
     * property Type
     */
    @VTID(7)
    bzb.se.ge.TimeTypeGE type();

    /**
     * property Type
     */
    @VTID(8)
    void type(
        bzb.se.ge.TimeTypeGE pType);

    /**
     * property Year
     */
    @VTID(9)
    int year();

    /**
     * property Year
     */
    @VTID(10)
    void year(
        int pYear);

    /**
     * property Month
     */
    @VTID(11)
    int month();

    /**
     * property Month
     */
    @VTID(12)
    void month(
        int pMonth);

    /**
     * property Day
     */
    @VTID(13)
    int day();

    /**
     * property Day
     */
    @VTID(14)
    void day(
        int pDay);

    /**
     * property Hour
     */
    @VTID(15)
    int hour();

    /**
     * property Hour
     */
    @VTID(16)
    void hour(
        int pHour);

    /**
     * property Minute
     */
    @VTID(17)
    int minute();

    /**
     * property Minute
     */
    @VTID(18)
    void minute(
        int pMinute);

    /**
     * property Second
     */
    @VTID(19)
    int second();

    /**
     * property Second
     */
    @VTID(20)
    void second(
        int pSecond);

    /**
     * property TimeZone
     */
    @VTID(21)
    double timeZone();

    /**
     * property TimeZone
     */
    @VTID(22)
    void timeZone(
        double pTimeZone);

    /**
     * method Clone
     */
    @VTID(23)
    bzb.se.ge.ITimeGE clone();

    /**
     * method ConvertToZone
     */
    @VTID(24)
    bzb.se.ge.ITimeGE convertToZone(
        double timeZone);

}
