package com.sample.airtickets.screen.ticket;

import io.jmix.ui.screen.*;
import com.sample.airtickets.entity.Ticket;

@UiController("Ticket.edit")
@UiDescriptor("ticket-edit.xml")
@EditedEntityContainer("ticketDc")
public class TicketEdit extends StandardEditor<Ticket> {
}