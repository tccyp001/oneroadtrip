package com.oneroadtrip.matcher.util;

import com.oneroadtrip.matcher.proto.GuideInfo;
import com.oneroadtrip.matcher.proto.Itinerary;

public class ItineraryUtil {

  public static float getCostUsd(Itinerary itin) {
    return itin.getChooseOneGuideSolution() ?
        itin.getQuoteForOneGuide().getCostUsd() : itin.getQuoteForMultipleGuides().getCostUsd();
  }

  public static long getGuideId(GuideInfo guideForWholeTrip) {
    return guideForWholeTrip.getGuideId();
  }

}
