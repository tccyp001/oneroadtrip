package com.oneroadtrip.matcher.resources.samples;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("upload")
public class FileUploadResource {
  private static final Logger LOG = LogManager.getLogger();

  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadFile(@FormDataParam("file") FormDataBodyPart in) {
    LOG.info("xfguo: {}", in);

    // Files.copy(in, upload.toPath());
    return Response.status(200).entity("haha\n").build();
  }

}
