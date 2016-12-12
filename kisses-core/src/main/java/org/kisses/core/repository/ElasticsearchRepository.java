package org.kisses.core.repository;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.Suggest.Suggestion;
import org.elasticsearch.search.suggest.Suggest.Suggestion.Entry;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.kisses.core.dto.ObjectIndexResponse;
import org.kisses.core.dto.ObjectSearchResponse;
import org.kisses.core.dto.ObjectUpdateResponse;
import org.kisses.core.pagination.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public interface ElasticsearchRepository<T> {

  // GET
  T get(String id);
  List<T> get(Set<String> ids);

  // SEARCH AND SCROLL
  List<T> findAll();
  List<T> scroll(QueryBuilder query);
  void forEach(Consumer<T> consumer);
  void forEach(QueryBuilder query, Consumer<T> consumer);
  ObjectSearchResponse<T> search(QueryBuilder query, Pageable pageable, SortBuilder...sorts);

  // INDEX
  ObjectIndexResponse<T> index(T entity);
  void index(T entity, BulkProcessor bulk);
  void index(Collection<T> entities);

  // UPDATE
  ObjectUpdateResponse<T> update(T entity);
  ObjectUpdateResponse<T> update(T entity, Map<String, Object> newFieldMap);
  void update(T entity, Map<String, Object> newFieldMap, BulkProcessor bulk);
  void update(Collection<T> entities);

  // DELETE
  DeleteResponse delete(String id);
  DeleteResponse delete(T entity);

  // AGG
  Aggregation aggregate(QueryBuilder query, AggregationBuilder agg);
  Map<String, Aggregation> aggregate(QueryBuilder query, Collection<AggregationBuilder> aggs);

  // SUGGEST
  List<String> suggest(SuggestionBuilder<?> suggestion, String text);
  Suggest suggest(SuggestBuilder suggest);
  Suggestion<? extends Entry> suggest(SuggestionBuilder<?> suggestion);

  // COUNT
  long count();
  long count(QueryBuilder query);

}
