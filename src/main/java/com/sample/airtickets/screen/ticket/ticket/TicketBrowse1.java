package com.sample.airtickets.screen.ticket.ticket;

import com.google.common.collect.ImmutableMap;
import com.sample.airtickets.app.TicketService;
import groovy.transform.Immutable;
import io.jmix.core.LoadContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.action.Action;
import io.jmix.ui.model.InstanceLoader;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.navigation.UrlIdSerializer;
import io.jmix.ui.navigation.UrlParamsChangedEvent;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.screen.*;
import com.sample.airtickets.entity.Ticket;
import liquibase.precondition.Precondition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@UiController("Ticket.browse1")
@UiDescriptor("ticket-browse-1.xml")
@EditedEntityContainer("ticketDc")
@Route(value = "tickets/view", parentPrefix = "tickets")
public class TicketBrowse1 extends Screen {


    @Autowired
    private TicketService ticketService;
    @Autowired
    private InstanceLoader<Ticket> ticketDl;
    @Autowired
    private UrlRouting urlRouting;

    private Ticket ticket;

    private UUID ticketUUid;

    public void setTicket(Ticket ticket) {

        this.ticket = ticket;
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        if (ticket != null && ticket.getId() != null) {
            String serializeId = UrlIdSerializer.serializeId(ticket.getId());
            if (serializeId != null) {
                urlRouting.replaceState(this, ImmutableMap.of("id", serializeId));
            }
        }
    }

    @Subscribe
    public void onUrlParamsChanged(UrlParamsChangedEvent event) {
        String serializeId = event.getParams().get("id");
        if (serializeId != null) {
            ticketUUid = (UUID) UrlIdSerializer.deserializeId(UUID.class, serializeId);
        }

        //ticketDl.load();
    }


    @Subscribe("windowClose")
    public void onWindowClose(Action.ActionPerformedEvent event) {
        closeWithDefaultAction();
    }

    @Install(to = "ticketDl", target = Target.DATA_LOADER)
    private Ticket ticketDlLoadDelegate(LoadContext<Ticket> loadContext) {
        if (ticketUUid != null) {
            Ticket ticket1 = ticketService.getTicket(ticketUUid);
            return ticket1;
        }
        return ticket;
    }

}