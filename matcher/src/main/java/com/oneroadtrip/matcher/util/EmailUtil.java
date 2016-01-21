package com.oneroadtrip.matcher.util;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;

import javax.activation.*;
public class EmailUtil {
	public static void sendPwdResetEmail(String token, String useremail) {
		HtmlEmail  email = new HtmlEmail ();
		final String username = "service@oneroadtrip.com";
		final String password = "Merryguide123";
		
		final String from = "service@oneroadtrip.com";
		//final String to = "tccyp86@hotmail.com";	
		//TODO  change to useremail in prod
		final String to = "tccyp86@hotmail.com";
		
		email.setHostName("smtp.exmail.qq.com"); // 设置发送端服务器
		email.setAuthentication(username, password); // 用户名和密码
		email.setCharset("UTF-8"); // 设置字符集
		email.setSSL(true); // gmail邮箱必须设置为true
		try {
			email.setFrom(from, "发件人"); // 发件人
			email.addTo(to, "收件人"); // 收件人1
			email.setSubject("Oneroadtrip 密码Reset"); // 主题
			email.setMsg("这里是测试简单邮件内容"); // 发送内容
			String url = "www.oneroadtrip.com/?#/resetToken=" + token;
			String htmlStr = "<html>这里是测试HTML邮件内容---<a href='" + url 
					 + "'>Reset Link</a><br>" + url + "</html>";
			email.setHtmlMsg(htmlStr);
			email.send();
			System.out.println("success!");
		} catch (EmailException e) {
			System.out.println("failure!");
			e.printStackTrace();
		}
	}
}
