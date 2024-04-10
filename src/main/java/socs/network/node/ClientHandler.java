package socs.network.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import socs.network.message.SOSPFPacket;

class ClientHandler extends Thread {
  private Socket socket;

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try {
      ObjectInputStream inFromClient = new ObjectInputStream(socket.getInputStream());
      SOSPFPacket packet = (SOSPFPacket) inFromClient.readObject();

      if (packet.sospfType == 0) {
        // TODO: process hello
      } else if (packet.sospfType == 1) {
        // TODO: process LSAUpdate
      }

    } catch (IOException | ClassNotFoundException ex) {
      System.err.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(0);
    }
  }
}
