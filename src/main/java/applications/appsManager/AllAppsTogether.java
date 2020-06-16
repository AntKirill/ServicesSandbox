package applications.appsManager;

import applications.Application;
import applications.ApplicationCreator;
import applications.googleCalendarAndTrelloSynch.ui.gui.ComboItem;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class AllAppsTogether extends JFrame {
    private final @NotNull AppsManagerController myController;
    private JPanel panel1;
    private JComboBox<ComboItem<ApplicationCreator>> comboBox1;
    private JButton executeButton;
    private JMenuBar menubar;
    private JLabel loadingLabel;
    private JPanel loadingPanel;
    private static final Logger LOGGER = Logger.getLogger(AllAppsTogether.class);

    public AllAppsTogether(String header, @NotNull AppsManagerController controller) {
        super(header);
        this.myController = controller;
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        CardLayout cardLayout = (CardLayout) loadingPanel.getLayout();
        cardLayout.next(loadingPanel);
        final JMenu settingsMenu = new JMenu("Settings");
        menubar.add(settingsMenu);
        ApplicationsManager.getAllApplicationFactories().forEach(factory -> comboBox1.addItem(new ComboItem<>(factory.getApplicationName(), factory)));
        final String defaultApplicationName = myController.getDefaultApplicationName();
        if (defaultApplicationName != null) {
            for (int i = 0; i < comboBox1.getItemCount(); i++) {
                ComboItem<ApplicationCreator> applicationCreatorComboItem = comboBox1.getItemAt(i);
                if (applicationCreatorComboItem.getKey().equals(defaultApplicationName)) {
                    comboBox1.setSelectedIndex(i);
                }
            }
        }
        executeButton.addActionListener(e -> onExecute());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void onExecute() {
        CardLayout cardLayout = (CardLayout) loadingPanel.getLayout();
        final ComboItem<ApplicationCreator> selectedItem =
                (ComboItem<ApplicationCreator>) comboBox1.getSelectedItem();
        assert selectedItem != null;
        final ApplicationCreator factory = selectedItem.getValue();
        if (factory == null) {
            JOptionPane.showMessageDialog(panel1, "Please, choose an application.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        myController.updateDefaultApplicationName(factory.getApplicationName());
        executeButton.setEnabled(false);
        cardLayout.next(loadingPanel);
        final JFrame jFrameInstance = this;
        new SwingWorker() {
            private String message = null;
            private Application application;

            @Override
            protected Object doInBackground() {
                try {
                    application = myController.createNewApplication(factory, true, jFrameInstance);
                } catch (GeneralSecurityException ex) {
                    message = "Security exception: " + ex.getMessage();
                    LOGGER.error(message, ex);
                } catch (IOException ex) {
                    message = "Exception during api request: " + ex.getMessage();
                    LOGGER.error(message, ex);
                } catch (RuntimeException e) {
                    message = "Internal error: " + e.getMessage();
                    LOGGER.error(message, e);
                }
                return null;
            }

            @Override
            protected void done() {
                executeButton.setEnabled(true);
                cardLayout.next(loadingPanel);
                if (message != null) {
                    JOptionPane.showMessageDialog(panel1, "Sorry, but an exception occurred: " + message, "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                application.showHandledUi();
            }
        }.execute();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-1));
        panel1.setEnabled(true);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null,
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        menubar = new JMenuBar();
        panel2.add(menubar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 1, new Insets(5, 5, 5, 5), -1, -1));
        panel3.setBackground(new Color(-1));
        panel1.add(panel3, new GridConstraints(1, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        executeButton = new JButton();
        executeButton.setText("Execute");
        panel3.add(executeButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBox1 = new JComboBox();
        panel3.add(comboBox1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        loadingPanel = new JPanel();
        loadingPanel.setLayout(new CardLayout(0, 0));
        loadingPanel.setBackground(new Color(-1));
        loadingPanel.setEnabled(true);
        panel3.add(loadingPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0,
                false));
        loadingLabel = new JLabel();
        loadingLabel.setHorizontalAlignment(0);
        loadingLabel.setIcon(new ImageIcon(getClass().getResource("/images/loading4.gif")));
        loadingLabel.setText("");
        loadingLabel.setVerticalAlignment(1);
        loadingPanel.add(loadingLabel, "Card1");
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.setBackground(new Color(-1));
        loadingPanel.add(panel4, "Card2");
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
