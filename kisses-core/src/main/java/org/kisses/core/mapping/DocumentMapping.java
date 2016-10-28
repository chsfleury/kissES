package org.kisses.core.mapping;

import org.kisses.core.scope.Scope;

import java.lang.reflect.Field;

/**
 * @author Charles Fleury
 * @since 27/10/16.
 */
public class DocumentMapping<T> {

  private Class<T> documentClass;

  private String index;
  private String indexSettingsFile;

  private String type;
  private String typeMappingFile;

  private Field idField;

  private Scope scope;

  public DocumentMapping(Class<T> documentClass, String index, String indexSettingsFile, String type, String typeMappingFile, Field idField) {
    this.documentClass = documentClass;
    this.index = index;
    this.indexSettingsFile = indexSettingsFile;
    this.type = type;
    this.typeMappingFile = typeMappingFile;
    this.idField = idField;
    this.scope = new Scope(index, type);
  }

  public Class<T> getDocumentClass() {
    return documentClass;
  }

  public String getIndex() {
    return index;
  }

  public String getIndexSettingsFile() {
    return indexSettingsFile;
  }

  public String getType() {
    return type;
  }

  public String getTypeMappingFile() {
    return typeMappingFile;
  }

  public Field getIdField() {
    return idField;
  }

  public Scope getScope() {
    return scope;
  }
}
