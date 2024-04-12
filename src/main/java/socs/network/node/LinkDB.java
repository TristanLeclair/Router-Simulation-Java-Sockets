package socs.network.node;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkDB implements Iterable<Link> {
  private final Link[] _links;
  private final int _maxSize;

  public int getCurrentSize() {
    return currentSize;
  }

  private int currentSize;

  public AtomicInteger getLsaSeqNumber() {
    return lsaSeqNumber;
  }

  private AtomicInteger lsaSeqNumber = new AtomicInteger(Integer.MIN_VALUE);

  /**
   * @param size max size of DB
   */
  public LinkDB(int size) {
    _maxSize = size;
    _links = new Link[_maxSize];
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
  public boolean addLink(String processIP, short processPort, String simulatedIP,
      RouterDescription currentRouter) {

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
  private boolean addLink(Link link) {
    if (!canAddLink()) {
      return false;
    }

    for (int i = 0; i < _maxSize; ++i) {
      if (_links[i] == null) {
        _links[i] = link;
      }
    }
    currentSize++;
    lsaSeqNumber.addAndGet(1);
    return true;
  }

  /**
   * @param portNumber port of link to remove from list
   * @return true if link removed
   */
  public boolean removeLink(short portNumber) {
    for (int i = 0; i < _maxSize; ++i) {
      Link link = _links[i];
      if (link.router2.processPortNumber == portNumber) {
        _links[i] = null;
        return true;
      }
    }
    lsaSeqNumber.addAndGet(1);

    return false;
  }

  /**
   * Remove link by index
   *
   * @param index of link (0-_maxSize)
   * @return Link
   */
  public Link removeLinkByIndex(int index) {
    Link link = _links[index];
    _links[index] = null;
    lsaSeqNumber.addAndGet(1);
    return link;
  }

  /**
   * @param portNumber port number to match by
   * @return first link to have port number in list
   */
  public Optional<Link> findLink(short portNumber) {
    return Arrays.stream(_links).filter(x -> x != null && x.router2.processPortNumber == portNumber).findFirst();
  }

  /**
   * @param portNumber of link to change
   * @return true if link was in DB and successfully changed
   */
  public boolean setLinkToTwoWay(short portNumber) {
    Optional<Link> first = Arrays.stream(_links).filter(x -> x.router2.processPortNumber == portNumber).findFirst();
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
    for (Link link : _links) {
      if (link == null) {
        continue;
      }
      sb.append(link.router2.simulatedIPAddress).append(System.getProperty("line.separator"));
    }
    return sb.toString();
  }

  @Override
  public Iterator<Link> iterator() {
    return new Iterator<Link>() {
      private int currentIndex = 0;

      @Override
      public boolean hasNext() {
        // Skips over null elements as the array can have holes in it
        while (currentIndex < _maxSize && _links[currentIndex] == null) {
          currentIndex++;
        }
        return currentIndex < _maxSize;
      }

      @Override
      public Link next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return _links[currentIndex++];
      }
    };
  }
}
