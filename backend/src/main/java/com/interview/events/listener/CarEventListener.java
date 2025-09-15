package com.interview.events.listener;

import com.interview.events.CarCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class CarEventListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCarCreated(CarCreatedEvent event) {
        log.info("Car created successfully: id={}, vin={}", event.id(), event.vin());
    }
}
