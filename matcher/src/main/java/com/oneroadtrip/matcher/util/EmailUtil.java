package com.oneroadtrip.matcher.util;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
public class EmailUtil {
	public static void sendPwdResetEmail(String token) {
		final String username = "tccyp86@gmail.com";
		final String password = "Cypinto_001";
		
		final String from = "tccyp86@gmail.com";
		final String to = "tccyp86@hotmail.com";
//"service@oneroadtrip.com", "Merryguide123"
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
	      // 获取默认session对象
	      Session session = Session.getInstance(props,new Authenticator(){
		    public PasswordAuthentication getPasswordAuthentication()
		    {
		     return new PasswordAuthentication(username, password); //发件人邮件用户名、密码
		    }
		   });

	 
	      try{
	         // 创建默认的 MimeMessage 对象
	         MimeMessage message = new MimeMessage(session);
	 
	         // Set From: 头部头字段
	         message.setFrom(new InternetAddress(from));
	 
	         // Set To: 头部头字段
	         message.addRecipient(Message.RecipientType.TO,
	                                  new InternetAddress(to));
	 
	         // Set Subject: 头部头字段
	         message.setSubject("This is the Subject Line!");
	 
	         // 设置消息体
	         message.setText("This is actual message" + token);
	 
	         // 发送消息
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	}
}
