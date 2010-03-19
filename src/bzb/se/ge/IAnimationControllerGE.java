package bzb.se.ge  ;

import com4j.*;

/**
 * IAnimationControllerGE Interface
 */
@IID("{BE5E5F15-8EC4-4DCC-B48D-9957D2DE4D05}")
public interface IAnimationControllerGE extends Com4jObject {
    /**
     * method Play
     */
    @VTID(7)
    void play();

    /**
     * method Pause
     */
    @VTID(8)
    void pause();

    /**
     * property SliderTimeInterval
     */
    @VTID(9)
    bzb.se.ge.ITimeIntervalGE sliderTimeInterval();

    /**
     * property CurrentTimeInterval
     */
    @VTID(10)
    bzb.se.ge.ITimeIntervalGE currentTimeInterval();

    /**
     * property CurrentTimeInterval
     */
    @VTID(11)
    void currentTimeInterval(
        bzb.se.ge.ITimeIntervalGE pInterval);

}
