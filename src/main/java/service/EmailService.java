package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

public class EmailService {

    private String senderEmail;
    private String appPassword;
    private String smtpHost;
    private int smtpPort;

    private static String lastError;
    private static String lastResetToken;
    private static long tokenTimestamp = 0;
    private static final long TOKEN_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes

    public EmailService() {
        loadConfig();
    }

    public static String getLastError() {
        return lastError;
    }

    public static String getLastResetToken() {
        // Check expiration
        if (lastResetToken != null && System.currentTimeMillis() - tokenTimestamp > TOKEN_EXPIRY_MS) {
            lastResetToken = null;
            return null;
        }
        return lastResetToken;
    }

    private void loadConfig() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                System.err.println("⚠ config.properties not found");
                setDefaults();
                return;
            }
            Properties props = new Properties();
            props.load(is);
            senderEmail = props.getProperty("email.username", "vanphuocken@gmail.com");
            appPassword = props.getProperty("email.password", "");
            smtpHost = props.getProperty("email.host", "smtp.gmail.com");
            smtpPort = Integer.parseInt(props.getProperty("email.port", "465"));
        } catch (Exception e) {
            System.err.println("⚠ Error loading config: " + e.getMessage());
            setDefaults();
        }
    }

    private void setDefaults() {
        senderEmail = "vanphuocken@gmail.com";
        appPassword = "";
        smtpHost = "smtp.gmail.com";
        smtpPort = 465;
    }

    public boolean sendPasswordResetEmail(String recipientEmail) {
        lastError = null;

        try {
            String resetToken = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            lastResetToken = resetToken;
            tokenTimestamp = System.currentTimeMillis();

            String subject = "Cinema Ticket Booking - Dat lai mat khau";
            String body = buildResetEmailBody(recipientEmail, resetToken);

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", smtpHost);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(smtpPort));
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.writetimeout", "10000");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, appPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail, "Cinema Ticket Booking"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(body, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("✅ Email sent to: " + recipientEmail + " with token: " + resetToken);
            return true;

        } catch (Exception e) {
            lastError = e.getMessage();
            System.err.println("❌ Failed to send email: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String buildResetEmailBody(String email, String token) {
        return "<div style=\"font-family: Arial, sans-serif; max-width: 500px; margin: auto; "
                + "background: #1a1a2e; color: #eef0f4; padding: 32px; border-radius: 12px;\">"
                + "<h2 style=\"color: #d4a843; text-align: center;\">Cinema Ticket Booking</h2>"
                + "<p>Xin chao,</p>"
                + "<p>Ban da yeu cau dat lai mat khau cho tai khoan: <b>" + email + "</b></p>"
                + "<div style=\"background: #16213e; padding: 16px; border-radius: 8px; "
                + "text-align: center; margin: 20px 0;\">"
                + "<p style=\"color: #8893a7; margin: 0;\">Ma xac nhan cua ban:</p>"
                + "<h1 style=\"color: #f59e0b; letter-spacing: 4px; margin: 8px 0;\">" + token + "</h1>"
                + "</div>"
                + "<p>Ma nay co hieu luc trong <b>15 phut</b>.</p>"
                + "<p style=\"color: #8893a7; font-size: 12px;\">"
                + "Neu ban khong yeu cau dat lai mat khau, vui long bo qua email nay."
                + "</p>"
                + "</div>";
    }
}
