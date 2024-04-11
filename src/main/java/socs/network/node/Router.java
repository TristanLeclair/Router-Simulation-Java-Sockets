package socs.network.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.SOSPFPacket;
import socs.network.util.Configuration;

public class Router {

  protected LinkStateDatabase lsd;
  RouterDescription rd = new RouterDescription();
  ServerThread serverSocket;
  // assuming that all routers are with 4 ports
  LinkDB ports = new LinkDB(4);
  private String _commands = null;
  private boolean started;

  public Router(Configuration config) {
    started = false;
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    rd.processPortNumber = Short.parseShort(config.getString("socs.network.router.port"));
    lsd = new LinkStateDatabase(rd);
  }

  private void sendLSAToNeighbor(Link link, LSA lsa) {
    SOSPFPacket lsaUpdate = new SOSPFPacket();
    lsaUpdate.srcProcessIP = rd.simulatedIPAddress;
    lsaUpdate.srcProcessPort = rd.processPortNumber;
    lsaUpdate.srcIP = rd.simulatedIPAddress;
    lsaUpdate.dstIP = link.router2.simulatedIPAddress;
    lsaUpdate.sospfType = 1;
    lsaUpdate.routerID = rd.simulatedIPAddress;
    lsaUpdate.neighborID = link.router2.simulatedIPAddress;
    // Add all LSAs from the DB to the packet Vector
    lsaUpdate.lsa = lsa;

    try (Socket socket = new Socket(link.router2.processIPAddress, link.router2.processPortNumber);
         ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
      out.writeObject(lsaUpdate);
      out.flush();
    } catch (IOException e) {
      System.err.println("Failed to send LSA update to " + link.router2.simulatedIPAddress);
      e.printStackTrace();
    }
  }

  private void printSetState(String id, RouterStatus status) {
    System.out.printf("set %s STATE to %s%n", id, status.toString());
  }

  private void printRecHello(String id) {
    System.out.printf("received HELLO from %s;%n", id);
  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
          break;
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
              cmdLine[3]);
        } else if (command.equals("start")) {
          processStart();
        } else if (command.equals("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
              cmdLine[3]);
        } else if (command.equals("neighbors")) {
          // output neighbors
          processNeighbors();
        } else {
          // invalid command
          System.out.println("Invalid command, please select one of: ");
          System.out.println(getCommands());
          // NOTE: i dont think we break here, we'll just keep prompting, at least for
          // testing purposes i think it works better
          // break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {
    String shortestPath = lsd.getShortestPath(destinationIP);
    System.out.println(shortestPath);
  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {
    if (!ports.removeLink(portNumber)) {
      System.out.println("Error disconnecting from link");
      return;
    }
    // TODO: resync lsd
    // also remove thread from pool (that will eventually exist)
  }

  /**
   * attach the link to the remote router, which is identified by the given
   * simulated ip;
   * to establish the connection via socket, you need to indentify the process IP
   * and process Port;
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort,
                             String simulatedIP) {
    // TODO: establish link without sync
    if (!requestHandler()) {
      System.out.println("Router full");
      return;
    }

    if (!attachToRouter(processIP, processPort, simulatedIP)) {
      System.out.println("Error adding link to router");
      // TODO: figure out if we need to do something if router is full (think we
      // print)
    }
  }

  private boolean attachToRouter(String processIP, short processPort, String simulatedIP) {
    return ports.addLink(processIP, processPort, simulatedIP, rd);
  }

  /**
   * process request from the remote router.
   * For example: when router2 tries to attach router1. Router1 can decide whether
   * it will accept this request.
   * The intuition is that if router2 is an unknown/anomaly router, it is always
   * safe to reject the attached request from router2.
   */
  private boolean requestHandler() {
    return ports.canAddLink();
    // if (!ports.canAddLink()) {
    // return false;
    // }
    //
    // Scanner scanner = new Scanner(System.in);
    // String response = "";
    //
    // do {
    // System.out.println("Do you accept this request? (Y/N)");
    //
    // response = scanner.nextLine().trim().toUpperCase();
    //
    // if ("Y".equals(response)) {
    // System.out.println("You accepted this attach request;");
    // scanner.close();
    // return true;
    // } else if ("N".equals(response)) {
    // System.out.println("You rejected the attach request;");
    // scanner.close();
    // return false;
    // } else {
    // System.out.println("Invalid input. Please enter Y or N.");
    // }
    //
    // } while (!response.equals("Y") && !response.equals("N"));
    //
    // scanner.close();
    // return false;
  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {
    // Create serverSocket
    try {
      serverSocket = new ServerThread(rd.processPortNumber);
      serverSocket.start();
    } catch (NumberFormatException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    sendHellosToNeighbors();

    sendLSAToNeighbors();

    started = true;
  }

  private void sendLSAToNeighbors() {
    LSA lsa = new LSA();
    lsa.lsaSeqNumber = ports.getLsaSeqNumber().get();
    LinkedList<LinkDescription> list = new LinkedList<>();
    for (Link link : ports) {
      LinkDescription ld = new LinkDescription();
      ld.portNum = link.router2.processPortNumber;
      ld.linkID = link.router2.simulatedIPAddress;
      list.add(ld);
    }
    lsa.links = list;
    lsa.linkStateID = rd.simulatedIPAddress;

    for (Link link : ports) {
      sendLSAToNeighbor(link, lsa);
    }
  }

  private void sendHellosToNeighbors() {
    LinkedList<Thread> threads = new LinkedList<>();
    for (Link link : ports) {
      InitHandler initHandler = new InitHandler(link);
      initHandler.start();
      threads.add(initHandler);
    }

    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * attach the link to the remote router, which is identified by the given
   * simulated ip;
   * to establish the connection via socket, you need to indentify the process IP
   * and process Port;
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,
                              String simulatedIP) {
    if (!started) {
      System.out.println("Need to start router first!");
      return;
    }

    if (!requestHandler()) {
      System.out.println("Router full");
      return;
    }

    if (!attachToRouter(processIP, processPort, simulatedIP)) {
      System.out.println("Error adding link to router");
      return;
    }
    Optional<Link> link = ports.findLink(processPort);
    if (link.isEmpty()) {
      System.out.println("Error adding link to router");
      return;
    }
    InitHandler initHandler = new InitHandler(link.get());
    initHandler.start();
    try {
      initHandler.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    sendLSAToNeighbors();
  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
    System.out.println(ports.toString());
  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {

  }

  private String getCommands() {
    if (_commands != null) {
      return _commands;
    }

    String[] commands = new String[] {
        "`attach [Process IP] [Process Port] [IP Address]`",
        "`connect [Process IP] [Process Port] [IP Address]`",
        "`disconnect [Port Number]`",
        "`detect [IP Address]`",
        "`neighbors`",
        "`quit`",
    };
    StringBuilder sb = new StringBuilder();
    Arrays.stream(commands)
        .forEach(x -> sb.append("- ").append(x).append(System.getProperty("line.separator")));
    _commands = sb.toString();

    return _commands;
  }

  private class ServerThread extends Thread {
    private final short port;

    public ServerThread(short port) {
      this.port = port;
    }

    @Override
    public void run() {
      try (ServerSocket serverSocket = new ServerSocket(port)) {
        System.out.println("Server listening on port " + port);

        while (!serverSocket.isClosed()) {
          Socket socket = serverSocket.accept();
          new ClientHandler(socket).start();
        }
      } catch (IOException ex) {
        System.err.println("Server exception: " + ex.getMessage());
        ex.printStackTrace();
        System.exit(0);
      }
    }

  }

  private class ClientHandler extends Thread {
    private final Socket socket;

    public ClientHandler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try {
        ObjectInputStream inFromClient = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream outToClient = new ObjectOutputStream(socket.getOutputStream());
        SOSPFPacket packet = (SOSPFPacket) inFromClient.readObject();

        if (packet.sospfType == 0) {
          processHello(packet, inFromClient, outToClient);
        } else if (packet.sospfType == 1) {
          processLSAUpdate(packet);
        }

        socket.close();

      } catch (IOException | ClassNotFoundException ex) {
        System.err.println("Server exception: " + ex.getMessage());
        ex.printStackTrace();
        System.exit(0);
      } finally {
        try {
          socket.close();
        } catch (IOException e) {
          System.err.println("Failed to close socket: " + e.getMessage());
        }
      }
    }

    private void processHello(SOSPFPacket packet, ObjectInputStream in, ObjectOutputStream out)
        throws IOException, ClassNotFoundException {
      printRecHello(packet.neighborID);

      Optional<Link> link = ports.findLink(packet.srcProcessPort);
      if (link.isPresent()) {
        return;
      }

      if (!ports.addLink(packet.srcIP, packet.srcProcessPort, packet.neighborID, rd)) {
        return;
      }
      printSetState(packet.neighborID, RouterStatus.INIT);

      SOSPFPacket hello = SOSPFPacket.createHello(rd.processPortNumber, rd.simulatedIPAddress,
          packet.neighborID, rd.simulatedIPAddress);
      out.writeObject(hello);
      out.flush();

      SOSPFPacket handshake = (SOSPFPacket) in.readObject();
      printRecHello(handshake.neighborID);

      if (!ports.setLinkToTwoWay(packet.srcProcessPort)) {
        System.out.println("Error");
        System.exit(1);
      }
      printSetState(packet.neighborID, RouterStatus.TWO_WAY);
    }

    private void processLSAUpdate(SOSPFPacket packet) throws IOException {
      System.out.println("Received LSA update");
      if (rd.simulatedIPAddress.equals(packet.srcIP)) {
        return;
      }
      lsd.syncLinkStateDatabase(packet.lsa);

      // Synchronize on links from LinkDB?????

      // Send LSA update to neighbors
      for (Link link : ports) {
        // Don't send to the neighbor that sent the LSA
        if (link.router2.simulatedIPAddress.equals(packet.srcIP)) {
          continue;
        }
        sendLSAToNeighbor(link);
      }
    }
  }

  private class InitHandler extends Thread {
    private final Link link;

    private InitHandler(Link link) {
      this.link = link;
    }

    @Override
    public void run() {
      try (Socket socket = new Socket("localhost", link.router2.processPortNumber)) {

        SOSPFPacket hello = SOSPFPacket.createHello(rd.processPortNumber, rd.simulatedIPAddress,
            link.router2.simulatedIPAddress, rd.simulatedIPAddress);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(hello);
        out.flush();

        SOSPFPacket response = (SOSPFPacket) in.readObject();
        printRecHello(response.neighborID);

        if (!ports.setLinkToTwoWay(response.srcProcessPort)) {
          return;
        }
        printSetState(response.neighborID, RouterStatus.TWO_WAY);

        out.writeObject(hello);
        out.flush();

        out.close();
        in.close();

      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        System.err.println("Wrong response from server");
      }
    }
  }

}
