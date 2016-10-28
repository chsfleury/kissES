package org.kisses.core.pagination;

import java.util.List;

/**
 * @author Charles Fleury
 * @since 01/02/16.
 */
public interface Page<T> extends Iterable<T> {

  /**
   * Returns the number of total pages.
   *
   * @return the number of total pages
   */
  int getTotalPages();

  /**
   * Returns the total amount of elements.
   *
   * @return the total amount of elements
   */
  long getTotalElements();

  /**
   * Returns the number of the current page. Is always non-negative.
   *
   * @return the number of the current page.
   */
  int getNumber();

  /**
   * Returns the size of the page.
   *
   * @return the size of the page.
   */
  int getSize();

  /**
   * Returns the number of elements currently on this page.
   *
   * @return the number of elements currently on this page.
   */
  int getNumberOfElements();

  /**
   * Returns the page content as {@link List}.
   *
   * @return
   */
  List<T> getContent();

  /**
   * Returns whether the page has content at all.
   *
   * @return
   */
  boolean hasContent();

  /**
   * Returns whether the current page is the first one.
   *
   * @return
   */
  boolean isFirst();

  /**
   * Returns whether the current page is the last one.
   *
   * @return
   */
  boolean isLast();

  /**
   * Returns if there is a next page.
   *
   * @return if there is a next page.
   */
  boolean hasNext();

  /**
   * Returns if there is a previous page.
   *
   * @return if there is a previous page.
   */
  boolean hasPrevious();

  /**
   * Returns the {@link Pageable} to request the next page. Can be {@literal null} in case the current
   * page is already the last one. Clients should check {@link #hasNext()} before calling this method to make
   * sure they receive a non-{@literal null} value.
   *
   * @return
   */
  Pageable nextPageable();

  /**
   * Returns the {@link Pageable} to request the previous page. Can be {@literal null} in case the current
   * page is already the first one. Clients should check {@link #hasPrevious()} before calling this method make
   * sure receive a non-{@literal null} value.
   *
   * @return
   */
  Pageable previousPageable();

  Pageable currentPageable();
  
}
