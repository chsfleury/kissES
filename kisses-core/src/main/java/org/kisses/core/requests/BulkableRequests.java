package org.kisses.core.requests;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Nullable;
import org.kisses.core.mapping.DocumentMapping;
import org.kisses.core.mapping.MappingRegistry;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public abstract class BulkableRequests {

  protected Client client;
  protected MappingRegistry mappingRegistry;

  public BulkableRequests(Client client, MappingRegistry mappingRegistry) {
    this.client = client;
    this.mappingRegistry = mappingRegistry;
  }

  protected <T> void bulk(Collection<T> entities) {
    Optional<T> first = entities.stream().findFirst();
    if(first.isPresent()) {
      DocumentMapping mapping = mappingRegistry.get(first.get().getClass());
      bulk(entities, mapping);
    }
  }

  protected <T> void bulk(Collection<T> entities, DocumentMapping<T> mapping) {
    int size = entities.size();
    BulkProcessor bulk = createBulk(null).setBulkActions(size).build();
    entities.forEach(entity -> bulk.add(prepare(entity, mapping).request()));
    bulk.close();
  }

  protected abstract <T> ActionRequestBuilder prepare(T entity, DocumentMapping mapping);

  protected BulkProcessor.Builder createBulk(@Nullable BulkProcessor.Listener listener) {
    return bulk(client, listener);
  }

  public static BulkProcessor.Builder bulk(Client client, @Nullable BulkProcessor.Listener listener) {
    if(listener == null) {
      listener = new BulkProcessor.Listener() {
        @Override
        public void beforeBulk(long executionId, BulkRequest request) {

        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

        }
      };
    }
    return new BulkProcessor.Builder(client, listener);
  }
}
