package jp.co.ricoh.advop.idcardscanprint.logic.smtp;


import jp.co.ricoh.advop.cheetahutil.util.LogC;
import jp.co.ricoh.advop.idcardscanprint.util.Const;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class SMTPManager {
    
    
    public static void SendEmail(String host, String from, String to,final String user, final String password, String port, String subject, String content, String isAuth, String isSSL, String attachFilePath) throws MessagingException,Exception {
        Multipart multiPart;
       // String finalString = "";
        //Properties props = System.getProperties();
        System.setProperty("mail.mime.encodeparameters", "false"); 
        System.setProperty("mail.mime.encodefilename", "true");
        
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();  
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");  
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");  
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");  
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");  
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");  
        CommandMap.setDefaultCommandMap(mc);  
        
        Properties props = new Properties();
        Session session = null;
        props.put("mail.transport.protocol", "smtp");
        LogC.d("ssl :" + isSSL);
        
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", isAuth);
        props.put("mail.smtp.connectiontimeout", 60000);//1 minute
        props.put("mail.smtp.timeout", 60000);
        
        if (isSSL.equalsIgnoreCase(Const.SMTP_OPTION_TRUE)) {
            LogC.d("SendEmail inSSL is true");    
            props.put("mail.smtp.socketFactory.port", port);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");            
            //props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.starttls.enable", "true");
            session = Session.getInstance(props, new Authenticator() {

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, password);
                }

            });
            
        } else {
            LogC.d("SendEmail inSSL is false");
            props.put("mail.smtp.startssl.enable", "false");
            session = Session.getInstance(props, null);
        }
        //props.put("mail.smtp.starttls.enable", "false");

        
        //DataHandler handler = new DataHandler(new ByteArrayDataSource(finalString.getBytes(), "text/plain"));
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
//      message.setDataHandler(handler);
 
        multiPart = new MimeMultipart("mixed");
        LogC.d("attachFile name:" + attachFilePath);
        if(attachFilePath != null) {
            MimeBodyPart attachment01 = createAttachment(attachFilePath);
        
            multiPart.addBodyPart(attachment01);
        }

        ArrayList mailAddress = new ArrayList<String>();
        LogC.e("",to);
        mailAddress.add(to);
        InternetAddress[] toAddress = new InternetAddress[mailAddress.size()];
        for (int i = 0; i < mailAddress.size(); i++) {
            toAddress[i] = new InternetAddress((String) mailAddress.get(i));
        }
        message.setRecipients(Message.RecipientType.TO, toAddress);
        message.setSubject(subject);
        message.setContent(multiPart);
        message.saveChanges();
//      message.setText(content);
//      message.setHeader("Mime-Version", "1.0");
//      message.setHeader("Content-Type", "text/plain; charset=us-ascii");
//      message.setHeader("Content-Transfer-Encoding", "7bit");
        message.setSentDate(new Date());
        Transport transport = session.getTransport("smtp");

        if (isAuth.equalsIgnoreCase(Const.SMTP_OPTION_TRUE)) {
            try {
                transport.connect(host, user, password);                
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();
            } catch (MessagingException e) {
                transport.close();
                throw e;
            } catch (Exception e) {
                transport.close();
                throw e;
            }
            
        } else {
            Transport.send(message);
        }
    }
    
    public static MimeBodyPart createAttachment(String fileName) throws Exception {  
        MimeBodyPart attachmentPart = new MimeBodyPart();  
        FileDataSource fds = new FileDataSource(fileName);  
        attachmentPart.setDataHandler(new DataHandler(fds));  
        attachmentPart.setFileName(fds.getName());  
        return attachmentPart;  
    }
}
