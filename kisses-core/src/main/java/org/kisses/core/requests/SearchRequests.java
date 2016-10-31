package org.kisses.core.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.kisses.core.dto.ObjectScrollResponse;
import org.kisses.core.dto.ObjectSearchResponse;
import org.kisses.core.dto.ObjectSearchResult;
import org.kisses.core.mapping.DocumentMapping;
import org.kisses.core.mapping.MappingRegistry;
import org.kisses.core.pagination.Pageable;
import org.kisses.core.pagination.impl.DefaultPage;
import org.kisses.core.pagination.impl.PageRequest;
import org.kisses.core.scope.Scope;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class SearchRequests {

  protected static final long DEFAULT_KEEP_CONTEXT = 15000;

  private Client client;
  private MappingRegistry mappingRegistry;
  private ObjectMapper mapper;

  public SearchRequests(Client client, MappingRegistry mappingRegistry, ObjectMapper mapper) {
    this.client = client;
    this.mappingRegistry = mappingRegistry;
    this.mapper = mapper;
  }

  public <T> ObjectSearchResponse<T> search(QueryBuilder query, DocumentMapping<T> mapping, Pageable pageable) {
    return search(query, mapping.getDocumentClass(), mapping.getScope(), pageable);
  }

  public <T> ObjectSearchResponse<T> search(QueryBuilder query, Class<T> target, Scope scope, Pageable pageable) {
    return search(
            prepare(scope, query),
            target,
            scope.getTypes().length > 1,
            pageable
    );
  }

  public SearchRequestBuilder prepare(Scope scope) {
    return prepare(scope, null);
  }

  public SearchRequestBuilder prepare(Scope scope, @Nullable QueryBuilder query) {
    return client.prepareSearch(scope.getIndices())
            .setTypes(scope.getTypes())
            .setQuery(query);
  }

  public <T> ObjectSearchResponse<T> search(SearchRequestBuilder request, Class<T> target, boolean dynamicMapping, Pageable pageable) {
    try {
      request.setSize(pageable.getPageSize()).setFrom(pageable.getOffset());
      SearchResponse searchResponse = request.get();
      List<ObjectSearchResult<T>> content = convert(searchResponse.getHits(), target, dynamicMapping);
      return new ObjectSearchResponse<>(searchResponse, new DefaultPage<>(content, pageable, searchResponse.getHits().getTotalHits()));
    } catch (IOException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> void forEach(QueryBuilder query, DocumentMapping<T> mapping, Consumer<T> consumer) {
    forEach(query, mapping.getDocumentClass(), mapping.getScope(), consumer);
  }

  public <T> void forEach(QueryBuilder query, Class<T> target, Scope scope, Consumer<T> consumer) {
    forEach(
            prepare(scope, query),
            target,
            scope.getTypes().length > 1,
            consumer
    );
  }

  public <T> void forEach(SearchRequestBuilder request, Class<T> targetClass, boolean dynamicMapping, Consumer<T> consumer) {
    ObjectScrollResponse<T> response = scroll(request, targetClass, dynamicMapping, DEFAULT_KEEP_CONTEXT);
    boolean hasNext = true;
    while(response.getResults().hasContent() && hasNext) {
      response.getResults().getContent().stream().map(ObjectSearchResult::getObject).forEach(consumer);
      if(response.getResults().hasNext()) {
        response = scroll(targetClass, response);
      } else {
        hasNext = false;
      }
    }
  }

  public <T> ObjectScrollResponse<T> scroll(QueryBuilder query, Class<T> target, Scope scope, long millis) {
    return scroll(
            prepare(scope, query),
            target,
            scope.getTypes().length > 1,
            millis
    );
  }

  public <T> ObjectScrollResponse<T> scroll(SearchRequestBuilder request, Class<T> target, boolean dynamicMapping, long millis) {
    try {
      SearchResponse searchResponse = request
              .addSort("_doc", SortOrder.ASC)
              .setScroll(new TimeValue(millis))
              .setSize(PageRequest.MAX_SIZE)
              .get();
      List<ObjectSearchResult<T>> content = convert(searchResponse.getHits(), target, dynamicMapping);
      return new ObjectScrollResponse<>(searchResponse, new DefaultPage<>(content, PageRequest.SCROLL, searchResponse.getHits().getTotalHits()), millis, dynamicMapping);
    } catch (IOException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> ObjectScrollResponse<T> scroll(Class<T> target, ObjectScrollResponse<T> previousResponse) {
    try {
      SearchResponse searchResponse = client.prepareSearchScroll(previousResponse.getScrollId()).setScroll(new TimeValue(previousResponse.getKeepContextMillis())).get();
      List<ObjectSearchResult<T>> content = convert(searchResponse.getHits(), target, previousResponse.isDynamicMapping());
      return new ObjectScrollResponse<>(searchResponse, new DefaultPage<>(content, previousResponse.getResults().nextPageable(), searchResponse.getHits().getTotalHits()), previousResponse.getKeepContextMillis(), previousResponse.isDynamicMapping());
    } catch (IOException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private <T> List<ObjectSearchResult<T>> convert(SearchHits hits, Class<T> targetClass, boolean dynamicMapping) throws IOException, IllegalAccessException {
    List<ObjectSearchResult<T>> list = new ArrayList<>(hits.getHits().length);
    DocumentMapping documentMapping = null;
    if(!dynamicMapping) {
      documentMapping = mappingRegistry.get(targetClass);
    }
    for (SearchHit hit : hits) {
      Class<?> target = dynamicMapping ? mappingRegistry.get(hit.getType()) : targetClass;
      if(dynamicMapping) {
        documentMapping = mappingRegistry.get(target);
      }
      T object = (T) mapper.readValue(hit.source(), target);
      if(documentMapping != null && documentMapping.getIdField() != null) {
        documentMapping.getIdField().set(object, hit.getId());
      }
      ObjectSearchResult<T> result = new ObjectSearchResult<>(hit, object);
      list.add(result);
    }
    return list;
  }
}
