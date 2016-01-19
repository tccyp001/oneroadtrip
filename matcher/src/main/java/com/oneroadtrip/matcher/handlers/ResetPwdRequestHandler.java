package com.oneroadtrip.matcher.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.oneroadtrip.matcher.proto.GuidePlanRequest;
import com.oneroadtrip.matcher.proto.GuidePlanResponse;
import com.oneroadtrip.matcher.proto.Status;
import com.oneroadtrip.matcher.util.ProtoUtil;

public class ResetPwdRequestHandler  implements RequestHandler {
	 private static final Logger LOG = LogManager.getLogger();


	@Override
	public String process(String post) {
		 return "";
	}

	  
}
