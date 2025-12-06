package dev.razafindratelo.misinformation.mail;

import dev.razafindratelo.misinformation.InfraGenerated;
import jakarta.mail.internet.InternetAddress;
import java.io.File;
import java.util.List;

@InfraGenerated
public record Email(
    InternetAddress to,
    List<InternetAddress> cc,
    List<InternetAddress> bcc,
    String subject,
    String htmlBody,
    List<File> attachments) {}
