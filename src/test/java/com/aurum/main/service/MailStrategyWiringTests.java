package com.aurum.main.service;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MailStrategyWiringTests {
    @Test void localProfileInjectsSmtpThroughStrategy() {
        assertProfile("local");
    }

    @Test void productionProfileInjectsSmtpThroughStrategy() {
        assertProfile("production");
    }

    @Test void defaultProfileInjectsSmtpThroughStrategy() {
        assertProfile();
    }

    private void assertProfile(String... profiles) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles(profiles);
            context.registerBean(JavaMailSender.class, () -> mock(JavaMailSender.class));
            context.registerBean(OtpService.class, () -> mock(OtpService.class));
            context.register(SmtpMailService.class, HomeService.class);
            context.refresh();

            assertEquals(1, context.getBeansOfType(MailStrategy.class).size());
            var strategy = context.getBean(MailStrategy.class);
            assertInstanceOf(SmtpMailService.class, strategy);
            assertSame(strategy, context.getBean(HomeService.class).getMailStrategy());
        }
    }
}
