package com.standingcat.event.service;

import com.standingcat.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EventServiceTest {
    @Mock
    private EventRepository eventRepository;
    //unit test for services
    @Test
    void contextLoads() {
    }
}
