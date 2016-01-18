package com.oneroadtrip.matcher.util;

import com.oneroadtrip.matcher.proto.GuideInfo;
import com.oneroadtrip.matcher.proto.Itinerary;

public class ItineraryUtil {

  public static float getCostUsd(Itinerary itin) {
    return itin.getQuote().getCostUsd();
  }

  public static long getGuideId(GuideInfo guideForWholeTrip) {
    return guideForWholeTrip.getGuideId();
  }

}
