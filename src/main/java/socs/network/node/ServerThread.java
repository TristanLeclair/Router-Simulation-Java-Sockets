package socs.network.node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ServerThread extends Thread {
  private int port;

  public ServerThread(int port) {
    this.port = port;
  }

  @Override
  public void run() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server listening on port " + port);

      while (!serverSocket.isClosed()) {
        Socket socket = serverSocket.accept();
        new ClientHandler(socket);
      }
    } catch (IOException ex) {
      System.err.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(0);
    }
  }

}
