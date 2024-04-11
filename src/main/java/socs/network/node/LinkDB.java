package socs.network.node;

import java.util.Arrays;
import java.util.Optional;

public class LinkDB {
  private Link[] links;
  private int _maxSize;
  private int currentSize;

  /**
   * @param size max size of DB
   */
  public LinkDB(int size) {
    _maxSize = size;
    links = new Link[_maxSize];
  }

  /**
   * Try to add link to DB, sets status of link to INIT
   *
   * @param processIP     of new link
   * @param processPort   of new link
   * @param simulatedIP   of new link
   * @param currentRouter description to add to link pair
   * @return true if link was added to DB
   */
  public boolean addLink(String processIP, short processPort, String simulatedIP, RouterDescription currentRouter) {

    RouterDescription newRouter = new RouterDescription();
    newRouter.processPortNumber = processPort;
    newRouter.processIPAddress = processIP;
    newRouter.simulatedIPAddress = simulatedIP;
    newRouter.status = RouterStatus.INIT;

    Link newLink = new Link(currentRouter, newRouter);

    return addLink(newLink);
  }

  public boolean canAddLink() {
    return currentSize < _maxSize;
  }

  /**
   * @param link to add to list
   * @return true if link added
   */
  public boolean addLink(Link link) {
    if (currentSize >= 4) {
      return false;
    }

    for (int i = 0; i < _maxSize; ++i) {
      if (links[i] == null) {
        links[i] = link;
      }
    }
    currentSize++;
    return true;
  }

  /**
   * @param portNumber port of link to remove from list
   * @return true if link removed
   */
  public boolean removeLink(short portNumber) {
    for (int i = 0; i < _maxSize; ++i) {
      Link link = links[i];
      if (link.router2.processPortNumber == portNumber) {
        links[i] = null;
        return true;
      }
    }

    return false;
  }

  /**
   * @param portNumber port number to match by
   * @return first link to have port number in list
   */
  public Optional<Link> findLink(short portNumber) {
    return Arrays.stream(links).filter(x -> x.router2.processPortNumber == portNumber).findFirst();
  }

  /**
   * @param portNumber of link to change
   * @return true if link was in DB and successfully changed
   */
  public boolean setLinkToTwoWay(short portNumber) {
    Optional<Link> first = Arrays.stream(links).filter(x -> x.router2.processPortNumber == portNumber).findFirst();
    if (first.isEmpty()) {
      return false;
    }

    first.get().router2.status = RouterStatus.TWO_WAY;
    return true;
  }

  @Override
  public String toString() {
    if (currentSize == 0) {
      return "Router has no current neighbors";
    }

    StringBuilder sb = new StringBuilder();
    for (Link link : links) {
      if (link == null) {
        continue;
      }
      sb.append(link.router2.simulatedIPAddress).append(System.getProperty("line.separator"));
    }
    return sb.toString();
  }

}
