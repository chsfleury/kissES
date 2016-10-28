package org.kisses.core.pagination.impl;


import org.kisses.core.pagination.Page;
import org.kisses.core.pagination.Pageable;

import java.util.Iterator;
import java.util.List;

/**
 * @author Charles Fleury
 * @since 01/02/16.
 */
public class DefaultPage<T> implements Page<T> {

  protected List<T> content;
  protected Pageable pageable;
  protected long total;

  public DefaultPage(List<T> content, Pageable pageable, long total) {
    this.content = content;
    this.pageable = pageable;
    this.total = total;
  }

  @Override
  public int getTotalPages() {
    return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
  }

  @Override
  public long getTotalElements() {
    return total;
  }

  @Override
  public int getNumber() {
    return pageable == null ? 0 : pageable.getPageNumber();
  }

  @Override
  public int getSize() {
    return pageable == null ? 0 : pageable.getPageSize();
  }

  @Override
  public int getNumberOfElements() {
    return content.size();
  }

  @Override
  public List<T> getContent() {
    return content;
  }

  @Override
  public boolean hasContent() {
    return !content.isEmpty();
  }

  @Override
  public boolean isFirst() {
    return !hasPrevious();
  }

  @Override
  public boolean isLast() {
    return !hasNext();
  }

  @Override
  public boolean hasNext() {
    return getNumber() + 1 < getTotalPages();
  }

  @Override
  public boolean hasPrevious() {
    return getNumber() > 0;
  }

  @Override
  public Pageable nextPageable() {
    return hasNext() ? pageable.next() : null;
  }

  @Override
  public Pageable previousPageable() {
    return hasPrevious() ? pageable.previousOrFirst() : null;
  }

  @Override
  public Pageable currentPageable() {
    return pageable;
  }

  @Override
  public Iterator<T> iterator() {
    return content.iterator();
  }
}
