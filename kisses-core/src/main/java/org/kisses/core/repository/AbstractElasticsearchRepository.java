package org.kisses.core.repository;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.kisses.core.KissES;
import org.kisses.core.dto.ObjectIndexResponse;
import org.kisses.core.dto.ObjectSearchResponse;
import org.kisses.core.dto.ObjectUpdateResponse;
import org.kisses.core.mapping.DocumentMapping;
import org.kisses.core.pagination.Pageable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class AbstractElasticsearchRepository<T> implements ElasticsearchRepository<T> {

  protected KissES es;
  protected DocumentMapping<T> mapping;

  public AbstractElasticsearchRepository(KissES es, Class<T> entityClass) {
    this.es = es;
    this.mapping = es.getMappingRegistry().get(entityClass);
  }

  @Override
  public T get(String id) {
    return es.get().get(mapping, id).getObject();
  }

  @Override
  public List<T> get(Set<String> ids) {
    return es.get().get(mapping, ids).getObjects();
  }

  @Override
  public List<T> findAll() {
    if(count() >= Integer.MAX_VALUE) {
      throw new IllegalStateException("too many elements");
    }
    List<T> results = new ArrayList<>();
    forEach(results::add);
    return results;
  }

  @Override
  public void forEach(Consumer<T> consumer) {
    forEach(matchAllQuery(), consumer);
  }

  @Override
  public void forEach(QueryBuilder query, Consumer<T> consumer) {
    es.search().forEach(query, mapping, consumer);
  }

  @Override
  public ObjectSearchResponse<T> search(QueryBuilder query, Pageable pageable) {
    return es.search().search(query, mapping, pageable);
  }

  @Override
  public ObjectIndexResponse<T> index(T entity) {
    return es.index().index(entity, mapping);
  }

  @Override
  public void index(Collection<T> entities) {
    es.index().index(entities, mapping);
  }

  @Override
  public ObjectUpdateResponse<T> update(T entity) {
    return es.update().update(entity, mapping);
  }

  @Override
  public ObjectUpdateResponse<T> update(T entity, Map<String, Object> newFieldMap) {
    return es.update().update(entity, mapping, newFieldMap);
  }

  @Override
  public void update(Collection<T> entities) {
    es.update().update(entities, mapping);
  }

  @Override
  public DeleteResponse delete(String id) {
    return es.delete().delete(mapping, id);
  }

  @Override
  public DeleteResponse delete(T entity) {
    return es.delete().delete(mapping, entity);
  }

  @Override
  public Aggregation aggregate(QueryBuilder query, AggregationBuilder agg) {
    return es.aggregate(query, mapping.getScope(), agg);
  }

  @Override
  public Map<String, Aggregation> aggregate(QueryBuilder query, Collection<AggregationBuilder> aggs) {
    return es.aggregate(query, mapping.getScope(), aggs);
  }

  @Override
  public long count() {
    return count(matchAllQuery());
  }

  @Override
  public long count(QueryBuilder query) {
    return es.count().count(query, mapping.getScope());
  }
}
