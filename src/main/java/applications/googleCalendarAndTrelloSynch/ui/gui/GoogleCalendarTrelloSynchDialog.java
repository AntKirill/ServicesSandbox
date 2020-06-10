package applications.googleCalendarAndTrelloSynch.ui.gui;

import applications.googleCalendarAndTrelloSynch.ApplicationConstants;
import applications.googleCalendarAndTrelloSynch.Configuration;
import applications.googleCalendarAndTrelloSynch.business.GoogleCalendarAndTrelloSynchController;
import applications.googleCalendarAndTrelloSynch.business.GoogleCalendarAndTrelloSynchControllerImpl;
import applications.googleCalendarAndTrelloSynch.database.dao.CompactDao;
import applications.googleCalendarAndTrelloSynch.database.dao.DefaultConfigurationDao;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.zinternaltools.HighlightInformation;
import com.google.api.services.calendar.model.Calendar;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import network.services.google.calendar.GoogleCalendarApiRequestsRunner;
import network.services.trello.TrelloAdvancedRequestsRunner;
import network.services.trello.entities.TrelloBoard;
import network.services.trello.entities.TrelloList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.naming.ConfigurationException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.mockito.Mockito.mock;

public class GoogleCalendarTrelloSynchDialog extends JDialog {
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
    private final GoogleCalendarAndTrelloSynchController myController;
    private final JMenuItem setCurrentSettingsAsDefaultItem;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTable table1;
    private JScrollPane scrollPane;
    private JComboBox<ComboItem<TrelloBoard>> comboBoxTrelloBoards;
    private JComboBox<ComboItem<TrelloList>> comboBoxTrelloLists;
    private JMenuBar menuBar;
    private JPanel loadingPanel;
    private JLabel successLabel;
    private JLabel errorLabel;
    private JRadioButton todayRadioButton;
    private JRadioButton tomorrowRadioButton;
    private JRadioButton otherDateRadioButton;
    private JPanel emptyScreenDatePicker;
    private JPanel cardDatePanel;
    private CardLayout cardLayout;
    private JMenuItem updateInformationItem;
    private DatePicker datePicker;

    public GoogleCalendarTrelloSynchDialog(@Nullable JFrame parent, GoogleCalendarAndTrelloSynchController controller) {
        super(parent);
        myController = controller;
        $$$setupUI$$$();
        setTitle(ApplicationConstants.APPLICATION_NAME);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setLocationRelativeTo(parent);
        final JMenu settingsMenu = new JMenu("Settings");
        final JMenu defaultOptionsSettings = new JMenu("Default options");
        setCurrentSettingsAsDefaultItem = new JMenuItem("Set current options as default");
        defaultOptionsSettings.add(setCurrentSettingsAsDefaultItem);
        updateInformationItem = new JMenuItem("Update information");
        settingsMenu.add(updateInformationItem);
        settingsMenu.add(defaultOptionsSettings);
        menuBar.add(settingsMenu);
        successLabel.setText("<html>\n" +
                "  <head>\n" +
                "    \n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <p style=\"margin-top: 0; color:green\">\n" +
                "      Success!\n" +
                "    </p>\n" +
                "  </body>\n" +
                "</html>\n");
        cardLayout = (CardLayout) loadingPanel.getLayout();
        cardLayout.show(loadingPanel, "emptyScreen");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(todayRadioButton);
        buttonGroup.add(tomorrowRadioButton);
        buttonGroup.add(otherDateRadioButton);
        tomorrowRadioButton.setSelected(true);

        cardDatePanel.add(datePicker, "datePickerScreen");
        CardLayout cardLayoutDatePanel = (CardLayout) cardDatePanel.getLayout();
        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
//        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
//                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        updateInformationItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Updating ...");
        });
        setCurrentSettingsAsDefaultItem.addActionListener(e -> myController.updateDefaultConfiguration(createRequestConfiguration()));
        otherDateRadioButton.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                cardLayoutDatePanel.show(cardDatePanel, "emptyScreen");
            } else if (e.getStateChange() == ItemEvent.SELECTED) {
                cardLayoutDatePanel.show(cardDatePanel, "datePickerScreen");
            }
        });
        comboBoxTrelloBoards.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            ComboItem<TrelloBoard> selectedItem = (ComboItem<TrelloBoard>) comboBoxTrelloBoards.getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            TrelloBoard board = selectedItem.getValue();
            new SwingWorker() {
                private Configuration toSelect;
                private Exception exceptionOccurred;
                private List<TrelloList> trelloListsList;

                @Override
                protected Object doInBackground() {
                    try {
                        toSelect = myController.getConfigurationToSelect();
                        trelloListsList = myController.getAllStoredTrelloListsFromBoard(board);
                    } catch (Exception e) {
                        exceptionOccurred = e;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    if (exceptionOccurred != null) {
                        finishLoadingUi(true, exceptionOccurred.getMessage());
                        return;
                    }
                    comboBoxTrelloLists.removeAllItems();
                    for (TrelloList list : trelloListsList) {
                        comboBoxTrelloLists.addItem(new ComboItem<>(list.getName(), list));
                    }
                    if (toSelect != null) {
                        selectTrelloList(toSelect.getTrelloList());
                    }
                    finishLoadingUi(false, null);
                }
            }.execute();
        });
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                prepareInformation();
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

    public static void main(String[] args) {
        GoogleCalendarTrelloSynchDialog dialog = new GoogleCalendarTrelloSynchDialog(null, new GoogleCalendarAndTrelloSynchControllerStub());
        dialog.prepareAndShow();
        System.exit(0);
    }

    private void disableAll() {
        updateInformationItem.setEnabled(false);
        comboBoxTrelloBoards.setEnabled(false);
        comboBoxTrelloLists.setEnabled(false);
        datePicker.setEnabled(false);
        buttonOK.setEnabled(false);
        buttonCancel.setEnabled(false);
        table1.setEnabled(false);
    }

    private void enableAll() {
        updateInformationItem.setEnabled(true);
        comboBoxTrelloBoards.setEnabled(true);
        comboBoxTrelloLists.setEnabled(true);
        datePicker.setEnabled(true);
        buttonOK.setEnabled(true);
        buttonCancel.setEnabled(true);
        table1.setEnabled(true);
    }

    private void onOK() {
        startLoadingUi();
        final Configuration configuration = createRequestConfiguration();
        new SwingWorker() {
            private Exception exceptionOccurred;
            private IOException networkRequestException;
            private ConfigurationException configurationException;

            @Override
            protected Object doInBackground() {
                try {
                    myController.repostDailyEvents(configuration);
                } catch (RuntimeException e) {
                    exceptionOccurred = e;
                } catch (ConfigurationException e) {
                    exceptionOccurred = e;
                    configurationException = e;
                } catch (IOException e) {
                    exceptionOccurred = e;
                    networkRequestException = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (exceptionOccurred != null) {
                    finishLoadingUi(true, exceptionOccurred.getMessage());
                    return;
                }
                finishLoadingUi(false, null);
            }
        }.execute();
//        dispose();
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected Configuration createRequestConfiguration() {
        final Configuration.ConfigurationBuilder configurationBuilder = new Configuration.ConfigurationBuilder();

        final List<Calendar> calendars = getCalendarsAsList(vector -> (boolean) vector.get(1));
        configurationBuilder.setGoogleCalendars(calendars);

        final TrelloBoard selectedTrelloBoard = ((ComboItem<TrelloBoard>) comboBoxTrelloBoards.getSelectedItem()).getValue();
        configurationBuilder.setTrelloBoard(selectedTrelloBoard);

        final TrelloList selectedTrelloList = ((ComboItem<TrelloList>) comboBoxTrelloLists.getSelectedItem()).getValue();
        configurationBuilder.setTrelloList(selectedTrelloList);

        final Configuration.DateButton dateButtonClicked = getDateButtonPressed();
        configurationBuilder.setDateButtonClicked(dateButtonClicked);

        final LocalDate date;
        if (dateButtonClicked == Configuration.DateButton.TODAY) {
            date = LocalDate.now();
        } else if (dateButtonClicked == Configuration.DateButton.TOMORROW) {
            date = LocalDate.now().plusDays(1);
        } else {
            date = datePicker.getDate();
        }
        configurationBuilder.setDate(date);

        return configurationBuilder.createConfiguration();
    }

    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Calendar> getCalendarsAsList(@NotNull Predicate<Vector> isInclude) {
        final List<Calendar> calendars = new ArrayList<>();
        iterateCalendars(vector -> {
            if (isInclude.test(vector)) {
                calendars.add(((ComboItem<Calendar>) vector.get(0)).getValue());
            }
        });
        return calendars;
    }

    @SuppressWarnings({"rawtypes"})
    private void iterateCalendars(@NotNull Consumer<Vector> consumer) {
        final DefaultTableModel model = (DefaultTableModel) table1.getModel();
        final Vector dataVector = model.getDataVector();
        for (Object o : dataVector) {
            consumer.accept((Vector) o);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void selectCalendars(@NotNull List<Calendar> calendarsToSelect) {
        final Predicate<Vector> isSelect = vector -> {
            for (Calendar calendar : calendarsToSelect) {
                if (calendar.getId().equals(((ComboItem<Calendar>) vector.get(0)).getValue().getId())) {
                    return true;
                }
            }
            return false;
        };
        iterateCalendars(vector -> {
            if (isSelect.test(vector)) {
                vector.set(1, true);
            }
        });
    }

    protected void selectTrelloBoard(@NotNull TrelloBoard toSelect) {
        for (int i = 0; i < comboBoxTrelloBoards.getItemCount(); i++) {
            ComboItem<TrelloBoard> item = comboBoxTrelloBoards.getItemAt(i);
            if (item.getValue().getBoardId().equals(toSelect.getBoardId())) {
                comboBoxTrelloBoards.setSelectedIndex(i);
                return;
            }
        }
    }

    protected void selectTrelloList(@NotNull TrelloList toSelect) {
        for (int i = 0; i < comboBoxTrelloLists.getItemCount(); i++) {
            ComboItem<TrelloList> item = comboBoxTrelloLists.getItemAt(i);
            if (item.getValue().getId().equals(toSelect.getId())) {
                comboBoxTrelloLists.setSelectedIndex(i);
                return;
            }
        }
    }

    protected void selectDateButton(@NotNull Configuration.DateButton selectedButton) {
        if (selectedButton == Configuration.DateButton.TODAY) {
            todayRadioButton.setSelected(true);
        } else if (selectedButton == Configuration.DateButton.TOMORROW) {
            tomorrowRadioButton.setSelected(true);
        } else {
            otherDateRadioButton.setSelected(true);
        }
    }

    private void updateCalendarsTable(List<Calendar> calendarList) {
        DefaultTableModel model = (DefaultTableModel) table1.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        for (Calendar calendar : calendarList) {
            model.addRow(new Object[]{new ComboItem<>(calendar.getSummary(), calendar), false});
        }
    }

    private void updateTrelloBoardsBox(List<TrelloBoard> trelloBoardList) {
        comboBoxTrelloBoards.removeAllItems();
        for (TrelloBoard board : trelloBoardList) {
            comboBoxTrelloBoards.addItem(new ComboItem<>(board.getBoardName(), board));
        }
    }

    protected Configuration.DateButton getDateButtonPressed() {
        if (todayRadioButton.isSelected()) {
            return Configuration.DateButton.TODAY;
        } else if (tomorrowRadioButton.isSelected()) {
            return Configuration.DateButton.TOMORROW;
        }
        return Configuration.DateButton.OTHER_DATE;
    }

    private void startLoadingUi() {
        disableAll();
        cardLayout.show(loadingPanel, "loadingScreen");
    }

    private void finishLoadingUi(boolean isErrorOccurred, @Nullable String errorMessage) {
        enableAll();
        if (!isErrorOccurred) {
            cardLayout.show(loadingPanel, "successScreen");
            return;
        }
        if (errorMessage == null) {
            errorMessage = "";
        }
        errorLabel.setText(ERROR_HTML_TEMPLATE.replace("#MESSAGE_PLACE_HOLDER#", errorMessage));
        cardLayout.show(loadingPanel, "errorScreen");
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    protected void prepareInformation() {
        startLoadingUi();
        new SwingWorker() {
            private Exception exceptionOccurred;
            private List<Calendar> calendarList;
            private List<TrelloBoard> trelloBoardList;
            private Configuration configurationToSelect;

            @Override
            protected Object doInBackground() {
                try {
                    calendarList = myController.getAllStoredGoogleCalendars();
                    if (calendarList.isEmpty()) {
                        myController.updateStorageWithFreshInformation();
                        calendarList = myController.getAllStoredGoogleCalendars();
                    }
                    configurationToSelect = myController.getConfigurationToSelect();
                    trelloBoardList = myController.getAllStoredTrelloBoards();
                } catch (Exception e) {
                    exceptionOccurred = e;
                }
                return null;
            }

            @Override
            protected void done() {
                if (exceptionOccurred != null) {
                    finishLoadingUi(true, exceptionOccurred.getMessage());
                    return;
                }

                updateCalendarsTable(calendarList);
                updateTrelloBoardsBox(trelloBoardList);
                if (configurationToSelect != null) {
                    selectCalendars(configurationToSelect.getGoogleCalendars());
                    selectTrelloBoard(configurationToSelect.getTrelloBoard());
                    selectDateButton(configurationToSelect.getDateButtonClicked());
                }
                finishLoadingUi(false, null);
            }
        }.execute();
    }

    public void prepareAndShow() {
        pack();
        setVisible(true);
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
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, 10));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel3.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel3.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadingPanel = new JPanel();
        loadingPanel.setLayout(new CardLayout(0, 0));
        panel1.add(loadingPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        loadingPanel.add(panel4, "emptyScreen");
        final JLabel label1 = new JLabel();
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setIcon(new ImageIcon(getClass().getResource("/images/loading5.gif")));
        label1.setText("Loading ...");
        loadingPanel.add(label1, "loadingScreen");
        successLabel = new JLabel();
        successLabel.setText("Label");
        successLabel.setVerticalAlignment(1);
        successLabel.setVerticalTextPosition(1);
        loadingPanel.add(successLabel, "successScreen");
        errorLabel = new JLabel();
        errorLabel.setText("Label");
        errorLabel.setVerticalAlignment(1);
        errorLabel.setVerticalTextPosition(1);
        loadingPanel.add(errorLabel, "errorScreen");
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(5, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Trello Board Name:");
        panel5.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxTrelloBoards = new JComboBox();
        panel5.add(comboBoxTrelloBoards, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBoxTrelloLists = new JComboBox();
        panel5.add(comboBoxTrelloLists, new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Trello List:");
        panel5.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        todayRadioButton = new JRadioButton();
        todayRadioButton.setText("Today");
        panel5.add(todayRadioButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tomorrowRadioButton = new JRadioButton();
        tomorrowRadioButton.setText("Tomorrow");
        panel5.add(tomorrowRadioButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        otherDateRadioButton = new JRadioButton();
        otherDateRadioButton.setText("OtherDate");
        panel5.add(otherDateRadioButton, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Date:");
        panel5.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cardDatePanel = new JPanel();
        cardDatePanel.setLayout(new CardLayout(0, 0));
        panel5.add(cardDatePanel, new GridConstraints(4, 0, 1, 4, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        emptyScreenDatePicker = new JPanel();
        emptyScreenDatePicker.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        cardDatePanel.add(emptyScreenDatePicker, "emptyScreen");
        cardDatePanel.add(datePicker, "datePickerScreen");
        final JLabel label5 = new JLabel();
        label5.setText("Google calendars:");
        panel5.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scrollPane = new JScrollPane();
        panel5.add(scrollPane, new GridConstraints(0, 1, 1, 3, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 100), null, 0, false));
        scrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
        table1.setAutoResizeMode(3);
        scrollPane.setViewportView(table1);
        menuBar = new JMenuBar();
        contentPane.add(menuBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label5.setLabelFor(scrollPane);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Calendar name", "IsUsing"}, 0) {
            @Override
            public Class getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return String.class;
                }
                return Boolean.class;
            }
        };
        table1 = new JTable(model);
        DatePickerSettings datePickerSettings = new DatePickerSettings();
        datePickerSettings.setAllowKeyboardEditing(false);
        datePickerSettings.setHighlightPolicy(date -> {
            if (date.equals(LocalDate.now())) {
                return new HighlightInformation(Color.orange, null, "Today!");
            }
            return null;
        });
        datePicker = new DatePicker(datePickerSettings);
    }

    private static class GoogleCalendarAndTrelloSynchControllerStub extends GoogleCalendarAndTrelloSynchControllerImpl {

        protected GoogleCalendarAndTrelloSynchControllerStub() {
            super(mock(GoogleCalendarApiRequestsRunner.class),
                    mock(TrelloAdvancedRequestsRunner.class),
                    mock(DefaultConfigurationDao.class),
                    (CompactDao<TrelloList>) mock(CompactDao.class),
                    (CompactDao<TrelloBoard>) mock(CompactDao.class),
                    (CompactDao<Calendar>) mock(CompactDao.class));
        }

        @Override
        public void repostDailyEvents(@NotNull Configuration configuration) throws IOException {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }
    }
}
