package edu.hubu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class MyProjectBackendApplicationTests {

    @Test
    void contextLoads() {
        System.out.printf(new BCryptPasswordEncoder().encode("123scc"));
    }

}
