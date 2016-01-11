package com.oneroadtrip.tools;

import org.testng.Assert;
import org.testng.annotations.Test;

public class GenerateInsertSqlTest {
  @Test
  public void testGetLongByDigitOnly() {
    Assert.assertEquals(GenerateInsertSql.getLongByDigitOnly("832-613-4629."),
        Long.valueOf(8326134629L));
    Assert.assertEquals(Long.valueOf(3129271259L),
        GenerateInsertSql.getLongByDigitOnly("312.927.1259"));
  }
}
