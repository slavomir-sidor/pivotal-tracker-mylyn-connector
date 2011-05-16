package com.teamunify.eclipse.mylyn.pt.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class PtCorePlugin extends Plugin {

  public static final String ID_PLUGIN = "com.teamunify.eclipse.mylyn.pt.core"; //$NON-NLS-1$

  public static final String CONNECTOR_KIND = "org.eclipse.mylyn.examples.xml"; //$NON-NLS-1$

  public static final String REPOSITORY_KEY_PATH = ID_PLUGIN + ".path";

  public static final String REPOSITORY_KEY_USERNAME = ID_PLUGIN + ".username";

  public static final String REPOSITORY_KEY_PASSWORD = ID_PLUGIN + ".password";

  public static final String QUERY_KEY_STORY_TYPE = ID_PLUGIN + ".storytype";

  public static final String QUERY_KEY_REQUESTED_BY = ID_PLUGIN + ".requestby";

  public static final String QUERY_KEY_OWNED_BY = ID_PLUGIN + ".ownedby";;

  public static final String QUERY_KEY_STATE = ID_PLUGIN + ".state";

  public static final String QUERY_KEY_ITERATION_TYPE = ID_PLUGIN + ".iterationtype";

  public static final String QUERY_KEY_LABEL = ID_PLUGIN + ".label";
  public static final String QUERY_KEY_LABEL_INVERSION = ID_PLUGIN + ".invertLabels";

  private static PtCorePlugin plugin;

  public PtCorePlugin() {}

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  public static PtCorePlugin getDefault() {
    return plugin;
  }

}
