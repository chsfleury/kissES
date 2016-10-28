package org.kisses.core.dto;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;

/**
 * @author Charles Fleury
 * @since 28/10/16.
 */
public class CreateMappingResponse {

  private CreateIndexResponse createIndexResponse;
  private PutMappingResponse putMappingResponse;

  public CreateMappingResponse(CreateIndexResponse createIndexResponse, PutMappingResponse putMappingResponse) {
    this.createIndexResponse = createIndexResponse;
    this.putMappingResponse = putMappingResponse;
  }

  public CreateIndexResponse getCreateIndexResponse() {
    return createIndexResponse;
  }

  public void setCreateIndexResponse(CreateIndexResponse createIndexResponse) {
    this.createIndexResponse = createIndexResponse;
  }

  public PutMappingResponse getPutMappingResponse() {
    return putMappingResponse;
  }

  public void setPutMappingResponse(PutMappingResponse putMappingResponse) {
    this.putMappingResponse = putMappingResponse;
  }
}
