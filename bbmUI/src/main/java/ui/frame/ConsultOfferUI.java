package ui.frame;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 *
 * @author roody
 */
public class ConsultOfferUI extends javax.swing.JFrame implements IGlobalUI {

        private JPanel mainPanel;
        private JFrame frame;
        private ArrayList listOfferId = new ArrayList();
        private JButton cancelButton;
        private String connectedUser;
        private ArrayList<String> data = new ArrayList<String>();
        private ArrayList<JButton> jbuttonList;
        private String response;
        private int click = -1;
        private boolean start = true;
        /**
         * Creates new form ShowOfferUI
         */
    public ConsultOfferUI(String user) {
            this.connectedUser = user;
            this.response = null;
            initialisation();
        }

        private ArrayList getListId(ArrayList<String> getListOffer) {
            ArrayList res = new ArrayList();
            for(int i =0; i<getListOffer.size();i++){
                JsonObject json = new Gson().fromJson(getListOffer.get(i), JsonObject.class);
                res.add(json.get("offerID").toString());
            }
            return res;
        }


        @Override
        public void initialisation() {
            frame = new JFrame("BlablaMove: Offer List Awaiting");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainPanel = new javax.swing.JPanel();
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancelButtonActionPerformed();
                }
            });
            curlAction();
            jbuttonList = new ArrayList<JButton>();
            setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
            if(listOfferId.size() == 0 ){
                mainPanel.add(new javax.swing.JLabel("No offer awaiting"));
            }else {
                for (int i = 0; i < listOfferId.size(); i++) {
                    lineCreation(i);
                    mainPanel.add(new javax.swing.JSeparator());
                }
            }
            mainPanel.add(cancelButton);

            pack();
            frame.setContentPane(this.mainPanel);
            frame.setVisible(true);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.pack();

        }

        private void lineCreation(int i) {
            JsonObject json = new Gson().fromJson(data.get(i), JsonObject.class);
            JPanel linePanel = new JPanel();
            final JButton acceptButton = new JButton("Accept");
            JLabel offerIdTxtLabel = new JLabel("Id :");
            JLabel offerIdResLabel = new JLabel(json.get("offerID").toString()+ "    ");

            jbuttonList.add(acceptButton);
            acceptButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    acceptButtonActionPerformed(evt);
                }

            });

            linePanel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.gridy = 0;
            linePanel.add(offerIdTxtLabel,c);
            c.gridx = 1;
            c.gridy = 0;
            linePanel.add(offerIdResLabel,c);
            c.weightx = 1;
            c.gridx = 4;
            c.gridy = 0;
            linePanel.add(acceptButton,c);




            mainPanel.add(linePanel);
        }


        @Override
        public JPanel getMainPanel() {
            return this.mainPanel;
        }

        @Override
        public boolean curlAction() {
            String url = "http://localhost:8080/BBM/OFFERS";
            try {
                URL object = new URL(url);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                if(this.start){
                    wr.write(curlJsonParser());
                    this.start = false;
                }else{
                    wr.write(curlJsonParserEnd(5));
                }
                wr.flush();

                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    System.out.println( "response is : " + sb.toString());
                    br.close();
                    response = sb.toString();
                    if(sb.toString() == "No offers waiting for confirmation."){

                    }
                    return !("" + sb.toString()).equals("");
                } else {
                    System.out.println( "HTTP result : " + HttpResult);
                    System.out.println( "response is : " + sb.toString());
                    return !("" + sb.toString()).equals("");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }



        @Override
        public String curlJsonParser() {
            String res = "{\"event\": \"consult-awaiting-offers\" ,\"data\": {\"ownerID\": \""+connectedUser +"\"}, \"identification\":{\"userID\":\""+ connectedUser +"\"}}";
            System.out.println("Request : " + res);
            return res;
        }

    public String curlJsonParserEnd(int i) {
        JsonObject json = new Gson().fromJson(data.get(click), JsonObject.class);
        String res = "{\"event\": \"confirm-awaiting-offers\" ,\"data\": {\"transactionID\":\"" + i + "\"}, \"identification\":{\"userID\":\""+ connectedUser +"\"}";
        System.out.println("Request : " + res);
        return res;
    }

        private String[] parseResponse(){
            String[] first = response.split(",");
            String date = first[0].split(":")[1];
            String start = first[1].split(":")[1];
            String end = first[2].split(":")[1];
            String price = first[3].split(":")[1];
            String[] res = {price,date,start,end};
            return res;
        }

        private void cancelButtonActionPerformed() {
            cancelButton.setSelected(false);
            frame.dispose();
            new MainMenuUI(connectedUser);
        }

        private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt){
            int i =0;
            JButton button = jbuttonList.get(i);
            System.out.println(evt.getActionCommand());
            while(evt.getSource() != button) {
                i++;
                button = jbuttonList.get(i);
            }
            button.setSelected(false);
            click = i;
            //if (true) {
            if (curlAction()) {
                //response = "{ \"date\": \"5\", \"startAddress\": \"Nice\", \"endAddress\": \"Sophia\", \"price\": \"10\" }";
                //frame.dispose();
                //new ShowRecapUI(connectedUser,parseResponse());

            } else {
                click = -1;
                JOptionPane.showMessageDialog(frame, "an error occur.");
            }
        }
}
