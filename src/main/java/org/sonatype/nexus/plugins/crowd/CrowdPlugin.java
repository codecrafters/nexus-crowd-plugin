package org.sonatype.nexus.plugins.crowd;

import org.eclipse.sisu.EagerSingleton;
import org.jetbrains.annotations.NonNls;
import org.sonatype.nexus.plugin.PluginIdentity;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@EagerSingleton
public class CrowdPlugin extends PluginIdentity {
  /**
   * Prefix for ID-like things.
   */
  @NonNls
  public static final String ID_PREFIX = "crowd-realm";

  /**
   * Expected groupId for plugin artifact.
   */
  @NonNls
  public static final String GROUP_ID = "org.sonatype.nexus.plugins";

  /**
   * Expected artifactId for plugin artifact.
   */
  @NonNls
  public static final String ARTIFACT_ID = "nexus-" + ID_PREFIX + "-plugin";

  @Inject
  public CrowdPlugin() throws Exception {
    super(GROUP_ID, ARTIFACT_ID);
  }
}
