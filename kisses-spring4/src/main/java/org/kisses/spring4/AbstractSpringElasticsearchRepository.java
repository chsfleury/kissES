package org.kisses.spring4;

import org.kisses.core.Kisses;
import org.kisses.core.repository.AbstractElasticsearchRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Charles Fleury
 * @since 31/10/16.
 */
public class AbstractSpringElasticsearchRepository<T> extends AbstractElasticsearchRepository<T> {

  public AbstractSpringElasticsearchRepository(Class<T> entityClass) {
    super(null, entityClass);
  }

  @Autowired
  public void setEs(Kisses es) {
    super.setEs(es);
  }

}
