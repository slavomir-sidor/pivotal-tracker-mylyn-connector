package com.teamunify.eclipse.mylyn.pt.ui;

import java.util.List;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import com.teamunify.eclipse.mylyn.pt.core.PtCorePlugin;
import com.teamunify.eclipse.mylyn.pt.core.PtTaskAttribute;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PivotalTracker;

/**
 * 
 * @author DL
 */
public class PtTaskPart extends AbstractTaskEditorPart {

  Section section;
  TaskRepository repository = null;

  public PtTaskPart() {
    setPartName("Tasks");
  }

  private AbstractAttributeEditor addAttribute(Composite composite, FormToolkit toolkit, TaskAttribute attribute,
                                               String taskId) {
    AbstractAttributeEditor editor = createAttributeEditor(attribute);
    if (editor != null) {
      editor.createControl(composite, toolkit);
      getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
      String type = attribute.getMetaData().getType();
      if (TaskAttribute.TYPE_LONG_TEXT.equals(type)) {
        GridDataFactory.fillDefaults().grab(true, true).indent(10, 0).align(SWT.FILL, SWT.FILL).hint(10, 40)
                       .minSize(SWT.DEFAULT, 40).applyTo(editor.getControl());
        editor.getControl().setData(taskId);
      } else {
        GridDataFactory.fillDefaults().applyTo(editor.getControl());
        editor.getControl().setData(taskId);
      }
    }
    return editor;
  }

  @Override
  public void createControl(Composite parent, FormToolkit toolkit) {
    repository = this.getTaskEditorPage().getTaskRepository();
    Button delete;
    section = createSection(parent, toolkit, true);
    Composite sectionComposite = toolkit.createComposite(section);
    GridLayout layout = new GridLayout(3, false);
    sectionComposite.setLayout(layout);
    List<TaskAttribute> taskAttributes = getTaskData().getAttributeMapper()
                                                      .getAttributesByType(getTaskData(), PtTaskAttribute.TYPE_TASK);

    section.setText("Tasks (" + taskAttributes.size() + ")");
    if (taskAttributes.size() > 0) {
      for (TaskAttribute commentAttribute : taskAttributes) {
        TaskAttribute attribute = commentAttribute.getMappedAttribute(PtTaskAttribute.ATTR_TASK_STATUS);
        TaskAttribute attribute1 = commentAttribute.getMappedAttribute(PtTaskAttribute.ATTR_TASK_ID);
        addAttribute(sectionComposite, toolkit, attribute, attribute1.getValue());
        attribute = commentAttribute.getMappedAttribute(PtTaskAttribute.ATTR_TASK_DESC);
        addAttribute(sectionComposite, toolkit, attribute, attribute1.getValue());
        delete = new Button(sectionComposite, SWT.PUSH);
        delete.setText("delete");
        delete.setData(attribute1.getValue());
        delete.addSelectionListener(new SelectionListeners());
      }
    }
    toolkit.paintBordersFor(sectionComposite);
    section.setClient(sectionComposite);
    setSection(toolkit, section);

  }

  class SelectionListeners implements SelectionListener {
    PivotalTracker pivotalTracker;

    SelectionListeners() {
      try {
        pivotalTracker = new PivotalTracker(repository.getProperty(PtCorePlugin.REPOSITORY_KEY_PATH),
                                            repository.getProperty(PtCorePlugin.REPOSITORY_KEY_USERNAME),
                                            repository.getProperty(PtCorePlugin.REPOSITORY_KEY_PASSWORD));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    public void widgetSelected(SelectionEvent paramSelectionEvent) {
      boolean flag;
      Button button = (Button) paramSelectionEvent.getSource();
      String buttonName = button.getText().toString();
      if (buttonName.equalsIgnoreCase("delete")) {
        flag = pivotalTracker.deleteTask(Integer.parseInt(getTaskData().getTaskId()),
                                         Integer.parseInt(button.getData().toString()));
        if (flag == true) {

          List<TaskAttribute> taskAttributes = getTaskData().getAttributeMapper()
                                                            .getAttributesByType(getTaskData(),
                                                                                 PtTaskAttribute.TYPE_TASK);

          if (taskAttributes.size() > 0) {
            for (TaskAttribute commentAttribute : taskAttributes) {
              if (commentAttribute.getId().contains(button.getData().toString())) {
                getTaskData().getRoot().removeAttribute(commentAttribute.getId());

              }
            }
          }
          taskAttributes = getTaskData().getAttributeMapper().getAttributesByType(getTaskData(),
                                                                                  PtTaskAttribute.TYPE_TASK);
          Composite composite = button.getParent();
          Control[] controls = composite.getChildren();
          for (Control control : controls) {
            if (control.getData().toString().equalsIgnoreCase(button.getData().toString())) {
              control.dispose();
            }

          }
          section.setText("Tasks (" + taskAttributes.size() + ")");

        }

      }

    }

    public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {}

  }

}
