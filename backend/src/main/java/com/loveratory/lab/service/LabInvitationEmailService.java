package com.loveratory.lab.service;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.lab.entity.LabEntity;
import com.loveratory.lab.entity.LabInvitationEntity;
import com.loveratory.notification.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LabInvitationEmailService {

    private final EmailSenderService emailSenderService;

    @Value("${app.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public void sendInvitationEmail(LabInvitationEntity invitation, LabEntity lab, UserEntity inviter) {
        String subject = "[Loveratory] Lab invitation: " + lab.getName();
        String acceptLink = frontendBaseUrl + "/invite/" + invitation.getToken();
        String body = """
                Hello,

                %s invited you to join the lab "%s".
                Lab code: %s

                Accept invitation:
                %s

                This invitation expires at %s.
                """.formatted(
                inviter.getName(),
                lab.getName(),
                lab.getCode(),
                acceptLink,
                invitation.getExpiresAt());

        try {
            emailSenderService.sendPlainText(invitation.getEmail(), subject, body);
        } catch (Exception exception) {
            log.warn("Failed to send lab invitation email. invitationId: {}, reason: {}",
                    invitation.getId(), exception.getMessage());
        }
    }
}
