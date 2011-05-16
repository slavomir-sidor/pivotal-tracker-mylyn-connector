package com.teamunify.eclipse.mylyn.pt.ui;

import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;
import com.teamunify.eclipse.mylyn.pt.core.PtCorePlugin;

public class PtTaskEditorPageFactory extends AbstractTaskEditorPageFactory {

  @Override
  public boolean canCreatePageFor(TaskEditorInput input) {
    return (input.getTask().getConnectorKind().equals(PtCorePlugin.CONNECTOR_KIND) || TasksUiUtil.isOutgoingNewTask(input.getTask(),
                                                                                                                    PtCorePlugin.CONNECTOR_KIND));
  }

  @Override
  public IFormPage createPage(TaskEditor editor) {
    return new PtTaskEditorPage(editor);
  }

  @Override
  public String[] getConflictingIds(TaskEditorInput input) {
    if (!input.getTask().getConnectorKind().equals(PtCorePlugin.CONNECTOR_KIND)) { return new String[] { ITasksUiConstants.ID_PAGE_PLANNING }; }
    return null;
  }

  @Override
  public Image getPageImage() {
    return CommonImages.getImage(TasksUiImages.REPOSITORY_SMALL);
  }

  @Override
  public String getPageText() {
    return "PivotalTracker";
  }

  @Override
  public int getPriority() {
    return PRIORITY_TASK;
  }

}
