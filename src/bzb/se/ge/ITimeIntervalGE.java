package bzb.se.ge  ;

import com4j.*;

/**
 * ITimeIntervalGE Interface
 */
@IID("{D794FE36-10B1-4E7E-959D-9638794D2A1B}")
public interface ITimeIntervalGE extends Com4jObject {
    /**
     * property BeginTime
     */
    @VTID(7)
    bzb.se.ge.ITimeGE beginTime();

    /**
     * property EndTime
     */
    @VTID(8)
    bzb.se.ge.ITimeGE endTime();

}
