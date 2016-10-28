package org.kisses.core;

import org.kisses.annotations.Id;
import org.kisses.annotations.Mapping;

/**
 * @author Charles Fleury
 * @since 27/10/16.
 */
@Mapping(index = "document", indexSettings = "document_index.json", type = "document", typeMapping = "document_type.json")
public class Document {

  @Id
  private String id;

  private String name;

}
