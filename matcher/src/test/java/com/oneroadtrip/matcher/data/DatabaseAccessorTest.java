package com.oneroadtrip.matcher.data;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.oneroadtrip.matcher.handlers.DbTest;

public class DatabaseAccessorTest extends DbTest {
  @Test
  public void test() throws Exception {
    DatabaseAccessor accessor = injector.getInstance(DatabaseAccessor.class);
    accessor.insertOneReservation(1L, 20151225, true, 0L);
    accessor.insertOneReservation(3L, 20151226, true, 0L);
    accessor.insertOneReservation(3L, 20151227, false, 1L);
    accessor.insertOneReservation(4L, 20151227, false, 2L);
    accessor.insertOneReservation(7L, 20151225, true, 0L);

    Assert.assertEquals(accessor.loadGuideToReserveDays(Lists.newArrayList(1L, 3L, 4L, 7L), 2L),
        ImmutableMap.of(1L, ImmutableSet.of(20151225), 3L, ImmutableSet.of(20151226), 4L,
            ImmutableSet.of(20151227), 7L, ImmutableSet.of(20151225)));
  }

}
