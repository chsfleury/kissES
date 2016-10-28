package org.kisses.core.pagination.impl;


import org.kisses.core.pagination.Pageable;

/**
 * @author Charles Fleury
 * @since 01/02/16.
 */
public class PageRequest implements Pageable {

  public static final int MAX_SIZE = 1000;
  public static final Pageable ZERO = new PageRequest();
  public static final Pageable ONE = new PageRequest(0, 1);
  public static final Pageable SCROLL = new PageRequest(0, MAX_SIZE);

  private final int page;
  private final int size;

  private PageRequest() {
    this.page = 0;
    this.size = 0;
  }

  /**
   * Creates a new PageRequest. Pages are zero indexed, thus providing 0 for {@code page} will return
   * the first page.
   *
   * @param page must not be less than zero.
   * @param size must not be less than one.
   */
  public PageRequest(int page, int size) {
    if (page < 0) {
      throw new IllegalArgumentException("Page index must not be less than zero!");
    }
    if (size < 1) {
      throw new IllegalArgumentException("Page size must not be less than one!");
    }
    if(size > MAX_SIZE) {
      throw new IllegalArgumentException("Page size must not be greater than " + MAX_SIZE + ". See scroll requests for big result set.");
    }
    this.page = page;
    this.size = size;
  }

  public int getPageSize() {
    return size;
  }

  public int getPageNumber() {
    return page;
  }

  public int getOffset() {
    return page * size;
  }

  public boolean hasPrevious() {
    return page > 0;
  }

  public Pageable previousOrFirst() {
    return hasPrevious() ? previous() : first();
  }

  public Pageable next() {
    return new PageRequest(getPageNumber() + 1, getPageSize());
  }

  public PageRequest previous() {
    return getPageNumber() == 0 ? this : new PageRequest(getPageNumber() - 1, getPageSize());
  }

  public Pageable first() {
    return new PageRequest(0, getPageSize());
  }
}
