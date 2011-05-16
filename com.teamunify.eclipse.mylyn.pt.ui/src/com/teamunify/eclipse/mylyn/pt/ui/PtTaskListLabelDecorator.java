/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.teamunify.eclipse.mylyn.pt.ui;

import java.net.URL;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.mylyn.tasks.core.ITask;

public class PtTaskListLabelDecorator implements ILightweightLabelDecorator {
  private static URL baseURL = null;

  public void decorate(Object element, IDecoration decoration) {
    if (element instanceof ITask) {
      ITask task = (ITask) element;
      String kind = task.getTaskKind();
      if (kind.equalsIgnoreCase("bug")) {
        ImageDescriptor imageDescriptor = null;
        try {
          imageDescriptor = ImageDescriptor.createFromURL(getImageURL("bug.png"));
          decoration.addOverlay(imageDescriptor, IDecoration.BOTTOM_RIGHT);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private URL getImageURL(String name) throws Exception {
    if (baseURL == null) {
      baseURL = PtUiPlugin.getDefault().getBundle().getEntry("/icons/");
    }
    return new URL(baseURL, name);
  }

  public void addListener(ILabelProviderListener arg0) {}

  public void dispose() {}

  public boolean isLabelProperty(Object arg0, String arg1) {
    return false;
  }

  public void removeListener(ILabelProviderListener arg0) {}
}