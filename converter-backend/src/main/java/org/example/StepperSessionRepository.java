package org.example;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for entities related to stepper sessions.
 */
public interface StepperSessionRepository extends JpaRepository<StepperSession, Long> {
}
