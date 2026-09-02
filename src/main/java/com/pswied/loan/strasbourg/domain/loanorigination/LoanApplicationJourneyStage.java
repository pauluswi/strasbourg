package com.pswied.loan.strasbourg.domain.loanorigination;

import java.time.Instant;

public record LoanApplicationJourneyStage(
        String lifecycleStage,
        String eventType,
        String payload,
        Instant occurredAt
) {
}
