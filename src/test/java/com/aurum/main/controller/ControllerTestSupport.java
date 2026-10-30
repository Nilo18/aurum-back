package com.aurum.main.controller;

import com.aurum.main.config.SecurityConfig;
import com.aurum.main.exception.GlobalExceptionHandler;
import com.aurum.main.filter.JwtAuthFilter;
import com.aurum.main.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = ControllerTestSupport.Config.class)
public abstract class ControllerTestSupport {
    @Autowired WebApplicationContext context;
    protected MockMvc mvc;
    @Autowired protected AuthService authService;
    @Autowired protected ClientService clientService;
    @Autowired protected DashboardService dashboardService;
    @Autowired protected EmployeeService employeeService;
    @Autowired protected EventService eventService;
    @Autowired protected FeedbackService feedbackService;
    @Autowired protected HomeService homeService;
    @Autowired protected MenuItemService menuItemService;
    @Autowired protected ProductService productService;
    @Autowired protected SupplierService supplierService;
    @Autowired protected VehicleService vehicleService;
    @Autowired protected JwtService jwtService;

    @BeforeEach void configureMvc() {
        reset(authService, clientService, dashboardService, employeeService, eventService, feedbackService, homeService, menuItemService, productService, supplierService, vehicleService, jwtService);
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean(FilterChainProxy.class)).build();
    }

    protected String bearer(String role) {
        String token = "test-token-" + role;
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractRole(token)).thenReturn(role);
        when(jwtService.extractUserId(token)).thenReturn(role.equals("DEMO") ? null : 42L);
        return "Bearer " + token;
    }

    @Configuration
    @EnableWebMvc
    @org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
    @Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class,
            AuthController.class, ClientController.class, DashboardController.class, EmployeeController.class, EventController.class, FeedbackController.class, HomeController.class, MenuItemController.class, ProductController.class, SupplierController.class, VehicleController.class})
    public static class Config {
        @Bean AuthService authService() { return mock(AuthService.class); }
        @Bean ClientService clientService() { return mock(ClientService.class); }
        @Bean DashboardService dashboardService() { return mock(DashboardService.class); }
        @Bean EmployeeService employeeService() { return mock(EmployeeService.class); }
        @Bean EventService eventService() { return mock(EventService.class); }
        @Bean FeedbackService feedbackService() { return mock(FeedbackService.class); }
        @Bean HomeService homeService() { return mock(HomeService.class); }
        @Bean MenuItemService menuItemService() { return mock(MenuItemService.class); }
        @Bean ProductService productService() { return mock(ProductService.class); }
        @Bean SupplierService supplierService() { return mock(SupplierService.class); }
        @Bean VehicleService vehicleService() { return mock(VehicleService.class); }
        @Bean JwtService jwtService() { return mock(JwtService.class); }
    }
}
