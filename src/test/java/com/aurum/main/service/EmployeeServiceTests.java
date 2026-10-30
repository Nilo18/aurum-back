package com.aurum.main.service;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import com.aurum.main.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTests {

    @Mock InvitationRepository invitations;
    @Mock MailService mail;
    @Mock EmployeeRepository employees;
    @Mock org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder;
    @Mock JwtService jwt;
    @Mock SearchStrategy<EmployeeDTO, EmployeeQuery> search;
    @Mock MaskedSearchStrategy<EmployeeDTO, EmployeeQuery> masked;
    @InjectMocks EmployeeService service;

    private InviteEmployeeRequest invite(Employee.EmployeeRole role) {
        return new InviteEmployeeRequest(Employee.EmployeeType.CHEF, BigDecimal.TEN, "staff@example.com", role);
    }

    @Test void cannotInviteExistingEmployee() {
        when(employees.existsByEmail("staff@example.com")).thenReturn(true);
        assertThrows(BadInvitationRequestException.class, () -> service.inviteEmployee(invite(Employee.EmployeeRole.STAFF)));
        verifyNoInteractions(invitations, mail);
    }

    @Test void cannotInviteAnotherOwner() {
        assertThrows(BadInvitationRequestException.class, () -> service.inviteEmployee(invite(Employee.EmployeeRole.OWNER)));
        verifyNoInteractions(invitations, mail);
    }

    @Test void cannotSendDuplicatePendingInvitation() {
        when(invitations.existsByEmail("staff@example.com")).thenReturn(true);
        assertThrows(BadInvitationRequestException.class, () -> service.inviteEmployee(invite(Employee.EmployeeRole.STAFF)));
        verify(invitations, never()).save(any());
        verifyNoInteractions(mail);
    }

    @Test void invitationPersistsRoleSalaryAndExpiringTokenAndEmailsLink() {
        service.setOwnerMail("owner@example.com");
        var request = invite(Employee.EmployeeRole.STAFF);
        service.inviteEmployee(request);
        var captor = org.mockito.ArgumentCaptor.forClass(Invitation.class);
        verify(invitations).save(captor.capture());
        var invitation = captor.getValue();
        assertEquals(request.email(), invitation.getEmail());
        assertEquals(request.role(), invitation.getRole());
        assertEquals(request.type(), invitation.getType());
        assertEquals(request.salary(), invitation.getSalary());
        assertTrue(invitation.getExpiresAt().isAfter(Instant.now().plusSeconds(86_390)));
        assertDoesNotThrow(() -> UUID.fromString(invitation.getToken()));
        verify(mail).formatAndSend(eq("owner@example.com"), eq(request.email()), anyString(), contains(invitation.getToken()));
    }

    @Test void validatesOnlyExistingUnexpiredInvitationsForNonMembers() {
        var request = new InvitationTokenRequest("token");
        assertThrows(InvitationTokenNotFoundException.class, () -> service.validateInvitation(request));
        var invitation = new Invitation();
        invitation.setEmail("staff@example.com");
        invitation.setExpiresAt(Instant.now().minusSeconds(60));
        when(invitations.findByToken("token")).thenReturn(Optional.of(invitation));
        assertThrows(InvitationTokenExpiredException.class, () -> service.validateInvitation(request));
        invitation.setExpiresAt(Instant.now().plusSeconds(60));
        when(employees.existsByEmail(invitation.getEmail())).thenReturn(true);
        assertThrows(BadInvitationRequestException.class, () -> service.validateInvitation(request));
        when(employees.existsByEmail(invitation.getEmail())).thenReturn(false);
        assertEquals(new GenericResponse(200, "Invitation validated successfully!"), service.validateInvitation(request));
    }

    @Test void registrationEncodesPasswordCreatesPendingEmployeeAndConsumesInvitation() {
        var invitation = new Invitation();
        invitation.setEmail("staff@example.com");
        invitation.setRole(Employee.EmployeeRole.STAFF);
        invitation.setType(Employee.EmployeeType.CHEF);
        invitation.setSalary(BigDecimal.TEN);
        when(invitations.findByToken("token")).thenReturn(Optional.of(invitation));
        when(encoder.encode("password")).thenReturn("encoded");
        when(employees.save(any(Employee.class))).thenAnswer(call -> call.getArgument(0));
        when(jwt.generateEmployeeToken(any(Employee.class))).thenReturn("signed-token");
        assertEquals(new AuthResponse(200, "signed-token"), service.completeInviteRegistration(
                new CompleteInviteRegistrationRequest("token", "Alice", "password")));
        verify(employees).save(argThat(employee -> employee.getName().equals("Alice")
                && employee.getPassword().equals("encoded")
                && employee.getEmail().equals(invitation.getEmail())
                && employee.getRole() == Employee.EmployeeRole.STAFF
                && employee.getStatus() == Employee.EmployeeStatus.PENDING));
        verify(invitations).delete(invitation);
    }

    @Test void registrationRequiresExistingInvitation() {
        assertThrows(InvitationTokenNotFoundException.class, () -> service.completeInviteRegistration(
                new CompleteInviteRegistrationRequest("missing", "Alice", "password")));
        verifyNoInteractions(employees, encoder, jwt);
    }

    @Test void demoUsesMaskedSearchAndStaffUsesRegularSearch() {
        var query = new EmployeeQuery();
        var page = new PageResponse<EmployeeDTO>(List.of(), 0, 10, 0);
        when(masked.searchMasked(query)).thenReturn(page);
        assertSame(page, service.getEmployees(query, Employee.EmployeeRole.DEMO));
        verifyNoInteractions(search);
        when(search.search(query)).thenReturn(page);
        assertSame(page, service.getEmployees(query, Employee.EmployeeRole.STAFF));
    }

    @Test void deletionDistinguishesMissingEmployeeOwnerAndSuccessfulDeletion() {
        var request = new EmailRequest("staff@example.com");
        assertThrows(EmployeeNotFoundException.class, () -> service.deleteEmployee(request));
        var owner = new Employee();
        owner.setRole(Employee.EmployeeRole.OWNER);
        when(employees.findByEmail(request.email())).thenReturn(Optional.of(owner));
        assertThrows(OwnerDeletionException.class, () -> service.deleteEmployee(request));
        when(employees.deleteNonOwner(request.email())).thenReturn(1);
        assertEquals(new GenericResponse(200, "Employee was deleted successfully"), service.deleteEmployee(request));
    }

}
