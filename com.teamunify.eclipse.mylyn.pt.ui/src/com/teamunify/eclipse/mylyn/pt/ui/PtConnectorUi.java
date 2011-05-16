package com.teamunify.eclipse.mylyn.pt.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;
import org.eclipse.mylyn.tasks.ui.wizards.TaskAttachmentPage;
import com.teamunify.eclipse.mylyn.pt.core.PtCorePlugin;

public class PtConnectorUi extends AbstractRepositoryConnectorUi {

  private static final String ICONNECTOR_ID = "com.teamunify.eclipse.mylyn.pt.ui.connector";

  public PtConnectorUi() {}

  @Override
  public String getConnectorKind() {
    System.out.println("PT Connector UI getConnectorKind");
    runConnectorExtension();
    return PtCorePlugin.CONNECTOR_KIND;
  }

  /**
   * Contribution point to add selection listener to listen selected project to create branch
   */
  private void runConnectorExtension() {

    IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(ICONNECTOR_ID);
    try {
      for (IConfigurationElement e : config) {
        final Object o = e.createExecutableExtension("class");
        if (o instanceof IPtConnectorUi) {
          ISafeRunnable runnable = new ISafeRunnable() {

            public void handleException(Throwable arg0) {
              // ignore
              System.out.println("Exception in client");
            }

            public void run() throws Exception {
              ((IPtConnectorUi) o).intializeSelectionListener();
            }

          };
          SafeRunner.run(runnable);
        }
      }
    } catch (CoreException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Override
  public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
    return new PtRepositoryPage(taskRepository);
  }

  @Override
  public boolean hasSearchPage() {
    return true;
  }

  @Override
  public IWizard getNewTaskWizard(TaskRepository repository, ITaskMapping selection) {
    return new NewTaskWizard(repository, selection);
  }

  @Override
  public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
    RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
    wizard.addPage(new PtQueryPage(repository, query));
    return wizard;
  }

  @Override
  public IWizardPage getTaskAttachmentPage(TaskAttachmentModel model) {
    TaskAttachmentPage page = new TaskAttachmentPage(model);
    page.setNeedsReplaceExisting(true);
    return page;
  }
}
