package azkaban.cluster;

import azkaban.utils.Props;
import org.apache.hadoop.conf.Configuration;

import javax.inject.Inject;
import java.util.Collection;

/**
 * An implementation of {@link ClusterRouter} that always routes jobs to the default cluster.
 */
public class DefaultClusterRouter extends ClusterRouter {
  @Inject
  public DefaultClusterRouter(final ClusterRegistry clusterRegistry, final Configuration configuration) {
    super(clusterRegistry, configuration);
  }

  @Override
  public Cluster getCluster(final String jobId, final Props jobProps, final Collection<String> componentDependency) {
    final Cluster cluster = this.clusterRegistry.getCluster(Cluster.DEFAULT_CLUSTER);
    if (cluster == null) {
      throw new RuntimeException("The default cluster is not found");
    }
    return cluster;
  }

  @Override
  public Cluster getCluster(final String clusterId) {
    return this.clusterRegistry.getCluster(clusterId);
  }
}
