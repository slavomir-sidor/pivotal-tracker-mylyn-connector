package com.teamunify.eclipse.mylyn.pt.ui;

import org.eclipse.swt.widgets.Composite;

public interface IPtRepositoryPage {
  void createSettingControls(Composite composite);

  void intializeSelectionListener();
}
