package org.kisses.core.mapping;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.elasticsearch.ElasticsearchException;
import org.kisses.annotations.Id;
import org.kisses.annotations.Mapping;
import org.kisses.core.requests.MappingRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Charles Fleury
 * @since 27/10/16.
 */
public class MappingRegistry {

  public static final Logger LOG = LoggerFactory.getLogger(MappingRegistry.class);

  private Map<Class<?>, DocumentMapping> mappingByClass;
  private Map<String, Class<?>> classByType;
  private MappingRequests map;

  public MappingRegistry(MappingRequests mappingRequests, String packagePath) {
    mappingByClass = new HashMap<>();
    classByType = new HashMap<>();
    map = mappingRequests;
    new FastClasspathScanner(packagePath).matchClassesWithAnnotation(Mapping.class, this::registerClass).scan();
  }

  public <T> DocumentMapping<T> get(Class<T> entityClass) {
    return mappingByClass.get(entityClass);
  }

  public Class<?> get(String type) {
    return classByType.get(type);
  }

  private <T> void registerClass(Class<T> mappingClass) {
    Mapping mapping = mappingClass.getAnnotation(Mapping.class);

    Field idField = null;
    for(Field field : mappingClass.getDeclaredFields()) {
      if(field.isAnnotationPresent(Id.class)) {
        if(idField == null) {
          idField = field;
        } else {
          throw new IllegalStateException("a mapping class cannot have two @Id fields");
        }
      }
    }

    if(idField != null) {
      idField.setAccessible(true);
    }

    DocumentMapping<T> documentMapping = new DocumentMapping<>(
            mappingClass,
            mapping.index(),
            mapping.indexSettings(),
            mapping.type(),
            mapping.typeMapping(),
            idField
    );

    mappingByClass.put(mappingClass, documentMapping);
    classByType.put(mapping.type(), mappingClass);

    try {
      if(!map.indexExists(documentMapping)) {
        map.createIndex(documentMapping);
        LOG.info("create index of {}", mappingClass.getSimpleName());
      }

      if(!map.mappingExists(documentMapping)) {
        map.createMapping(documentMapping);
        LOG.info("create mapping of {}", mappingClass.getSimpleName());
      }
    } catch (IOException e) {
      LOG.error("Cannot register " + documentMapping.getIndex(), e);
    } catch (ElasticsearchException e) {
      throw new RuntimeException("Error while create " + mappingClass.getSimpleName() + " index or mapping", e);
    }
  }

}
