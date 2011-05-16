package com.teamunify.eclipse.mylyn.pt.ui;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class PtUiPlugin extends Plugin {

  public static final String ID_PLUGIN = "com.teamunify.eclipse.mylyn.pt.ui"; //$NON-NLS-1$

  private static PtUiPlugin plugin;

  public PtUiPlugin() {}

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

  public static PtUiPlugin getDefault() {
    return plugin;
  }

}
