package org.kisses.core.requests;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.kisses.core.mapping.DocumentMapping;
import org.kisses.core.mapping.MappingRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.elasticsearch.client.Requests.indicesExistsRequest;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class MappingRequests {

  private Client client;
  private MappingRegistry registry;

  public MappingRequests(Client client) {
    this.client = client;
  }

  public void setRegistry(MappingRegistry registry) {
    this.registry = registry;
  }

  public <T> DocumentMapping<T> get(Class<T> entityClass) {
    return registry.get(entityClass);
  }

  public boolean indexExists(DocumentMapping mapping) {
    return client.admin().indices().exists(indicesExistsRequest(mapping.getIndex())).actionGet().isExists();
  }

  public boolean mappingExists(DocumentMapping mapping) {
    return !client.admin().indices().prepareGetMappings(mapping.getIndex()).setTypes(mapping.getType()).get().getMappings().isEmpty();
  }

  public CreateIndexResponse createIndex(DocumentMapping mapping) throws IOException {
    String settings = readFile(mapping.getIndexSettingsFile());
    return client.admin().indices().prepareCreate(mapping.getIndex()).setSettings(settings).get();
  }

  public PutMappingResponse createMapping(DocumentMapping mapping) throws IOException {
    String mappingSource = readFile(mapping.getTypeMappingFile());
    return client.admin().indices().preparePutMapping(mapping.getIndex()).setType(mapping.getType()).setSource(mappingSource).get();
  }

  public DeleteIndexResponse delete(DocumentMapping mapping) {
    return client.admin().indices().prepareDelete(mapping.getIndex()).get();
  }

  public void clear(DocumentMapping mapping) throws IOException {
    if(indexExists(mapping) && delete(mapping).isAcknowledged()) {
      createIndex(mapping);
      createMapping(mapping);
    }
  }

  private String readFile(String file) throws IOException {
    InputStream is = getClass().getClassLoader().getResourceAsStream(file);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];
    while ((nRead = is.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }
    buffer.flush();
    return new String(buffer.toByteArray());
  }
}
