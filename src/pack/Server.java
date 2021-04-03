package pack;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * This class provides the server which allows clients to communicate between each other. The
 * clients are sending data to the server which will be directly send from the server to the other
 * clients.
 */
public class Server {

  // This is the equivalent to the server.
  private ServerSocket server;

  // This object trys connect to the mySQL database.
  Connection con;
  // Executes mySQL statements.
  Statement stmt;

  // Sets the input parameters for storing the data in the mySQL database.
  private final String sqlInsert = "insert into messanges (id, ip, msg, thedate)";
  // Gives the current size of the mySQL database.
  private final String sqlSize = "select COUNT(*) from messanges";
  // Returns all of the send messages stored in the mySQL database.
  private final String sqlUserText = "select msg from messanges";
  // Returns all of the dates from the send messages which are stored in the mySQL database.
  private final String sqlUserDate = "select thedate from messanges";

  // This object is used to save the client output streams. So that the output from the client can
  // be directly send to all clients by the server.
  private ArrayList<PrintWriter> clientOutputStream;

  // The needed input for creating a connection to the database.
  private final String url = "jdbc:mysql://127.0.0.1:3306/messengerdb";
  private final String user = "eduard";
  private final String password = "E1!2duard";

  private final int SERVER_IMPORTANT = 1;
  private final int SERVER_NORMAL = 0;

  /**
   * Starts the server.
   */
  public static void main(String[] args) {
    Server server = new Server();
    if (server.runServer() == true) {
      server.acceptClients();
    }
  }

  /**
   * Starts the Server/ServerSocket and prints out the current server status.
   */
  public boolean runServer() {
    try {
      // Trys to create a local hosted server on port 5133.
      server = new ServerSocket(5133);
      clientOutputStream = new ArrayList<>();
      printTextToConsole("The server got started!", SERVER_IMPORTANT);
      try {
        // Trys to create a connecntion to the mySQL database.
        con = DriverManager.getConnection(url, user, password);
        // Trys to create a statement objects which will execute mySQL statements.
        stmt = con.createStatement();
        printTextToConsole("The server connected to the database!", SERVER_IMPORTANT);
      } catch (SQLException e) {
        e.printStackTrace();
        printTextToConsole("The server couldn't connect to the database!", SERVER_IMPORTANT);
      }
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      printTextToConsole("The server couldn't be started!", SERVER_IMPORTANT);
      return false;
    }
  }

  /**
   * This method is used to create a consistent connection between the clients, the clients output
   * streams, the mySQL database and the server itself.
   */
  public void acceptClients() {
    while (true) {
      try {
        Socket client = server.accept();
        // Sends the saved date from the mySQL database to the client who is currently connecting to
        // the server.
        sendSavedData(client);
        // The output stream from the client is getting converted into a more efficient data
        // processor. Saving the client output stream in a normal "outputStream" object will be way
        // less efficient because the "outputStream" isn't returning the data in the most useful
        // format. Besides that the "PrintWriter" contains a lot of useful methods to return the
        // data from the client. This also applies to the BufferedReader.
        PrintWriter printWriter = new PrintWriter(client.getOutputStream());
        clientOutputStream.add(printWriter);
        // Creating a thread for every client in order to make them run parallel.
        Thread thread = new Thread(new ClientThread(client));
        thread.start();
        // If all the statements above had happened a client correctly connected to the server.
        printTextToConsole("[" + client.getInetAddress() + "]" + " [connected]", SERVER_IMPORTANT);
      } catch (IOException e) {
        e.printStackTrace();
        printTextToConsole("The server couldn't accept all incoming clients!", SERVER_IMPORTANT);
      }
    }
  }

  /**
   * Prints out red or black messages based on the rarity of the message.
   */
  public void printTextToConsole(String msg, int importance) {
    if (importance == SERVER_NORMAL) {
      System.out.println(msg);
    } else if (importance == SERVER_IMPORTANT) {
      // The error message simulates an important message.
      System.err.println(msg);
    }
  }

  /**
   * This method is reading in all of the saved data from the mySQL database and sends it to the
   * currently connected clients.
   */
  public void sendSavedData(Socket client) {
    try {
      // Receiving the data from the mySQL database.
      ResultSet rs1 = stmt.executeQuery(sqlUserText);
      Statement stmttemp = con.createStatement();
      ResultSet rs2 = stmttemp.executeQuery(sqlUserDate);
      PrintWriter printWriter = new PrintWriter(client.getOutputStream());
      while (rs1.next() && rs2.next()) {
        // Creates the data which is stored in the mySQL database.
        printWriter.println(rs1.getString("msg") + " | " + rs2.getString("thedate"));
        // Sends the data to all clients.
        printWriter.flush();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method iterates throughout the whole output from the clients and sends the messages from
   * every client to all clients available.
   */
  public void sendMessageToAllClients(String msg) {
    Iterator<PrintWriter> it = clientOutputStream.iterator();

    while (it.hasNext()) {
      PrintWriter printWriter = (PrintWriter) it.next();
      printWriter.println(msg);
      // This method is sending all of the received messages to the intended object. In this case
      // the server is sending a message from one client to all the others. With other words all
      // clients are the intended receiver of this message. Therefore everyone will receive a
      // message by calling this method. The server has gotten a reference to all clients when
      // it had been initialized.
      printWriter.flush();
    }
  }

  /**
   * This "Thread"/class is used to process the data from a client in an individual thread. This
   * class send the given data from one client to everyone.
   */
  public class ClientThread implements Runnable {

    // This object is connects to the server server and is able to receive and transfer data to it.
    private Socket client;

    // This object is used to save the client input stream.
    private BufferedReader readerFromClient;

    /**
     * Getting the whole input from the client and processing it in a BufferedReader so that the
     * iteration throughout the whole data is easier and faster.
     */
    private ClientThread(Socket client) {
      try {
        this.client = client;
        readerFromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
      } catch (IOException e) {
        e.printStackTrace();
        printTextToConsole("The ClientThread couldn't be started!", SERVER_IMPORTANT);
      }
    }

    /**
     * Whenever we are creating an instance of this "Thread"/class this method is consistently
     * running until the thread is getting closed. Besides that, the method iterates above all the
     * given input from a client and then sends the input from the one client to every client
     * connected to the server.
     */
    @Override
    public void run() {
      String msg;
      try {
        while ((msg = readerFromClient.readLine()) != null) {
          // Gives the current date in the right format.
          DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
          Date date = new Date();
          try {
            // Getting the size of the database.
            ResultSet rs = stmt.executeQuery(sqlSize);
            if (rs.next()) {
              // Saving the given data in the database.
              stmt.executeUpdate(
                  sqlInsert + " values ('" + rs.getInt(1) + 1 + "', '" + client.getInetAddress()
                      + "', '" + msg + "', '" + dateFormat.format(date) + "')");
            }
          } catch (SQLException e) {
            e.printStackTrace();
            printTextToConsole("The message couldn't be saved in the mySQL database!",
                SERVER_NORMAL);
          }
          sendMessageToAllClients(msg + " | " + dateFormat.format(date));
          printTextToConsole("[" + client.getInetAddress() + "]" + " [" + msg + "]" + " ["
              + dateFormat.format(date) + "]", SERVER_NORMAL);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
