package com.agfa.sample.jee.control;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class IdGeneratorAtomic implements IdGenerator {
    private AtomicLong generator;

    @PostConstruct
    void init() {
        generator = new AtomicLong();
    }

    @Override
    public long generateId() {
        return generator.incrementAndGet();
    }
}
