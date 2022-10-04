package com.sample.airtickets.screen.ticketreservation;

import com.sample.airtickets.app.TicketService;
import com.sample.airtickets.entity.Airport;
import com.sample.airtickets.entity.Flight;
import com.sample.airtickets.entity.Ticket;

import com.sample.airtickets.screen.ticket.ticket.TicketBrowse1;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.DialogOutcome;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.*;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@UiController("TicketReservationScreen")
@UiDescriptor("ticket-reservation-screen.xml")
public class TicketReservationScreen extends Screen {
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private ProgressBar progressBar;
    @Autowired
    private EntityComboBox<Airport> fromAirportField;
    @Autowired
    private DateField<LocalDate> dateField;
    @Autowired
    private Notifications notifications;

    @Autowired
    private TicketService ticketService;
    @Autowired
    private EntityComboBox<Airport> toAirportField;

    @Autowired
    private CollectionContainer<Flight> flightsDc;

    @Autowired
    protected BackgroundWorker backgroundWorker;
    @Autowired
    private GroupTable<Flight> flightsTable;

    @Subscribe("searchBtn")
    public void onSearchBtnClick(Button.ClickEvent event) {
        if (fromAirportField.getValue() == null && toAirportField.getValue() == null && dateField.getValue() == null) {
            notifications.create()
                    .withCaption("Please fill at least one filter field")
                    .show();
            return;
        }

        // flightsDc.setItems(ticketService.searchFlights(fromAirportField.getValue(), toAirportField.getValue(), dateField.getValue()));
        BackgroundTask<Integer, Void> task = new SearchFlightsTask();
        BackgroundTaskHandler taskHandler = backgroundWorker.handle(task);
        taskHandler.execute();
        //flightsDc.setItems(task.getFlightList());

    }

    @Install(to = "flightsTable.linkBtn", subject = "columnGenerator")
    private Component flightsTableLinkBtnColumnGenerator(Flight flight) {
        LinkButton linkButton = uiComponents.create(LinkButton.class);
        linkButton.setCaption("Reserve");
        linkButton.addClickListener(clickEvent -> {
            dialogs.createInputDialog(this).withCaption("Reserve ticket").withParameters(
                            InputParameter.stringParameter("passengerName")
                                    .withCaption("Passenger name")
                                    .withRequired(true),
                            InputParameter.stringParameter("passportNumber")
                                    .withCaption("Passport number")
                                    .withRequired(true),
                            InputParameter.stringParameter("telephone")
                                    .withCaption("Telephone")
                                    .withRequired(true))

                    .withActions(DialogActions.OK_CANCEL)
                    .withCloseListener(closeEvent -> {
                        if (closeEvent.closedWith(DialogOutcome.OK)) {
                            String name = closeEvent.getValue("name");
                            Double quantity = closeEvent.getValue("quantity");
                            //Customer customer = closeEvent.getValue("customer");
                            // Status status = closeEvent.getValue("status");
                            // process entered values...


                            if (!flightsTable.getSelected().isEmpty()) {
                                Flight flightSelected = flightsDc.getItem();
                                Ticket ticket = dataManager.create(Ticket.class);
                                ticket.setFlight(flightSelected);
                                ticket.setPassportNumber(closeEvent.getValue("passengerName"));
                                ticket.setPassengerName(closeEvent.getValue("passportNumber"));
                                ticket.setTelephone(closeEvent.getValue("telephone"));
                                ticketService.saveTicket(ticket);

                                TicketBrowse1 ticketBrowse1 = screenBuilders.screen (this)
                                        .withScreenClass(TicketBrowse1.class)
                                        .withOpenMode(OpenMode.NEW_TAB)
                                        .build();

                                ticketBrowse1.setTicket(ticket);
                                ticketBrowse1.show();
                            }

                        }
                    })
                    .show();
            ;
        });
        return linkButton;
    }

//    @Install(to = "flightsDl", target = Target.DATA_LOADER)
//    private List<Flight> flightsDlLoadDelegate(LoadContext<Flight> loadContext) {
//        return ticketService.searchFlights(fromAirportField.getValue(), toAirportField.getValue(), dateField.getValue());
//    }

    private class SearchFlightsTask extends BackgroundTask<Integer, Void> {
        private List<Flight> flightList;

        public SearchFlightsTask() {
            super(10, TimeUnit.SECONDS, TicketReservationScreen.this);
            //  this.customers = customers;
        }

        @Override
        public Void run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
            taskLifeCycle.publish(0);
            flightList = ticketService.searchFlights(fromAirportField.getValue(), toAirportField.getValue(), dateField.getValue());
            return null;
        }

        @Override
        public void progress(List<Integer> changes) {
            progressBar.setVisible(true);
        }


        @Override
        public void done(Void result) {
            progressBar.setVisible(false);
            flightsDc.setItems(flightList);
        }

    }

}