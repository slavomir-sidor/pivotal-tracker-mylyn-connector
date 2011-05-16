package com.teamunify.eclipse.mylyn.pt.ui;

import java.io.File;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.RepositoryTemplate;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import com.teamunify.eclipse.mylyn.pt.core.PtCorePlugin;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PivotalTracker;

public class PtRepositoryPage extends AbstractRepositorySettingsPage {
  private static final String IREPOSITORY_ID = "com.teamunify.eclipse.mylyn.pt.ui.repositories";

  @Override
  protected void createSettingControls(Composite parent) {

    // Let the parent create the dialog
    super.createSettingControls(parent);

    // Modify the control labels so it makes more sense to users.
    Control[] children = parent.getChildren();
    for (Control c : children) {
      if (c instanceof Label) {
        Label l = (Label) c;
        if (l.getText().equals(LABEL_SERVER)) {
          l.setText("Project ID:");
          break;
        }
      }
    }
    runRepoExtension(parent);
  }

  /**
   * Contribution point to add the Field SVN Base URL in the Repository Page and add selection listener to listen
   * selecte project to create branch
   */

  private void runRepoExtension(Composite parent) {
    final Composite currentComposite = parent;
    IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(IREPOSITORY_ID);
    try {
      for (IConfigurationElement e : config) {
        final Object o = e.createExecutableExtension("class");
        if (o instanceof IPtRepositoryPage) {
          ISafeRunnable runnable = new ISafeRunnable() {

            public void handleException(Throwable arg0) {
              // ignore
              System.out.println("Exception in client");
            }

            public void run() throws Exception {
              ((IPtRepositoryPage) o).createSettingControls(currentComposite);
              ((IPtRepositoryPage) o).intializeSelectionListener();
            }

          };
          SafeRunner.run(runnable);
        }
      }
    } catch (CoreException ex) {
      System.out.println(ex.getMessage());
    }
  }

  public PtRepositoryPage(TaskRepository taskRepository) {
    super("Pivotal Tracker Connector Settings", "Specify a directory", taskRepository);
    setNeedsAnonymousLogin(false);
    setNeedsAdvanced(false);
    setNeedsEncoding(false);
    setNeedsHttpAuth(false);
    setNeedsProxy(false);
  }

  @Override
  public void applyTo(TaskRepository repository) {
    super.applyTo(repository);
    repository.setProperty(PtCorePlugin.REPOSITORY_KEY_PATH, getRepositoryUrl());
    repository.setProperty(PtCorePlugin.REPOSITORY_KEY_USERNAME, getUserName());
    repository.setProperty(PtCorePlugin.REPOSITORY_KEY_PASSWORD, getPassword());
  }

  private File getLocation(TaskRepository repository) {
    File root = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
    return new File(root, repository.getRepositoryUrl());
  }

  @Override
  public void createControl(Composite parent) {
    super.createControl(parent);
    addRepositoryTemplatesToServerUrlCombo();
  }

  @Override
  protected void createAdditionalControls(Composite parent) {
    // ignore
  }

  @Override
  public String getConnectorKind() {
    return PtCorePlugin.CONNECTOR_KIND;
  }

  @Override
  protected Validator getValidator(final TaskRepository repository) {
    System.out.println("Comes inside Validator");
    return new Validator() {
      @SuppressWarnings({ "deprecation", "null" })
      @Override
      public void run(IProgressMonitor monitor) throws CoreException {
        PivotalTracker pivotalTracker = null;
        try {
          pivotalTracker = new PivotalTracker("", repository.getUserName(), repository.getPassword());
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        File location = getLocation(repository);
        if (repository.getUserName() != null && repository.getPassword() != null) {
          if (pivotalTracker.getToken().trim().equalsIgnoreCase("")) {
            throw new CoreException(
                                    new Status(
                                               IStatus.ERROR,
                                               PtUiPlugin.ID_PLUGIN,
                                               NLS.bind("Invalid User Name or Password. It is also possible you do not have an API token. Go to the Pivotal Tracker website, and create one near the bottom of your profile page.",
                                                        location.getName())));
          } else {
            boolean isProjectExist = true;
            if (repository.getRepositoryUrl() == "") { throw new CoreException(
                                                                               new Status(
                                                                                          IStatus.ERROR,
                                                                                          PtUiPlugin.ID_PLUGIN,
                                                                                          NLS.bind("Please enter Project Id in digits",
                                                                                                   location.getName()))); }

            try {
              isProjectExist = pivotalTracker.getProject(Integer.parseInt(repository.getRepositoryUrl()));
            } catch (NumberFormatException e) {

            } catch (Exception e) {
              e.printStackTrace();
            }

            if (!isProjectExist) { throw new CoreException(
                                                           new Status(
                                                                      IStatus.ERROR,
                                                                      PtUiPlugin.ID_PLUGIN,
                                                                      NLS.bind("Project Id  ''{0}'' does not exist. To find your project ID, navigate to it in a browser, and look for the number at or near the end of the URL.",
                                                                               location.getName()))); }

          }
        }
      }
    };
  }

  @Override
  protected boolean isValidUrl(String url) {
    return true;
  }

  @Override
  protected void repositoryTemplateSelected(RepositoryTemplate template) {
    repositoryLabelEditor.setStringValue(template.label);
    setUrl(template.repositoryUrl);
    setAnonymous(template.anonymous);
    getContainer().updateButtons();
  }

}
