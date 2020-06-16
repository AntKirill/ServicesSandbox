package applications.googleCalendarAndTrelloSynch.GoogleCalendarEventsShifter;

import applications.googleCalendarAndTrelloSynch.ui.gui.ComboItem;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePicker;
import com.google.api.services.calendar.model.Calendar;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GoogleCalendarEventsShift extends JDialog {
    private final JMenuItem setCurrentSettingsAsDefaultItem;
    private final JMenuItem updateInformationItem;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox<ComboItem<Calendar>> googleCalendarComboBox;
    private JRadioButton nowRadioButton;
    private JRadioButton todaySTimeRadioButton;
    private JRadioButton tomorrowRadioButton;
    private JRadioButton anyDateAndTimeRadioButton;
    private JSpinner spinnerHours;
    private JSpinner spinnerMinutes;
    private JPanel loadingPane;
    private JLabel successScreen;
    private JLabel errorScreen;
    private JMenuBar menuBar;
    private JPanel pickerPanel;
    private TimePicker timePicker;
    private DateTimePicker dateTimePicker;
    private final @NotNull GoogleCalendarEventsShifterController myController;
    private static final Logger LOGGER = Logger.getLogger(GoogleCalendarEventsShift.class);

    private final List<JComponent> clickableComponents = new ArrayList<>();

    private static final String SUCCESS_HTML = "<html>\n" +
            "  <head>\n" +
            "    \n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <p style=\"margin-top: 0; color:green\">\n" +
            "      Success!\n" +
            "    </p>\n" +
            "  </body>\n" +
            "</html>\n";

    private static final String ERROR_HTML_TEMPLATE = "<html>\n" +
            "  <head>\n" +
            "    \n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <p style=\"margin-top: 0; color:red\">\n" +
            "      Error: #MESSAGE_PLACE_HOLDER#\n" +
            "    </p>\n" +
            "  </body>\n" +
            "</html>\n";

    public GoogleCalendarEventsShift(@NotNull GoogleCalendarEventsShifterController controller, JFrame parent) {
        super(parent);
        $$$setupUI$$$();
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.myController = controller;
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(nowRadioButton);
        buttonGroup.add(todaySTimeRadioButton);
        buttonGroup.add(tomorrowRadioButton);
        buttonGroup.add(anyDateAndTimeRadioButton);

        final JMenu settingsMenu = new JMenu("Settings");
        final JMenu defaultOptionsSettings = new JMenu("Default options");
        setCurrentSettingsAsDefaultItem = new JMenuItem("Set current options as default");
        defaultOptionsSettings.add(setCurrentSettingsAsDefaultItem);
        updateInformationItem = new JMenuItem("Update information");
        settingsMenu.add(updateInformationItem);
        settingsMenu.add(defaultOptionsSettings);
        menuBar.add(settingsMenu);

        CardLayout loadingLayoutPanel = (CardLayout) loadingPane.getLayout();
        CardLayout pickerLayoutPanel = (CardLayout) pickerPanel.getLayout();

        loadingLayoutPanel.show(loadingPane, "emptyScreen");

        successScreen.setText(SUCCESS_HTML);

        clickableComponents.add(setCurrentSettingsAsDefaultItem);
        clickableComponents.add(updateInformationItem);
        clickableComponents.add(buttonOK);
        clickableComponents.add(spinnerHours);
        clickableComponents.add(spinnerMinutes);
        clickableComponents.add(googleCalendarComboBox);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        todaySTimeRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pickerPanel.setVisible(true);
                pickerLayoutPanel.show(pickerPanel, "timePickerScreen");
            }
        });
        anyDateAndTimeRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pickerPanel.setVisible(true);
                pickerLayoutPanel.show(pickerPanel, "dateTimePickerScreen");
            }
        });
        ItemListener hidePickerPanelListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                pickerPanel.setVisible(false);
            }
        };
        nowRadioButton.addItemListener(hidePickerPanelListener);
        tomorrowRadioButton.addItemListener(hidePickerPanelListener);

        updateInformationItem.addActionListener(e -> onUpdate());
        setCurrentSettingsAsDefaultItem.addActionListener(e -> onUpdateDefaultConfigs());
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                pickerPanel.setVisible(false);
                var calendars = controller.getLocalStoredGoogleCalendars();
                if (calendars.isEmpty()) {
                    onUpdate();
                } else {
                    setLocalStoredFieldValues();
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
    }

    private void onUpdateDefaultConfigs() {
        final GoogleCalendarShifterConfiguration googleCalendarShifterConfiguration = collectConfigurations();
        if (googleCalendarShifterConfiguration == null) {
            JOptionPane.showMessageDialog(this, "Current configurations are invalid", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        myController.updateDefaultConfiguration(googleCalendarShifterConfiguration);
    }

    private void onUpdate() {
        showUiLoading();
        new SwingWorker() {
            private String message;

            @Override
            protected Object doInBackground() {
                try {
                    myController.updateLocalStoredInformation();
                } catch (IOException ioException) {
                    message = "Error during internet request: " + message;
                    LOGGER.error(message, ioException);
                } catch (Exception e) {
                    message = "Internal error";
                    LOGGER.error(message, e);
                }
                return null;
            }

            @Override
            protected void done() {
                setLocalStoredFieldValues();
                finishUiLoading(message);
            }
        }.execute();
    }

    private void selectGoogleCalendar(Calendar toSelect) {
        for (int i = 0; i < googleCalendarComboBox.getItemCount(); i++) {
            ComboItem<Calendar> comboItem = googleCalendarComboBox.getItemAt(i);
            if (comboItem.getValue().getId().equals(toSelect.getId())) {
                googleCalendarComboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void setLocalStoredFieldValues() {
        var calendars = myController.getLocalStoredGoogleCalendars();
        googleCalendarComboBox.removeAllItems();
        calendars.forEach(calendar -> googleCalendarComboBox.addItem(new ComboItem<>(calendar.getSummary(), calendar)));
        selectDefaultValues();
    }

    private void selectDefaultValues() {
        final GoogleCalendarShifterConfiguration defaultConfiguration = myController.getDefaultConfiguration();
        if (defaultConfiguration == null) {
            return;
        }
        selectGoogleCalendar(defaultConfiguration.getCalendar());
        spinnerMinutes.setValue(defaultConfiguration.getMinutesShift().intValue());
        spinnerHours.setValue(defaultConfiguration.getHoursShift().intValue());
        switch (defaultConfiguration.getSelectedRadioButton()) {
            case NOW:
                nowRadioButton.setSelected(true);
                break;
            case TOMORROW:
                tomorrowRadioButton.setSelected(true);
                break;
            case TODAY_AT_TIME:
                todaySTimeRadioButton.setSelected(true);
                break;
            case ANY_DATE_TIME:
                anyDateAndTimeRadioButton.setSelected(true);
                break;
        }
    }

    private void showUiLoading() {
        clickableComponents.forEach(component -> component.setEnabled(false));
        CardLayout loadingLayoutPanel = (CardLayout) loadingPane.getLayout();
        loadingLayoutPanel.show(loadingPane, "loadingScreen");
    }

    private void finishUiLoading(String errorMessage) {
        clickableComponents.forEach(component -> component.setEnabled(true));
        CardLayout loadingLayoutPanel = (CardLayout) loadingPane.getLayout();
        if (errorMessage != null) {
            errorScreen.setText(ERROR_HTML_TEMPLATE.replace("#MESSAGE_PLACE_HOLDER#", errorMessage));
            loadingLayoutPanel.show(loadingPane, "errorScreen");
            return;
        }
        loadingLayoutPanel.show(loadingPane, "successScreen");
    }

    @Nullable
    private GoogleCalendarShifterConfiguration collectConfigurations() {
        var builder = new GoogleCalendarShifterConfiguration.GoogleCalendarShifterConfigurationBuilder();
        final Object selectedCalendar = googleCalendarComboBox.getSelectedItem();
        if (selectedCalendar == null) {
            return null;
        }
        GoogleCalendarShifterConfiguration.SelectedRadioButton button = null;
        LocalDateTime localDateTime = null;
        if (nowRadioButton.isSelected()) {
            button = GoogleCalendarShifterConfiguration.SelectedRadioButton.NOW;
            localDateTime = LocalDateTime.now();
        } else if (todaySTimeRadioButton.isSelected()) {
            button = GoogleCalendarShifterConfiguration.SelectedRadioButton.TODAY_AT_TIME;
            localDateTime = LocalDateTime.of(LocalDate.now(), timePicker.getTime());
        } else if (tomorrowRadioButton.isSelected()) {
            button = GoogleCalendarShifterConfiguration.SelectedRadioButton.TOMORROW;
            localDateTime = LocalDate.now().plusDays(1).atStartOfDay();
        } else if (anyDateAndTimeRadioButton.isSelected()) {
            button = GoogleCalendarShifterConfiguration.SelectedRadioButton.ANY_DATE_TIME;
            localDateTime = dateTimePicker.getDateTimePermissive();
        }
        assert (button != null) && (localDateTime != null);
        return builder.setCalendar(((ComboItem<Calendar>) selectedCalendar).getValue())
                .setSelectedRadioButton(button)
                .setDateTime(localDateTime)
                .setHoursShift(((Integer) spinnerHours.getValue()).shortValue())
                .setMinutesShift(((Integer) spinnerMinutes.getValue()).shortValue())
                .createGoogleCalendarShifterConfiguration();
    }

    private void onOK() {
        final GoogleCalendarShifterConfiguration googleCalendarShifterConfiguration = collectConfigurations();
        if (googleCalendarShifterConfiguration == null) {
            JOptionPane.showMessageDialog(this, "Please pick a calendar", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        showUiLoading();
        new SwingWorker() {
            private String message;

            @Override
            protected Object doInBackground() {
                try {
                    myController.shiftGoogleCalendarEvents(googleCalendarShifterConfiguration);
                } catch (IOException e) {
                    message = "Error during api request: " + e.getMessage();
                    LOGGER.error(message, e);
                } catch (Exception e) {
                    message = "Internal error";
                    LOGGER.error(message, e);
                }
                return null;
            }

            @Override
            protected void done() {
                finishUiLoading(message);
            }
        }.execute();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

//    public static void main(String[] args) {
//        GoogleCalendarEventsShift dialog = new GoogleCalendarEventsShift();
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
//    }

    private void createUIComponents() {
        SpinnerNumberModel hoursModel = new SpinnerNumberModel(0, -24, 24, 1);
        SpinnerNumberModel minutesModel = new SpinnerNumberModel(0, -60, 60, 15);
        spinnerHours = new JSpinner(hoursModel);
        spinnerMinutes = new JSpinner(minutesModel);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0,
                false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel3.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel3.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        loadingPane = new JPanel();
        loadingPane.setLayout(new CardLayout(0, 0));
        panel4.add(loadingPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setIcon(new ImageIcon(getClass().getResource("/images/loading5.gif")));
        label1.setText("Loading ...");
        loadingPane.add(label1, "loadingScreen");
        successScreen = new JLabel();
        successScreen.setHorizontalAlignment(10);
        successScreen.setHorizontalTextPosition(2);
        successScreen.setText("");
        successScreen.setVerticalAlignment(1);
        loadingPane.add(successScreen, "successScreen");
        errorScreen = new JLabel();
        errorScreen.setText("");
        errorScreen.setVerticalAlignment(1);
        errorScreen.setVerticalTextPosition(0);
        loadingPane.add(errorScreen, "errorScreen");
        final JLabel label2 = new JLabel();
        label2.setText("");
        loadingPane.add(label2, "emptyScreen");
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(210,
                41), null, 0, false));
        googleCalendarComboBox = new JComboBox();
        panel5.add(googleCalendarComboBox, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Google calendar:");
        panel5.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nowRadioButton = new JRadioButton();
        nowRadioButton.setText("Now");
        panel5.add(nowRadioButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        todaySTimeRadioButton = new JRadioButton();
        todaySTimeRadioButton.setText("Today's time");
        panel5.add(todaySTimeRadioButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tomorrowRadioButton = new JRadioButton();
        tomorrowRadioButton.setText("Tomorrow");
        panel5.add(tomorrowRadioButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Shift from:");
        panel5.add(label4, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        anyDateAndTimeRadioButton = new JRadioButton();
        anyDateAndTimeRadioButton.setText("Any date and time");
        panel5.add(anyDateAndTimeRadioButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Hours:");
        panel5.add(label5, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel5.add(spinnerHours, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Minutes:");
        panel5.add(label6, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel5.add(spinnerMinutes, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Shift on:");
        panel5.add(label7, new GridConstraints(4, 0, 2, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pickerPanel = new JPanel();
        pickerPanel.setLayout(new CardLayout(0, 0));
        panel5.add(pickerPanel, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        pickerPanel.add(panel6, "timePickerScreen");
        timePicker = new TimePicker();
        timePicker.setText("");
        panel6.add(timePicker, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        final JLabel label8 = new JLabel();
        label8.setText("Pick a time:");
        panel6.add(label8, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(20, 18), null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        pickerPanel.add(panel7, "dateTimePickerScreen");
        final JLabel label9 = new JLabel();
        label9.setText("Pick date and time:");
        panel7.add(label9, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dateTimePicker = new DateTimePicker();
        panel7.add(dateTimePicker, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 4,
                false));
        menuBar = new JMenuBar();
        contentPane.add(menuBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        label3.setLabelFor(googleCalendarComboBox);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
