package socs.network.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

import socs.network.util.Configuration;

public class Router {

  protected LinkStateDatabase lsd;

  RouterDescription rd = new RouterDescription();

  // assuming that all routers are with 4 ports
  LinkDB ports = new LinkDB(4);

  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    lsd = new LinkStateDatabase(rd);
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
      return;
    }
    // TODO: resync lsd
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

    boolean successfullyAdded = ports.addLink(processIP, processPort, simulatedIP, rd);
    // TODO: figure out if we need to do something if router is full (think we
    // print)
  }

  /**
   * process request from the remote router.
   * For example: when router2 tries to attach router1. Router1 can decide whether
   * it will accept this request.
   * The intuition is that if router2 is an unknown/anomaly router, it is always
   * safe to reject the attached request from router2.
   */
  private boolean requestHandler() {
    if (!ports.canAddLink()) {
      return false;
    }

    Scanner scanner = new Scanner(System.in);
    String response = "";

    do {
      System.out.println("Do you accept this request? (Y/N)");

      response = scanner.nextLine().trim().toUpperCase();

      if ("Y".equals(response)) {
        System.out.println("You accepted this attach request;");
        scanner.close();
        return true;
      } else if ("N".equals(response)) {
        System.out.println("You rejected the attach request;");
        scanner.close();
        return false;
      } else {
        System.out.println("Invalid input. Please enter Y or N.");
      }

    } while (!response.equals("Y") && !response.equals("N"));

    scanner.close();
    return false;
  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {
    // TODO: establish link without sync
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
          break;
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

}
