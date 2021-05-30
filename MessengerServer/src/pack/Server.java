package pack;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class provides the server which allows clients to communicate between each other. The
 * clients are sending data to the server which will be directly send from the server to the other
 * clients.
 */
public class Server {

  // This is the equivalent to the server.
  private ServerSocket server;

  // This object is used to save the client output streams. So that the output from the client can
  // be directly send to all clients by the server.
  private ArrayList<PrintWriter> clientOutputStream;

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
      server = new ServerSocket(Port);
      clientOutputStream = new ArrayList<>();
      printTextToConsole("The server got started!", SERVER_IMPORTANT);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      printTextToConsole("The server couldn't be started!", SERVER_IMPORTANT);
      return false;
    }
  }

  /**
   * This method is used to create a consistent connection between the clients, the clients output
   * streams and the server itself.
   */
  public void acceptClients() {
    while (true) {
      try {
        Socket client = server.accept();
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
        // If all the statements above had happened a client connected to the server.
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
    private BufferedReader reader;

    /**
     * Getting the whole input from the client and processing it in a BufferedReader so that the
     * iteration throughout the whole data is easier and faster.
     */
    private ClientThread(Socket client) {
      try {
        this.client = client;
        reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
      } catch (IOException e) {
        e.printStackTrace();
        printTextToConsole("One ClientThread couldn't be started!", SERVER_IMPORTANT);
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
        while ((msg = reader.readLine()) != null) {
          sendMessageToAllClients(msg);
          printTextToConsole("[" + client.getInetAddress() + "]" + " [" + msg + "]", SERVER_NORMAL);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
