package com.aurum.main.service;

import com.aurum.main.dto.requests.CompleteInviteRegistrationRequest;
import com.aurum.main.dto.requests.InvitationTokenRequest;
import com.aurum.main.dto.requests.InviteEmployeeRequest;
import com.aurum.main.dto.responses.AuthResponse;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.exception.BadInvitationRequestException;
import com.aurum.main.exception.InvitationTokenExpiredException;
import com.aurum.main.exception.InvitationTokenNotFoundException;
import com.aurum.main.model.Employee;
import com.aurum.main.model.Invitation;
import com.aurum.main.repository.EmployeeRepository;
import com.aurum.main.repository.InvitationRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Data
public class EmployeeService {
    private final InvitationRepository invitationRepository;
    private final MailService mailService;
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${spring.mail.username}")
    private String ownerMail;

    public GenericResponse inviteEmployee(InviteEmployeeRequest request) {
        boolean alreadyMember = this.employeeRepository.existsByEmail(request.email());

        if (alreadyMember) {
            throw new BadInvitationRequestException("Invited user is already a member of the company");
        }

        if (request.role() == Employee.EmployeeRole.OWNER) {
            throw new BadInvitationRequestException("Invited user cannot be owner");
        }

        boolean alreadyInvited = invitationRepository.existsByEmail(request.email());

        if (alreadyInvited) {
            throw new BadInvitationRequestException("This user already has pending invitation");
        }

        Invitation invitation = new Invitation();

        invitation.setEmail(request.email());
        String token = UUID.randomUUID().toString();
        invitation.setToken(token);
        invitation.setType(request.type());
        invitation.setSalary(request.salary());
        invitation.setRole(request.role());
        invitation.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        invitationRepository.save(invitation);

        String link = "http://localhost:4200/staff-register?token=" + token;
        mailService.formatAndSend(
                ownerMail,
                request.email(),
                "Aurum Employee Invitation",
                "You've been invited to join Aurum team! Please follow the link to proceed: "
                + link
        );

        return new GenericResponse(200, "Invited the employee successfully!");
    }

    public GenericResponse validateInvitation(InvitationTokenRequest request) {
        Invitation suggestedInvitation = invitationRepository.findByToken(request.token())
                .orElseThrow(() -> new InvitationTokenNotFoundException("Invitation token not found"));

        if (suggestedInvitation.getExpiresAt().isBefore(Instant.now())) {
            throw new InvitationTokenExpiredException("Invitation expired");
        }

        if (employeeRepository.existsByEmail(suggestedInvitation.getEmail())) {
            throw new BadInvitationRequestException("Invitation cannot be accepted");
        }

        return new GenericResponse(200, "Invitation validated successfully!");
    }

    public AuthResponse completeInviteRegistration(CompleteInviteRegistrationRequest request) {
        Invitation invitation = invitationRepository.findByToken(request.token())
                .orElseThrow(() -> new InvitationTokenNotFoundException("Invitation token not found"));

        Employee employee = new Employee();
        employee.setName(request.name());
        employee.setType(invitation.getType());
        employee.setSalary(invitation.getSalary());
        employee.setEmail(invitation.getEmail());
        employee.setPassword(passwordEncoder.encode(request.password()));
        employee.setRole(invitation.getRole());
        employee.setStatus(Employee.EmployeeStatus.PENDING);

        employee = employeeRepository.save(employee);

        invitationRepository.delete(invitation);

        String token = jwtService.generateEmployeeToken(employee);

        return new AuthResponse(200, token);
    }
}
