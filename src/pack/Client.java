package pack;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JTextField;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This is the client for the user which connects to the server when starting it.
 */
public class Client {

  // Java swing components which are used for the GUI.
  private JFrame frame;
  private JTextArea textArea;
  private JScrollPane scrollPane;
  private JTextField textName;
  private JTextField textMsg;
  private JButton btnSend;

  // This object is connects to the server server and is able to receive and transfer data to it.
  private Socket client;

  // These objects are saving the client in- and outputstreams.
  private PrintWriter writer;
  private BufferedReader reader;

  // The message praefix
  private String praefix = ">";

  // The user should be to receive one warning message whenever he isn't giving the client his name
  // and the content of the message he wants to send.
  private boolean haveSendMsgWarning = false;

  /**
   * Starts the client.
   */
  public static void main(String[] args) {
    try {
      Client client = new Client();
      client.frame.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
      // Whenever the client couldn't connect to the server he receives an error message.
      JOptionPane.showMessageDialog(null, "An error happened while trying to start the client!\n"
          + "The server is most likely offline.", "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Creates the application.
   */
  public Client() {
    if (connectToServer() == true) {
      initialize();
      Thread thread = new Thread(new ServerDataReceiver());
      thread.start();
    }
  }

  /**
   * Initializes the swing contents of the frame.
   */
  private void initialize() {
    frame = new JFrame();
    frame.setResizable(false);
    frame.setTitle("Messenger by Eduard Hermann");
    frame.setIconImage(
        Toolkit.getDefaultToolkit().getImage(Client.class.getResource("/icon/icon.png")));
    frame.setBounds(100, 100, 650, 550);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(null);

    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setFont(new Font("Tahoma", Font.PLAIN, 22));
    scrollPane = new JScrollPane(textArea);
    scrollPane.setBounds(10, 11, 614, 425);
    frame.getContentPane().add(scrollPane);

    textName = new JTextField();
    textName.setText("Unknown");
    textName.setFont(new Font("Tahoma", Font.PLAIN, 22));
    textName.setBounds(10, 447, 152, 53);
    textName.setColumns(10);
    textName.addKeyListener(new KeyAdapter() {
      // The client is able to send a message with his enter key whenever he is in the name text
      // field.
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMsgToServer(textMsg.getText());
        }
      }
    });
    frame.getContentPane().add(textName);

    textMsg = new JTextField();
    textMsg.setFont(new Font("Tahoma", Font.PLAIN, 22));
    textMsg.setColumns(10);
    textMsg.setBounds(172, 447, 335, 53);
    textMsg.addKeyListener(new KeyAdapter() {
      // The client is able to send a message with his enter key whenever he is in the message text
      // field.
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMsgToServer(textMsg.getText());
        }
      }
    });
    frame.getContentPane().add(textMsg);

    btnSend = new JButton("Send");
    btnSend.setFont(new Font("Tahoma", Font.PLAIN, 26));
    btnSend.setBounds(517, 447, 107, 53);
    btnSend.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        sendMsgToServer(textMsg.getText());
      }
    });
    frame.getContentPane().add(btnSend);
  }

  /**
   * This method creates the connection between the client and the server.
   */
  public boolean connectToServer() {
    try {
      client = new Socket("192.168.178.23", 5133);
      // The in- and output streams getting converted into more efficient data processors.
      writer = new PrintWriter(client.getOutputStream());
      reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
      return true;
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Checks if a text field is empty.
   */
  public boolean checkForEmptyTextField(JTextField textField) {
    if (textField.getText().isEmpty() == true) {
      return true;
    }
    return false;
  }

  /**
   * Sends the message to the server which will finally send this message to all clients.
   */
  public void sendMsgToServer(String msg) {
    // Checks if all text fields are filled.
    if ((checkForEmptyTextField(textName) == false) && (checkForEmptyTextField(textMsg) == false)) {
      writer.println(praefix + textName.getText() + ": " + textMsg.getText());
      // This method is sending the printed message to the intended object. In this case this is the
      // server. The client has gotten a reference to this server when it had been initialized.
      writer.flush();
      // Clearing the message text field.
      textMsg.setText("");
      // This statement sets the scrollbar of the scroll pane to the bottom. Therefore, the user is
      // able to consistantly see the message he sent.
      scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    } else {
      Toolkit.getDefaultToolkit().beep();
      if (haveSendMsgWarning == false) {
        haveSendMsgWarning = true;
        JOptionPane.showMessageDialog(null,
            "You need to define your username and the content of the message you want to send!",
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  /**
   * This method appends a message to the text area.
   */
  public void appendMsg(String msg) {
    textArea.append(msg + "\n");
  }

  /**
   * This is the class which reveices the data/messages from the server.
   */
  public class ServerDataReceiver implements Runnable {

    /**
     * This method is consistently running. Besides that the method is reading in all of the input
     * which had been given from the server to the client. We are able to read out the data with the
     * simple "appendMsg" method because the send data had gotten the right format when we first
     * send it to the server.
     */
    @Override
    public void run() {
      String msg;
      try {
        while ((msg = reader.readLine()) != null) {
          appendMsg(msg);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
