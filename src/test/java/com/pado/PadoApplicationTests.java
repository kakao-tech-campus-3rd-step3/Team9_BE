package com.pado;

import com.pado.domain.auth.infra.mail.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class PadoApplicationTests {

    @MockitoBean
    private MailClient mailClient;

    @Test
    void contextLoads() {

    }

}
