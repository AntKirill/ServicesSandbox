package applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.ui.console;

import applications.Application;
import applications.googleCalendarAndTrelloSynch.ToTrelloEventsReposter.business.GoogleCalendarAndTrelloSynchController;
import org.jetbrains.annotations.NotNull;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Scanner;

public class ConsoleView implements Application.Viewable {
    private final GoogleCalendarAndTrelloSynchController controller;

    public ConsoleView(GoogleCalendarAndTrelloSynchController controller) {
        this.controller = controller;
    }

    @NotNull
    protected String getUserAnswer() {
        Scanner scanner = new Scanner(System.in);
        String nextLine = scanner.nextLine();
        scanner.close();
        return nextLine;
    }

    @Override
    public void show() {
        System.out.print("Load events for tomorrow? [Y/n] ");
        String nextLine = getUserAnswer();
        final LocalDate date;
        if (nextLine.isEmpty() || nextLine.equals("Y")) {
            System.out.println("Loading events for tomorrow ...");
            date = LocalDate.now().plusDays(1);
        } else {
            System.out.println("Loading events for today ...");
            date = LocalDate.now();
        }
        try {
            controller.repostDailyEvents(null);
        } catch (IOException | ConfigurationException e) {
            System.err.println("Sorry, an exception occurred: " + e.getMessage());
        }
    }
}
