package org.kisses.core.scope;

/**
 * @author Charles Fleury
 * @since 27/10/16.
 */
public class Scope {

  private String[] indices;
  private String[] types;

  public Scope() {}

  public Scope(String index, String type) {
    setIndices(index);
    setTypes(type);
  }

  public Scope(String index, String...types) {
    setIndices(index);
    setTypes(types);
  }

  public String[] getIndices() {
    return indices;
  }

  public String[] getTypes() {
    return types;
  }

  public void setIndices(String...indices) {
    this.indices = indices;
  }

  public void setTypes(String...types) {
    this.types = types;
  }

  public boolean hasTypes() {
    return types != null && types.length != 0;
  }

}
