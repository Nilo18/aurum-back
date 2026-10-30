package com.aurum.main.service;

import com.resend.Resend;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class MailStrategyWiringTests {
    @Test void localProfileInjectsSmtpThroughStrategy() {
        assertProfile("local", SmtpMailService.class);
    }

    @Test void productionProfileInjectsResendThroughStrategy() {
        assertProfile("production", ResendMailService.class);
    }

    private void assertProfile(String profile, Class<? extends MailStrategy> implementation) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles(profile);
            context.registerBean(JavaMailSender.class, () -> mock(JavaMailSender.class));
            context.registerBean(Resend.class, () -> mock(Resend.class));
            context.registerBean(OtpService.class, () -> mock(OtpService.class));
            context.register(SmtpMailService.class, ResendMailService.class, HomeService.class);
            context.refresh();

            assertEquals(1, context.getBeansOfType(MailStrategy.class).size());
            var strategy = context.getBean(MailStrategy.class);
            assertInstanceOf(implementation, strategy);
            assertSame(strategy, context.getBean(HomeService.class).getMailStrategy());
        }
    }
}
