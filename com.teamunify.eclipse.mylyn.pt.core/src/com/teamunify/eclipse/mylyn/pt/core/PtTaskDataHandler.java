package com.teamunify.eclipse.mylyn.pt.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.osgi.util.NLS;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.Attachment;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.Attachments;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.NoteData;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.Notes;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PivotalTracker;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PtConfiguration;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.Story;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.Task;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.Tasks;

public class PtTaskDataHandler extends AbstractTaskDataHandler {

  private final PtConnector connector;

  public static String rank;

  public PtTaskDataHandler(PtConnector connector) {
    this.connector = connector;
  }

  @Override
  public TaskAttributeMapper getAttributeMapper(TaskRepository repository) {
    return new TaskAttributeMapper(repository);
  }

  @Override
  public boolean initializeTaskData(TaskRepository repository, TaskData taskData, ITaskMapping initializationData,
                                    IProgressMonitor monitor) throws CoreException {
    PivotalTracker client = connector.getPivotalTracker(repository);
    PtConfiguration configuration = client.getConfiguration();

    TaskAttribute attribute = taskData.getRoot().createAttribute(TaskAttribute.SUMMARY);
    attribute.getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_SHORT_RICH_TEXT).setLabel("Summary:");

    attribute = taskData.getRoot().createAttribute(TaskAttribute.TASK_URL);

    attribute = taskData.getRoot().createAttribute(TaskAttribute.DATE_COMPLETION);
    attribute.getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_DATE);

    attribute = taskData.getRoot().createAttribute(TaskAttribute.DESCRIPTION);
    attribute.getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_LONG_RICH_TEXT).setLabel("Description:");

    attribute = taskData.getRoot().createAttribute(TaskAttribute.TASK_KIND);
    attribute.getMetaData().setReadOnly(false).setKind(TaskAttribute.KIND_DEFAULT)
             .setType(TaskAttribute.TYPE_SINGLE_SELECT).setLabel("Story Type :");

    String[] storyType = configuration.getStoryTypes();
    for (String story : storyType) {
      attribute.putOption(story, story);
    }
    attribute.setValue("Feature");

    attribute = taskData.getRoot().createAttribute(TaskAttribute.PRODUCT);
    attribute.getMetaData().setReadOnly(false).setKind(TaskAttribute.KIND_DEFAULT)
             .setType(TaskAttribute.TYPE_SINGLE_SELECT).setLabel("Estimate");

    String[] estimates = configuration.getEstimates();
    for (String estimate : estimates) {
      if (estimate.equalsIgnoreCase("1 points")) {
        estimate = "1 point";
      }

      attribute.putOption(estimate, estimate);
    }
    attribute.setValue("Unestimated");

    attribute = taskData.getRoot().createAttribute(TaskAttribute.USER_ASSIGNED);
    attribute.getMetaData().setReadOnly(false).setKind(TaskAttribute.KIND_DEFAULT)
             .setType(TaskAttribute.TYPE_SINGLE_SELECT).setLabel("Requested By :");
    List<String> members = configuration.getMembers();
    for (String member : members) {
      attribute.putOption(member, member);
    }
    if (configuration.getRequestedBy() != null) {
      attribute.setValue(configuration.getRequestedBy());
    }

    attribute = taskData.getRoot().createAttribute(TaskAttribute.STATUS);
    attribute.getMetaData().setReadOnly(true).setKind(TaskAttribute.KIND_DEFAULT)
             .setType(TaskAttribute.TYPE_SINGLE_SELECT).setLabel("Status :");
    // taskData.getRoot().getAttribute(TaskAttribute.STATUS).getMetaData().setReadOnly(true);
    String[] states = configuration.getStoryStates();
    for (String state : states) {
      attribute.putOption(state, state);
    }
    attribute.setValue("Not Yet Started");
    // Set Status to read only
    attribute = taskData.getRoot().createAttribute(TaskAttribute.USER_REPORTER);
    attribute.getMetaData().setReadOnly(false).setKind(TaskAttribute.KIND_DEFAULT)
             .setType(TaskAttribute.TYPE_SINGLE_SELECT).setLabel("Owned By : ");
    for (String member : members) {
      attribute.putOption(member, member);
    }

    attribute = taskData.getRoot().createAttribute(TaskAttribute.PRIORITY);
    attribute.getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_LONG_RICH_TEXT).setLabel("Task Key :");

    // Added to display the comments

    attribute = taskData.getRoot().createAttribute(TaskAttribute.DATE_MODIFICATION);
    attribute.getMetaData().setReadOnly(false).setType(TaskAttribute.TYPE_DATETIME).setLabel("Modified:");

    taskData.getRoot().createAttribute(TaskAttribute.COMMENT_NEW).getMetaData()
            .setType(TaskAttribute.TYPE_LONG_RICH_TEXT).setReadOnly(false);

    attribute = taskData.getRoot().createAttribute(PtTaskAttribute.TASK_LABEL);
    attribute.getMetaData().setReadOnly(false).setKind(TaskAttribute.KIND_DEFAULT)
             .setType(PtTaskAttribute.TYPE_DOUBLE_LIST).setLabel("Labels:");

    if (configuration.getLabels() != null) {
      String[] labels = configuration.getLabels();
      for (String label : labels) {
        attribute.putOption(label, label);
      }
    }

    taskData.getRoot().createAttribute(PtTaskAttribute.TASK_NEW).getMetaData()
            .setType(TaskAttribute.TYPE_LONG_RICH_TEXT).setReadOnly(false);
    return true;
  }

  @Override
  public RepositoryResponse postTaskData(TaskRepository repository, TaskData taskData,
                                         Set<TaskAttribute> oldAttributes, IProgressMonitor monitor)
                                                                                                    throws CoreException {
    PivotalTracker pivotalTracker = connector.getPivotalTracker(repository);
    String taskId = taskData.getTaskId();

    Story storyData = new Story();

    TaskAttribute taskAttribute = taskData.getRoot().getAttribute(TaskAttribute.SUMMARY);

    if (taskAttribute.getValue() == null || taskAttribute.getValue().isEmpty()) {
      throw new CoreException(new Status(IStatus.ERROR, PtCorePlugin.ID_PLUGIN,
                                         NLS.bind("Story Title cannot be Empty !", "")));
    } else {
      storyData.setName(taskAttribute.getValue());
    }

    taskAttribute = taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION);
    storyData.setDescription(taskAttribute.getValue());
    taskAttribute = taskData.getRoot().getAttribute(TaskAttribute.USER_ASSIGNED);
    storyData.setRequested_by(taskAttribute.getValue());
    taskAttribute = taskData.getRoot().getAttribute(TaskAttribute.PRODUCT);
    String estimate = taskAttribute.getValue();
    int estimateValue;
    if (estimate.equalsIgnoreCase("Unestimated")) {
      estimateValue = -1;
    } else {
      estimateValue = new Integer(estimate.substring(0, 1));
    }
    storyData.setEstimate(estimateValue);

    taskAttribute = taskData.getRoot().getAttribute(TaskAttribute.TASK_KIND);
    String storyType = taskAttribute.getValue().toLowerCase();
    storyData.setStory_type(storyType);

    taskAttribute = taskData.getRoot().getAttribute(TaskAttribute.STATUS);
    String stateValue = taskAttribute.getValue();
    if (stateValue.equalsIgnoreCase("Not Yet Started")) {
      stateValue = "unstarted";
    } else {
      stateValue = stateValue.toLowerCase();
    }
    storyData.setCurrent_state(stateValue);

    taskAttribute = taskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER);
    storyData.setOwned_by(taskAttribute.getValue());

    String newComment = ""; //$NON-NLS-1$
    TaskAttribute newCommentAttribute = taskData.getRoot().getMappedAttribute(TaskAttribute.COMMENT_NEW);
    if (newCommentAttribute != null) {
      newComment = newCommentAttribute.getValue();
    }
    NoteData noteData = new NoteData();
    noteData.setText(newComment);

    NoteData noteDataArray[] = new NoteData[1];
    noteDataArray[0] = noteData;

    Notes notes = new Notes();
    notes.setNote(noteDataArray);

    storyData.setNotes(notes);

    taskAttribute = taskData.getRoot().getAttribute(PtTaskAttribute.TASK_LABEL);
    String labels = "";

    for (String value : taskAttribute.getValues()) {
      labels = value + "," + labels;
    }
    if (labels.endsWith(",")) {
      labels = labels.substring(0, labels.length() - 1);
    }
    storyData.setLabels(labels);

    List<TaskAttribute> pttaskAttributes = taskData.getAttributeMapper().getAttributesByType(taskData,

    PtTaskAttribute.TYPE_TASK);
    Tasks tasks = new Tasks();
    Task[] taskArray = null;

    if (pttaskAttributes.size() > 0) {
      taskArray = new Task[pttaskAttributes.size() + 1];

    }
    PtConfiguration configuration = pivotalTracker.getConfiguration();
    List<String> members = configuration.getMembersEmail();
    /*
     * if (!members.contains(repository.getUserName())) { throw new CoreException(
     * new Status(
     * IStatus.ERROR,
     * PtCorePlugin.ID_PLUGIN,
     * NLS.bind("Project memebers can only edit the tasks...",
     * "")));
     * }
     */
    if (taskArray != null) {
      for (int i = 0; i < pttaskAttributes.size(); i++) {
        TaskAttribute pttaskAttribute = pttaskAttributes.get(i);
        Task task = new Task();
        TaskAttribute attribute = pttaskAttribute.getMappedAttribute(PtTaskAttribute.ATTR_TASK_ID);
        task.setId(new Integer(attribute.getValue()));
        task.setDescription(pttaskAttribute.getMappedAttribute(PtTaskAttribute.ATTR_TASK_DESC).getValue());
        attribute = pttaskAttribute.getMappedAttribute(PtTaskAttribute.ATTR_TASK_STATUS);
        boolean status = attribute.getValue().equalsIgnoreCase("true") ? true : false;
        if (stateValue.equalsIgnoreCase("finished") && status == false) { throw new CoreException(
                                                                                                  new Status(
                                                                                                             IStatus.ERROR,
                                                                                                             PtCorePlugin.ID_PLUGIN,
                                                                                                             NLS.bind("Please check all tasks when status is finished!",
                                                                                                                      "")));

        }
        task.setComplete(status);
        taskArray[i] = task;
      }
    }
    if (taskArray == null) {
      taskArray = new Task[1];
    }

    Task task1 = new Task();
    taskAttribute = taskData.getRoot().getAttribute(PtTaskAttribute.TASK_NEW);
    if (!taskAttribute.getValue().isEmpty()) {
      task1.setDescription(taskAttribute.getValue());
      taskArray[taskArray.length - 1] = task1;
    }
    tasks.setTask(taskArray);
    storyData.setTasks(tasks);

    String oldStatus = "";
    Iterator<TaskAttribute> iterate = oldAttributes.iterator();
    while (iterate.hasNext()) {
      TaskAttribute oldAttribute = iterate.next();
      if (oldAttribute.getId().equalsIgnoreCase(TaskAttribute.STATUS)) {
        oldStatus = oldAttribute.getValue();

        break;
      }
    }
    if (!stateValue.equalsIgnoreCase("unstarted") && storyType.equalsIgnoreCase("feature") && estimateValue == -1) { throw new CoreException(
                                                                                                                                             new Status(
                                                                                                                                                        IStatus.ERROR,
                                                                                                                                                        PtCorePlugin.ID_PLUGIN,
                                                                                                                                                        NLS.bind("Please estimate the story before moving to {0}",
                                                                                                                                                                 stateValue)));

    }

    if (!storyType.equalsIgnoreCase("release") && oldStatus.equalsIgnoreCase("not yet started")
        && !stateValue.equalsIgnoreCase("started") && !stateValue.equalsIgnoreCase("unstarted")) { throw new CoreException(
                                                                                                                           new Status(
                                                                                                                                      IStatus.ERROR,
                                                                                                                                      PtCorePlugin.ID_PLUGIN,
                                                                                                                                      NLS.bind("Please start the story before you {0}",
                                                                                                                                               stateValue))); }
    if ((storyType.equalsIgnoreCase("bug") || (storyType.equalsIgnoreCase("chore"))) && estimateValue == -1
        && stateValue.equalsIgnoreCase("started") && configuration.isBugsChoreEstimatable() != false) { throw new CoreException(
                                                                                                                                new Status(
                                                                                                                                           IStatus.ERROR,
                                                                                                                                           PtCorePlugin.ID_PLUGIN,
                                                                                                                                           NLS.bind("Please estimate the story before moving to {0}",
                                                                                                                                                    stateValue)));

    }

    try {
      if (taskData.isNew()) {
        taskId = pivotalTracker.addStory(storyData);
        storyData.setId(Integer.parseInt(taskId));
        return new RepositoryResponse(ResponseKind.TASK_CREATED, taskId);
      } else {
        storyData.setId(new Integer(taskId));
        pivotalTracker.updateStory(storyData);
        return new RepositoryResponse(ResponseKind.TASK_UPDATED, taskId);
      }
    } catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, PtCorePlugin.ID_PLUGIN, NLS.bind("{0}", e.getMessage()),
                                         new Exception()));
    }
  }

  public TaskData readTaskData(TaskRepository repository, Story story, IProgressMonitor monitor) throws CoreException {
    try {
      TaskData taskData = parseDocument(repository, story, monitor, "");
      return taskData;
    } catch (Exception e) {
      throw new CoreException(new Status(IStatus.ERROR, PtCorePlugin.ID_PLUGIN, NLS.bind(e.getMessage(), ""), e));
    }
  }

  public TaskData parseDocument(TaskRepository repository, Story story, IProgressMonitor monitor, String priority)
                                                                                                                  throws Exception {
    TaskData taskData = null;
    PivotalTracker client = connector.getPivotalTracker(repository);
    PtConfiguration configuration = client.getConfiguration();
    try {
      DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
      String taskId = story.getId() + "";

      taskData = new TaskData(getAttributeMapper(repository), repository.getConnectorKind(),
                              repository.getRepositoryUrl(), taskId);
      initializeTaskData(repository, taskData, null, monitor);
      TaskAttribute attribute = taskData.getRoot().getAttribute(TaskAttribute.SUMMARY);
      attribute.setValue(story.getName());
      attribute = taskData.getRoot().getAttribute(TaskAttribute.DESCRIPTION);
      attribute.setValue(story.getDescription());
      attribute = taskData.getRoot().getAttribute(TaskAttribute.PRODUCT);
      String estimate = "";
      if (((story.getStory_type().equalsIgnoreCase("bug") || story.getStory_type().equalsIgnoreCase("chore")) && configuration.isBugsChoreEstimatable() == false)
          || story.getStory_type().equalsIgnoreCase("release")) {
        attribute.clearOptions();
        attribute.putOption("Unestimated", "Unestimated");
        estimate = "Unestimated";
      }

      if (story.getEstimate() == -1) {
        estimate = "Unestimated";
      } else if (story.getEstimate() == 1) {
        estimate = story.getEstimate() + " point";
      } else if (estimate.equalsIgnoreCase("")) {
        estimate = story.getEstimate() + " points";
      }
      attribute.setValue(estimate);
      attribute = taskData.getRoot().getAttribute(TaskAttribute.TASK_KIND);
      String storytype = story.getStory_type();
      storytype = storytype.substring(0, 1).toUpperCase() + storytype.substring(1, storytype.length());
      attribute.setValue(storytype);
      attribute = taskData.getRoot().getAttribute(TaskAttribute.USER_ASSIGNED);
      attribute.setValue(story.getRequested_by());
      attribute = taskData.getRoot().getAttribute(TaskAttribute.STATUS);
      String stateValue = story.getCurrent_state();
      if (stateValue.equalsIgnoreCase("unstarted")) {
        stateValue = "Not Yet Started";
      } else {
        stateValue = stateValue.substring(0, 1).toUpperCase() + stateValue.substring(1, stateValue.length());
      }
      attribute.setValue(stateValue);

      attribute = taskData.getRoot().getAttribute(TaskAttribute.TASK_KIND);

      if (attribute.getValue().equalsIgnoreCase("release")) {
        attribute = taskData.getRoot().getAttribute(TaskAttribute.STATUS);
        attribute.clearOptions();
        for (String status : configuration.getReleaseStates()) {
          attribute.putOption(status, status);
        }
        attribute = taskData.getRoot().getAttribute(TaskAttribute.STATUS);
      } else if (attribute.getValue().equalsIgnoreCase("chore")) {
        attribute = taskData.getRoot().getAttribute(TaskAttribute.STATUS);
        attribute.clearOptions();
        for (String status : configuration.getChoreStates()) {
          attribute.putOption(status, status);
        }
      } else {
        String[] states = configuration.getStoryStates();
        attribute = taskData.getRoot().getAttribute(TaskAttribute.STATUS);
        attribute.clearOptions();
        for (String state : states) {
          attribute.putOption(state, state);
        }
      }

      boolean complete = (stateValue.equalsIgnoreCase("finished") || stateValue.equalsIgnoreCase("delivered") || stateValue.equalsIgnoreCase("accepted"));
      attribute = taskData.getRoot().getAttribute(TaskAttribute.DATE_COMPLETION);
      taskData.getAttributeMapper().setDateValue(attribute, complete ? new Date() : null);

      attribute = taskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER);
      if (story.getOwned_by() != null) {
        attribute.setValue(story.getOwned_by());
      } else {
        attribute.setValue("");
      }

      attribute = taskData.getRoot().getAttribute(TaskAttribute.PRIORITY);
      attribute.setValue(story.getIterationType());
      rank = priority;

      // Added to display comments
      Notes notes = story.getNotes();
      if (notes != null) {

        NoteData elements[] = notes.getNote();
        if (elements != null) {
          for (NoteData noteData : elements) {
            TaskCommentMapper mapper = new TaskCommentMapper(); // Create a new one each time, to be safe.
            mapper.setAuthor(repository.createPerson(noteData.getAuthor()));
            Date date = null;
            date = formatter.parse(noteData.getNoted_at());
            mapper.setCreationDate(date);
            mapper.setText(noteData.getText());
            mapper.setNumber(noteData.getId());
            attribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_COMMENT + noteData.getId());
            mapper.applyTo(attribute);
          }

        }
      }
      if (taskId != "") {
        taskData.getRoot().getAttribute(TaskAttribute.STATUS).getMetaData().setReadOnly(false);
      }
      Attachments attachment = story.getAttachments();
      if (attachment != null) {
        Attachment elements[] = attachment.getAttachment();
        if (elements != null) {
          for (Attachment attachmentElement : elements) {
            TaskAttachmentMapper mapper = new TaskAttachmentMapper();
            mapper.setAuthor(repository.createPerson(attachmentElement.getUploaded_by()));
            mapper.setFileName(attachmentElement.getFilename());
            if (attachmentElement.getFilename().equalsIgnoreCase("mylyn-context.zip")) {
              mapper.setDescription("mylyn/context/zip");
            } else {
              mapper.setDescription(attachmentElement.getDescription());
            }
            mapper.setUrl(attachmentElement.getUrl());
            mapper.setCreationDate(formatter.parse(attachmentElement.getUploaded_at()));
            mapper.setAttachmentId(attachmentElement.getId() + "");
            attribute = taskData.getRoot().createAttribute(TaskAttribute.PREFIX_ATTACHMENT + attachmentElement.getId());
            mapper.applyTo(attribute);
          }
        }
      }

      if (story.getUpdated_at() != null) {
        Date date = formatter.parse(story.getUpdated_at());
        attribute = taskData.getRoot().getAttribute(TaskAttribute.DATE_MODIFICATION);
        taskData.getAttributeMapper().setDateValue(attribute, date);
      }

      if (story.getLabels() != null) {
        List<String> labels = Arrays.asList(story.getLabels().split(","));
        attribute = taskData.getRoot().getAttribute(PtTaskAttribute.TASK_LABEL);
        attribute.setValues(labels);
      }

      attribute = taskData.getRoot().getAttribute(PtTaskAttribute.TASK_NEW);
      taskData.getAttributeMapper().setValue(attribute, "");
      Tasks tasks = story.getTasks();

      if (tasks != null) {
        for (Task task : tasks.getTask()) {
          PtTaskMapper mapper = new PtTaskMapper();
          mapper.setTaskId(task.getId() + "");
          mapper.setStatus(task.isComplete());
          mapper.setText(task.getDescription());
          attribute = taskData.getRoot().createAttribute(PtTaskAttribute.ATTR_PREFIX_TASK + task.getId());
          mapper.applyTo(attribute);
        }
      }
    } catch (Exception e) {
      throw new Exception("Error while reading story from PT");
    }
    return taskData;
  }
}
