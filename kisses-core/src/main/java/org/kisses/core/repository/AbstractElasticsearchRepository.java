package org.kisses.core.repository;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.kisses.core.Kisses;
import org.kisses.core.dto.ObjectIndexResponse;
import org.kisses.core.dto.ObjectMultiSearchResponse;
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

  protected Kisses es;
  protected DocumentMapping<T> mapping;
  private Class<T> entityClass;

  public AbstractElasticsearchRepository(Kisses es, Class<T> entityClass) {
    this.entityClass = entityClass;
    if(es != null) {
      setEs(es);
    }
  }

  protected void setEs(Kisses es) {
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
  public List<T> scroll(QueryBuilder query) {
    if(count(query) >= Integer.MAX_VALUE) {
      throw new IllegalStateException("too many elements");
    }
    List<T> results = new ArrayList<>();
    forEach(query, results::add);
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
  public ObjectSearchResponse<T> search(QueryBuilder query, Pageable pageable, SortBuilder... sorts) {
    return es.search().search(query, mapping, pageable, sorts);
  }

  @Override
  public ObjectMultiSearchResponse<T> multiSearch(Pageable pageable, QueryBuilder... queries) {
    return es.search().multi(mapping, pageable, queries);
  }

  @Override
  public ObjectMultiSearchResponse<T> multiSearch(Pageable pageable, SearchRequestBuilder... requests) {
    return es.search().multi(mapping, pageable, requests);
  }

  @Override
  public ObjectIndexResponse<T> index(T entity) {
    return es.index().index(entity, mapping);
  }

  @Override
  public void index(T entity, BulkProcessor bulk) {
    es.index().index(entity, mapping, bulk);
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
  public void update(T entity, Map<String, Object> newFieldMap, BulkProcessor bulk) {
    es.update().update(entity, mapping, newFieldMap, bulk);
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
  public List<String> suggest(SuggestionBuilder<?> suggestion, String text) {
    return es.suggest(mapping.getScope(), suggestion, text);
  }

  @Override
  public Suggest suggest(SuggestBuilder suggest) {
    return es.suggest().suggest(mapping.getScope(), suggest);
  }

  @Override
  public Suggestion<? extends Entry> suggest(SuggestionBuilder<?> suggestion) {
    return es.suggest().suggest(mapping.getScope(), suggestion);
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
