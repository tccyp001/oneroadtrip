package com.oneroadtrip.matcher.data;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import com.google.common.base.Charsets;

public interface Curl {

  public String curl(String url) throws IOException;

  public static class CurlImpl implements Curl {
    private static final Logger LOG = LogManager.getLogger();

    public String curl(String url) throws IOException {
      try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse resp = httpclient.execute(httpGet)) {
          if (resp.getStatusLine().getStatusCode() != HttpStatus.OK_200) {
            throw new IOException("Failed in curl " + url);
          }
          LOG.info("xfguo: resp status = {}", resp.getStatusLine());
          HttpEntity entity = resp.getEntity();
          StringWriter writer = new StringWriter();
          IOUtils.copy(entity.getContent(), writer, Charsets.UTF_8);

          return writer.toString();
        }
      }
    }

  }

}
