package org.kisses.core.requests;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.kisses.core.dto.CreateMappingResponse;
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

  public boolean exists(DocumentMapping mapping) {
    return client.admin().indices().exists(indicesExistsRequest(mapping.getIndex())).actionGet().isExists()
            && !client.admin().indices().prepareGetMappings(mapping.getIndex()).setTypes(mapping.getType()).get().getMappings().isEmpty();
  }

  public CreateMappingResponse create(DocumentMapping mapping) throws IOException {
    String settings = readFile(mapping.getIndexSettingsFile());
    CreateIndexResponse createIndexResponse = client.admin().indices().prepareCreate(mapping.getIndex()).setSettings(settings).get();
    String mappingSource = readFile(mapping.getTypeMappingFile());
    PutMappingResponse putMappingResponse = client.admin().indices().preparePutMapping(mapping.getIndex()).setType(mapping.getType()).setSource(mappingSource).get();
    return new CreateMappingResponse(createIndexResponse, putMappingResponse);
  }

  public DeleteIndexResponse delete(DocumentMapping mapping) {
    return client.admin().indices().prepareDelete(mapping.getIndex()).get();
  }

  public void clear(DocumentMapping mapping) throws IOException {
    if(exists(mapping) && delete(mapping).isAcknowledged()) {
      create(mapping);
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
