package com.teamunify.eclipse.mylyn.pt.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelEvent;
import org.eclipse.mylyn.tasks.core.data.TaskDataModelListener;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import com.teamunify.eclipse.mylyn.pt.core.PtConnector;
import com.teamunify.eclipse.mylyn.pt.core.PtCorePlugin;
import com.teamunify.eclipse.mylyn.pt.core.PtTaskAttribute;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PivotalTracker;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PtConfiguration;

public class PtTaskEditorPage extends AbstractTaskEditorPage {

  PtConnector ptConnector;
  private final Map<TaskAttribute, AbstractAttributeEditor> attributeEditorMap;

  @Override
  protected AttributeEditorFactory createAttributeEditorFactory() {
    AttributeEditorFactory factory = new AttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite()) {

      @Override
      public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {
        AbstractAttributeEditor editor = null;
        if (PtTaskAttribute.TYPE_DOUBLE_LIST.equalsIgnoreCase(type)) {
          return new LabelAttributeEditor(getModel(), taskAttribute);
        } else {
          editor = super.createEditor(type, taskAttribute);
        }
        attributeEditorMap.put(taskAttribute, editor);
        return editor;
      }
    };
    return factory;
  }

  public PtTaskEditorPage(TaskEditor editor) {
    super(editor, "ptTaskEditorPage", "PivotalTracker", PtCorePlugin.CONNECTOR_KIND);
    attributeEditorMap = new HashMap<TaskAttribute, AbstractAttributeEditor>();
    setNeedsSubmitButton(true);
  }

  @Override
  protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
    Set<TaskEditorPartDescriptor> descriptors = super.createPartDescriptors();

    descriptors.add(new TaskEditorPartDescriptor("com.teamunify.eclipse.mylyn.pt.ui.PtTaskPart") {
      @Override
      public AbstractTaskEditorPart createPart() {
        return new PtTaskPart();
      }
    }.setPath(PATH_ATTRIBUTES));

    descriptors.add(new TaskEditorPartDescriptor("com.teamunify.eclipse.mylyn.pt.ui.PtAddTask") {
      @Override
      public AbstractTaskEditorPart createPart() {
        return new PtAddTask();

      }
    }.setPath(PATH_ATTRIBUTES));

    return descriptors;
  }

  @Override
  public void init(IEditorSite site, IEditorInput input) {

    super.init(site, input);
    ptConnector = new PtConnector();
    getModel().addModelListener(new ModelListener());

  }

  private class ModelListener extends TaskDataModelListener {

    @Override
    public void attributeChanged(TaskDataModelEvent paramTaskDataModelEvent) {

      TaskAttribute selectedAttribute = paramTaskDataModelEvent.getTaskAttribute();
      if (selectedAttribute.getId().equalsIgnoreCase("task.common.kind")) {
        TaskData taskData = selectedAttribute.getTaskData();
        try {
          PivotalTracker client = ptConnector.getPivotalTracker(taskData.getAttributeMapper().getTaskRepository());
          PtConfiguration configuration = client.getConfiguration();
          TaskAttribute attribute = taskData.getRoot().getAttribute(TaskAttribute.TASK_KIND);
          String value = attribute.getValue();
          TaskAttribute attribute1 = taskData.getRoot().getAttribute(TaskAttribute.PRODUCT);
          if (((value.equalsIgnoreCase("bug") || value.equalsIgnoreCase("chore")) && configuration.isBugsChoreEstimatable() == false)
              || value.equalsIgnoreCase("release")) {
            attribute1.clearOptions();
            attribute1.putOption("Unestimated", "Unestimated");
          } else {
            attribute1.clearOptions();
            for (String type : configuration.getEstimates()) {
              attribute1.putOption(type, type);
            }
          }
          attribute1.setValue("Unestimated");
          attributeEditorMap.get(attribute1).refresh();

          TaskAttribute attribute2 = taskData.getRoot().getAttribute(TaskAttribute.STATUS);
          if (value.equalsIgnoreCase("release")) {
            attribute2.clearOptions();
            for (String state : configuration.getReleaseStates()) {
              attribute2.putOption(state, state);
            }

          } else if (value.equalsIgnoreCase("chore")) {
            attribute2.clearOptions();
            for (String state : configuration.getChoreStates()) {
              attribute2.putOption(state, state);
            }
          } else {
            String[] states = configuration.getStoryStates();
            for (String state : states) {
              attribute2.putOption(state, state);
            }

          }
          attribute2.setValue("Not Yet Started");
          attributeEditorMap.get(attribute2).refresh();
        } catch (Exception e) {
          e.printStackTrace();
        }

      }
    }
  }
}
