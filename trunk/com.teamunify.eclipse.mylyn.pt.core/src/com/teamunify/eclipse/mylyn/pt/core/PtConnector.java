package com.teamunify.eclipse.mylyn.pt.core;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.eclipse.osgi.util.NLS;
import com.teamunify.eclipse.mylyn.pt.core.util.PtQueryFilter;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PivotalTracker;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.Story;

public class PtConnector extends AbstractRepositoryConnector {

  private static Map<TaskRepository, PivotalTracker> clientByRepository1 = new HashMap<TaskRepository, PivotalTracker>();

  private final PtTaskDataHandler taskDataHandler;

  private final PtAttachmentHandler attachmentHandler = new PtAttachmentHandler(this);

  public synchronized PivotalTracker getPivotalTracker(TaskRepository repository) throws CoreException {

    PivotalTracker client = clientByRepository1.get(repository);
    if (client == null) {
      try {
        client = new PivotalTracker(repository.getProperty(PtCorePlugin.REPOSITORY_KEY_PATH),
                                    repository.getProperty(PtCorePlugin.REPOSITORY_KEY_USERNAME),
                                    repository.getProperty(PtCorePlugin.REPOSITORY_KEY_PASSWORD));

        if (client.getErrmsg() == "") {
          clientByRepository1.put(repository, client);
        }
      } catch (Exception e) {
        throw new CoreException(new Status(IStatus.ERROR, PtCorePlugin.ID_PLUGIN, NLS.bind(e.getMessage(), ""), e));
      }
    }
    return client;
  }

  /*
   * || (!client.getUserName().equalsIgnoreCase(repository.getProperty(PtCorePlugin.REPOSITORY_KEY_PATH))
   * && !client.getPassword().equalsIgnoreCase(repository.getProperty(PtCorePlugin.REPOSITORY_KEY_PASSWORD)) &&
   * !client.getProjectId()
   * .equalsIgnoreCase(repository.getProperty(PtCorePlugin.REPOSITORY_KEY_PATH))
   */
  public PtConnector() {
    taskDataHandler = new PtTaskDataHandler(this);
  }

  @Override
  public boolean canCreateNewTask(TaskRepository repository) {
    return true;
  }

  @Override
  public boolean canCreateTaskFromKey(TaskRepository repository) {
    return true;
  }

  @Override
  public String getConnectorKind() {
    return PtCorePlugin.CONNECTOR_KIND;
  }

  @Override
  public String getLabel() {
    return "Pivotal Tracker Connector";
  }

  @Override
  public String getRepositoryUrlFromTaskUrl(String taskFullUrl) {
    // ignore
    return null;
  }

  @Override
  public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor) throws CoreException {
    Story story = null;
    try {
      story = getPivotalTracker(repository).getStoryById(taskId);
      Thread.sleep(500);
    } catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, PtCorePlugin.ID_PLUGIN, NLS.bind(e.getMessage(), ""), e));
    }
    return taskDataHandler.readTaskData(repository, story, monitor);
  }

  @Override
  public String getTaskIdFromTaskUrl(String taskFullUrl) {
    // ignore
    return null;
  }

  @Override
  public String getTaskUrl(String repositoryUrl, String taskId) {
    return null;
  }

  @Override
  public boolean hasTaskChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
    // compare date on task (local state) and taskData (repository state)
    TaskAttribute attribute = taskData.getRoot().getAttribute(TaskAttribute.DATE_MODIFICATION);
    if (attribute != null) {
      Date dataModificationDate = taskData.getAttributeMapper().getDateValue(attribute);
      if (dataModificationDate != null) {
        Date taskModificationDate = task.getModificationDate();
        if (taskModificationDate != null) { return !taskModificationDate.equals(dataModificationDate); }
      }
    }

    return true;
  }

  @Override
  public IStatus performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector collector,
                              ISynchronizationSession session, IProgressMonitor monitor) {

    PtQueryFilter filter = new PtQueryFilter(query);
    try {
      List<Story> storyList = getPivotalTracker(repository).getCurrentIterations(query.getAttribute(PtCorePlugin.QUERY_KEY_ITERATION_TYPE));
      for (Story story : storyList) {

        TaskData taskData = taskDataHandler.parseDocument(repository, story, monitor, ""
                                                                                      + (storyList.indexOf(story) + 1));
        taskData.getRoot().getAttribute(TaskAttribute.TASK_URL).setValue(query.getSummary());

        // set to true if repository does not return full task details
        // taskData.setPartial(true);
        if (filter.accepts(taskData)) {
          collector.accept(taskData);
        }
      }
    } catch (Exception e) {
      return new Status(IStatus.ERROR, PtCorePlugin.ID_PLUGIN, NLS.bind("Query failed: ''{0}''", e.getMessage()), e);
    }
    return Status.OK_STATUS;
  }

  @Override
  public void updateRepositoryConfiguration(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
    try {
      getPivotalTracker(repository).updateConfiguration(monitor);
    } catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, PtCorePlugin.ID_PLUGIN, NLS.bind(e.getMessage(), ""), e));
    }
  }

  @Override
  public void updateTaskFromTaskData(TaskRepository repository, ITask task, TaskData taskData) {
    task.setAttribute(TaskAttribute.RANK, PtTaskDataHandler.rank);
    getTaskMapping(taskData).applyTo(task);

  }

  @Override
  public TaskMapper getTaskMapping(TaskData taskData) {
    return new TaskMapper(taskData);
  }

  @Override
  public void preSynchronization(ISynchronizationSession session, IProgressMonitor monitor) throws CoreException {

    try {
      monitor.beginTask("Getting changed tasks", IProgressMonitor.UNKNOWN);

      if (!session.isFullSynchronization()) { return; }

      // there are no Trac tasks in the task list, skip contacting the repository
      if (session.getTasks().isEmpty()) { return; }

      TaskRepository repository = session.getTaskRepository();

      if (repository.getSynchronizationTimeStamp() == null || repository.getSynchronizationTimeStamp().length() == 0) {
        for (ITask task : session.getTasks()) {
          session.markStale(task);
        }
        return;
      }

      try {
        PivotalTracker pivotalTracker = getPivotalTracker(repository);

        Set<Integer> ids = pivotalTracker.getActivityStoryIds(new Long(repository.getSynchronizationTimeStamp()));
        if (ids.isEmpty()) {
          // repository is unchanged
          // session.setNeedsPerformQueries(false);
          return;
        }

        for (ITask task : session.getTasks()) {
          Integer id = Integer.parseInt(task.getTaskId());
          if (ids.contains(id)) {
            session.markStale(task);
          }
        }
      } catch (Exception e) {
        throw new CoreException(new Status(IStatus.ERROR, PtCorePlugin.ID_PLUGIN, NLS.bind(e.getMessage(), ""), e));
      }
    } finally {
      monitor.done();
    }
  }

  @Override
  public void postSynchronization(ISynchronizationSession event, IProgressMonitor monitor) throws CoreException {

    try {
      monitor.beginTask("", 1);
      if (event.isFullSynchronization() && event.getStatus() == null) {
        PivotalTracker pivtoal = getPivotalTracker(event.getTaskRepository());
        Date date = pivtoal.getLastActivityAt();
        if (date != null) {
          event.getTaskRepository().setSynchronizationTimeStamp(date.getTime() / 1000l + "");
        }
      }
    } catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, PtCorePlugin.ID_PLUGIN, NLS.bind(e.getMessage(), ""), e));
    } finally {
      monitor.done();
    }
  }

  @Override
  public AbstractTaskDataHandler getTaskDataHandler() {
    return taskDataHandler;
  }

  @Override
  public PtAttachmentHandler getTaskAttachmentHandler() {
    return attachmentHandler;
  }

  public static boolean hasAttachmentSupport(TaskRepository repository, ITask task) {
    return true;
  }

  @Override
  public boolean canDeleteTask(TaskRepository repository, ITask task) {
    return true;
  }

  @Override
  public IStatus deleteTask(TaskRepository repository, ITask task, IProgressMonitor monitor) throws CoreException {
    boolean flag = getPivotalTracker(repository).deleteStory(Integer.parseInt(task.getTaskId()));
    if (flag) {
      return Status.OK_STATUS;
    } else {
      return null;
    }
  }
}
