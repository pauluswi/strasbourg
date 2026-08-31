package com.pswied.loan.strasbourg.interfaces.rest.outbox;

import com.pswied.loan.strasbourg.application.outbox.OutboxEventStorePort;
import com.pswied.loan.strasbourg.application.outbox.OutboxPublisherService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/outbox")
@Produces(MediaType.APPLICATION_JSON)
public class OutboxResource {

    private final OutboxPublisherService outboxPublisherService;
    private final OutboxEventStorePort outboxEventStorePort;

    @Inject
    public OutboxResource(
            OutboxPublisherService outboxPublisherService,
            OutboxEventStorePort outboxEventStorePort
    ) {
        this.outboxPublisherService = outboxPublisherService;
        this.outboxEventStorePort = outboxEventStorePort;
    }

    @POST
    @Path("/publish")
    public OutboxPublishResponse publish() {
        int published = outboxPublisherService.publishPendingEvents();
        int remainingPending = outboxEventStorePort.findPendingEvents().size();
        return new OutboxPublishResponse(published, remainingPending);
    }

    public record OutboxPublishResponse(int publishedEvents, int pendingEvents) {
    }
}
