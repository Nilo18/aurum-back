package com.aurum.main.service;

import com.aurum.main.dto.EmployeeDTO;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.AuthResponse;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.*;
import com.aurum.main.model.Employee;
import com.aurum.main.model.Invitation;
import com.aurum.main.repository.EmployeeRepository;
import com.aurum.main.repository.InvitationRepository;
import com.aurum.main.repository.MaskedSearchStrategy;
import com.aurum.main.repository.SearchStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Data
public class EmployeeService {
    private final InvitationRepository invitationRepository;
    private final MailStrategy mailStrategy;
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SearchStrategy<EmployeeDTO, EmployeeQuery> employeeStrategy;
    private final MaskedSearchStrategy<EmployeeDTO, EmployeeQuery> maskedEmployeeStrategy;

    @Value("${spring.mail.username}")
    private String ownerMail;

    public GenericResponse inviteEmployee(InviteEmployeeRequest request, HttpServletRequest servlet) {
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

        String origin = servlet.getHeader("Origin");
        String frontendUrl =
                "http://localhost:4200".equals(origin)
                        ? "http://localhost:4200"
                        : "https://aurum-front-sigma.vercel.app";

        String link = frontendUrl + "/staff-register?token=" + token;
        mailStrategy.formatAndSend(
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

        ensureInvitationCanBeAccepted(suggestedInvitation);

        return new GenericResponse(200, "Invitation validated successfully!");
    }

    @Transactional
    public AuthResponse completeInviteRegistration(CompleteInviteRegistrationRequest request) {
        Invitation invitation = invitationRepository.findByTokenForUpdate(request.token())
                .orElseThrow(() -> new InvitationTokenNotFoundException("Invitation token not found"));

        ensureInvitationCanBeAccepted(invitation);

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

    private void ensureInvitationCanBeAccepted(Invitation invitation) {
        if (invitation.getExpiresAt() == null || !invitation.getExpiresAt().isAfter(Instant.now())) {
            throw new InvitationTokenExpiredException("Invitation expired");
        }
        if (employeeRepository.existsByEmail(invitation.getEmail())) {
            throw new BadInvitationRequestException("Invitation cannot be accepted");
        }
    }

    public PageResponse<EmployeeDTO> getEmployees(EmployeeQuery query, Employee.EmployeeRole role) {
        if (role == Employee.EmployeeRole.DEMO) {
            return maskedEmployeeStrategy.searchMasked(query);
        }

        return employeeStrategy.search(query);
    }

    public GenericResponse deleteEmployee(EmailRequest request) {
        int rowsDeleted = employeeRepository.deleteNonOwner(request.email());

        if (rowsDeleted == 0) {
            Employee employee = employeeRepository.findByEmail(request.email())
                    .orElseThrow(() ->
                            new EmployeeNotFoundException("Employee not found"));

            if (employee.getRole() == Employee.EmployeeRole.OWNER) {
                throw new OwnerDeletionException("Owner cannot be deleted");
            }
        }

        return new GenericResponse(200, "Employee was deleted successfully");
    }
}
