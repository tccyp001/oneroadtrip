package com.oneroadtrip.matcher.resources.samples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.oneroadtrip.matcher.util.HashUtil;
import com.sun.jersey.multipart.FormDataParam;

// TODO(xfguo): Make this a separate service?
@Path("/file")
public class FileUploadResource {
  private static final Logger LOG = LogManager.getLogger();
  
  private static final String PARAM_NAME = "file";
  // TODO(xfguo): We may need to find a better directory for saving these files.
  private static final String IMAGE_DIRECTORY = "/tmp/data";

  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadFile(@FormDataParam(PARAM_NAME) InputStream in) {
    String randStr = HashUtil.getRandomString();
    File upload = new File(IMAGE_DIRECTORY, randStr);

    try {
      Files.copy(in, upload.toPath());
    } catch (IOException e) {
      LOG.error("Errors in uploading file: {}", upload, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.ok().entity(String.format("{photo_token:\"%s\"}",  randStr)).build();
  }

  @GET
  @Path("/get/{param}")
  @Produces({"image/png", "image/jpeg", "image/gif"})
  public Response getFile(@PathParam("param") String fileName) {
    File fileToSend = new File(IMAGE_DIRECTORY, fileName);
    
    return Response.ok(fileToSend).build(); 
  }
}
