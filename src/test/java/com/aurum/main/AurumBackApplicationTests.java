package com.aurum.main;

import com.aurum.main.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

// Verify real application wiring without requiring a database, SMTP server, or secrets.
@SpringBootTest(properties = {
        "jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "jwt.expiration-ms=3600000",
        "spring.mail.username=test@example.com",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.data.jdbc.autoconfigure.DataJdbcRepositoriesAutoConfiguration,"
                + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
})
@MockitoBean(types = {
        ClientRepository.class, EmployeeRepository.class, EventRepository.class,
        FeedbackRepository.class, FeedbackInvitationRepository.class, InvitationRepository.class,
        MenuItemRepository.class, ProductRepository.class, SupplierRepository.class,
        VehicleRepository.class, SupplierCategoryRepository.class,
        JdbcClient.class, JdbcAggregateTemplate.class
})
class AurumBackApplicationTests {
    @Test void contextLoads() {
    }
}
