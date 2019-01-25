package ui.frame;

import ui.UIIOHandler;

import javax.swing.*;

/**
 *
 * @author roody
 */
public class MainMenuUI extends JFrame implements IGlobalUI{

    private JButton createOfferButton;
    private JPanel mainPanel;
    private JButton offerDemandButton;
    private JButton transactionButton;
    private JLabel welcomeLabel;
    private JLabel yourPointResLabel;
    private JLabel yourPointTxtLabel;
    private JFrame frame;
    private UIIOHandler ioHandler;

    private String connectedUser;

    /**
     * Creates new form MainMenuUI
     */
    public MainMenuUI(String user) {
        connectedUser = user;
        initialisation();
        setPointForActualUser();
    }


    @Override
    public void initialisation() {
        frame = new JFrame("BlablaMove");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel = new javax.swing.JPanel();
        welcomeLabel = new javax.swing.JLabel();
        createOfferButton = new javax.swing.JButton();
        offerDemandButton = new javax.swing.JButton();
        transactionButton = new javax.swing.JButton();
        yourPointTxtLabel = new javax.swing.JLabel();
        yourPointResLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        welcomeLabel.setText("Welcome to Blablamove");

        createOfferButton.setText("Create Offer");
        createOfferButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createOfferButtonActionPerformed(evt);
            }
        });

        offerDemandButton.setText("Offer Demand");
        offerDemandButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                offerDemandButtonActionPerformed(evt);
            }
        });

        transactionButton.setText("Transaction");
        transactionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               transactionButtonActionPerformed(evt);
            }
        });

        yourPointTxtLabel.setText("Your Point");

        yourPointResLabel.setText("5800");
        int pointResSize = yourPointResLabel.getText().length();
        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mainPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(mainPanelLayout.createSequentialGroup()
                                                .addComponent(createOfferButton)
                                                .addGap(38, 38, 38)
                                                .addComponent(offerDemandButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(transactionButton))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(mainPanelLayout.createSequentialGroup()
                                                                .addGap(15, 15, 15)
                                                                .addComponent(yourPointResLabel))
                                                        .addComponent(yourPointTxtLabel))
                                                .addGap(166, 166, 166))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(welcomeLabel)
                                                .addGap(135, 135, 135)))
                                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
                mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mainPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(welcomeLabel)
                                .addGap(18, 18, 18)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(createOfferButton)
                                        .addComponent(offerDemandButton)
                                        .addComponent(transactionButton))
                                .addGap(27, 27, 27)
                                .addComponent(yourPointTxtLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(yourPointResLabel)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(mainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        frame.setContentPane(this.mainPanel);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.pack();
    }

    @Override
    public JPanel getMainPanel() {
        return this.mainPanel;
    }

    @Override
    public boolean curlAction() {
        return false;
    }

    @Override
    public String curlJsonParser() {
        return null;
    }

    private void createOfferButtonActionPerformed(java.awt.event.ActionEvent evt) {
        createOfferButton.setSelected(false);
        frame.dispose();
        new OfferCreationUI(connectedUser);
    }

    private void offerDemandButtonActionPerformed(java.awt.event.ActionEvent evt) {
        offerDemandButton.setSelected(false);
        frame.dispose();
        new OfferDemandUI(connectedUser);
    }

    private void transactionButtonActionPerformed(java.awt.event.ActionEvent evt) {
        transactionButton.setSelected(false);
        frame.dispose();
        new TransactionUI();
    }

    private void setPointForActualUser(){
        //ioHandler.sendToApp("{ event :  , data : { }}");
        //yourPointResLabel.setText("1");
    }
}
