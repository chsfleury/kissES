package org.kisses.spring4;

import org.elasticsearch.node.NodeValidationException;
import org.kisses.core.Kisses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;
import java.util.List;

/**
 * @author Charles Fleury
 * @since 31/10/16.
 */
@Configuration
public class KissesConfiguration {

  @Value("${kisses.cluster-name:elasticsearch}")
  private String clusterName;

  @Value("#{'${kisses.cluster-nodes:}'.split(',')}")
  private List<String> clusterNodes;

  @Value("${kisses.mapping-package:''}")
  private String mappingPackage;

  @Value("${kisses.local:false}")
  private boolean localMode;

  @Value("${kisses.path.home:.}")
  private String homePath;

  @Bean(destroyMethod = "close")
  public Kisses kisses() throws NodeValidationException, UnknownHostException {
    if(mappingPackage == null || mappingPackage.isEmpty()) {
      throw new IllegalStateException("mapping package must be set");
    }

    Kisses kisses;

    if(localMode) {
      kisses = Kisses.embedded(mappingPackage, homePath);
    } else {
      kisses = Kisses.transport(mappingPackage, clusterName, clusterNodes);
    }

    return kisses;
  }

}
