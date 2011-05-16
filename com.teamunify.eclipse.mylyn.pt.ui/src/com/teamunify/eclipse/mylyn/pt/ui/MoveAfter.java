package com.teamunify.eclipse.mylyn.pt.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.internal.tasks.ui.IDynamicSubMenuContributor;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.swt.widgets.Display;
import com.teamunify.eclipse.mylyn.pt.core.PtConnector;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PivotalTracker;

public class MoveAfter implements IDynamicSubMenuContributor {
  PivotalTracker pivotalTracker = null;
  ITask selectedTask = null;
  TaskRepository repository = null;

  public MenuManager getSubMenuManager(final List<IRepositoryElement> selectedElements) {
    final MenuManager subMenuManager = new MenuManager("Move After");

    // Get selected Task
    for (IRepositoryElement element : selectedElements) {
      if (element instanceof ITask) {
        selectedTask = (AbstractTask) element;
      }
    }
    if (selectedTask != null) {
      try {
        TaskData taskdata = TasksUiPlugin.getTaskDataManager().getTaskData(selectedTask);
        String selectedTaskStatus = taskdata.getRoot().getAttribute(TaskAttribute.STATUS).getValue();
        String selectedTaskLane = taskdata.getRoot().getAttribute(TaskAttribute.PRIORITY).getValue();

        repository = TasksUiPlugin.getRepositoryManager().getRepository(selectedTask.getRepositoryUrl());
        PtConnector connector = new PtConnector();
        pivotalTracker = connector.getPivotalTracker(repository);

        List<RepositoryQuery> queries = new ArrayList<RepositoryQuery>(TasksUiInternal.getTaskList().getQueries());
        Collection<ITask> tasklist = null;
        for (RepositoryQuery query : queries) {
          if (query.getSummary().equalsIgnoreCase(selectedTask.getUrl())) {
            tasklist = query.getChildren();
          }
        }

        if (tasklist != null) {
          if (selectedTaskLane.equalsIgnoreCase("backlog") || selectedTaskLane.equalsIgnoreCase("icebox")
              || selectedTaskLane.equalsIgnoreCase("current_backlog")) {

            Iterator<ITask> iterator = tasklist.iterator();
            while (iterator.hasNext()) {
              final ITask task = iterator.next();
              if (selectedTask.getTaskId() != task.getTaskId()) {
                subMenuManager.add(prepareMenuAction(task));
              }
            }
          }

          if (!selectedTaskStatus.equalsIgnoreCase("accepted") && selectedTaskLane.equalsIgnoreCase("current")) {
            if (selectedTask.getTaskKind().equalsIgnoreCase("release")
                && selectedTaskStatus.equalsIgnoreCase("Not Yet Started")) {
              Iterator<ITask> iterator1 = tasklist.iterator();
              while (iterator1.hasNext()) {
                final ITask task = iterator1.next();
                if (selectedTask.getTaskId() != task.getTaskId()) {
                  subMenuManager.add(prepareMenuAction(task));
                }
              }
            } else if (selectedTaskStatus.equalsIgnoreCase("Not Yet Started")) {
              Iterator<ITask> iterator1 = tasklist.iterator();
              while (iterator1.hasNext()) {
                final ITask task = iterator1.next();
                TaskData currentTaskData = TasksUiPlugin.getTaskDataManager().getTaskData(task);
                String currentTaskStatus = currentTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue();
                if (currentTaskStatus.equalsIgnoreCase("Not Yet Started")
                    && selectedTask.getTaskId() != task.getTaskId()) {
                  subMenuManager.add(prepareMenuAction(task));
                }
              }

            } else {
              Iterator<ITask> iterator1 = tasklist.iterator();
              while (iterator1.hasNext()) {
                final ITask task = iterator1.next();
                TaskData currentTaskData = TasksUiPlugin.getTaskDataManager().getTaskData(task);
                String currentTaskStatus = currentTaskData.getRoot().getAttribute(TaskAttribute.STATUS).getValue();
                if (!currentTaskStatus.equalsIgnoreCase("Not Yet Started")
                    && !currentTaskStatus.equalsIgnoreCase("accepted") && selectedTask.getTaskId() != task.getTaskId()) {
                  subMenuManager.add(prepareMenuAction(task));
                }
              }
            }
          }
        }
      } catch (CoreException e) {
        e.printStackTrace();
      }
    }

    return subMenuManager;
  }

  private Action prepareMenuAction(final ITask task) {

    Action action = new Action(task.getSummary(), IAction.AS_RADIO_BUTTON) {
      @Override
      public void run() {
        moveAfter(task.getTaskId());
      }
    };
    action.setImageDescriptor(TasksUiImages.CATEGORY);
    return action;
  }

  /**
   * public for testing
   * 
   * Deals with text where user has entered a '@' or tab character but which are not meant to be accelerators. from:
   * Action#setText: Note that if you want to insert a '@' character into the text (but no accelerator, you can simply
   * insert a '@' or a tab at the end of the text. see Action#setText
   */
  public String handleAcceleratorKeys(String text) {
    if (text == null) { return null; }

    int index = text.lastIndexOf('\t');
    if (index == -1) {
      index = text.lastIndexOf('@');
    }
    if (index >= 0) { return text.concat("@"); //$NON-NLS-1$
    }
    return text;
  }

  private void moveAfter(String targetStoryId) {

    try {
      String response = pivotalTracker.moveStory(selectedTask.getTaskId(), targetStoryId, "after");
      if (response.equalsIgnoreCase("success")) {
        TasksUiInternal.synchronizeRepository(repository, true);
      } else if (response != null) {
        // show response string to user
        Status status = new Status(IStatus.ERROR, "Move Story Error", 0, response, null);
        ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Error While Moving Story!", status);
      }
    } catch (Exception e) {
      // show e.getMessage() to user
      Status status = new Status(IStatus.ERROR, "Move Story Error", 0, e.getMessage(), null);
      ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Error While Moving Story!", status);
    }
  }

}
