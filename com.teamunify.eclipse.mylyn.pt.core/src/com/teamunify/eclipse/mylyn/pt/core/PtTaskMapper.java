package com.teamunify.eclipse.mylyn.pt.core;

import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

public class PtTaskMapper {

  private String taskId;

  private boolean status;

  private String text;

  public PtTaskMapper() {}

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public boolean isStatus() {
    return status;
  }

  public void setStatus(boolean status) {
    this.status = status;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void applyTo(TaskAttribute taskAttribute) {
    Assert.isNotNull(taskAttribute);
    TaskData taskData = taskAttribute.getTaskData();
    TaskAttributeMapper mapper = taskData.getAttributeMapper();
    taskAttribute.getMetaData().defaults().setType(PtTaskAttribute.TYPE_TASK);

    if (getTaskId() != null) {
      TaskAttribute child = taskAttribute.createMappedAttribute(PtTaskAttribute.ATTR_TASK_ID);
      child.getMetaData().defaults().setType(TaskAttribute.TYPE_INTEGER);
      mapper.setValue(child, getTaskId());
    }

    if (getText() != null) {
      TaskAttribute child = taskAttribute.createMappedAttribute(PtTaskAttribute.ATTR_TASK_DESC);
      child.getMetaData().defaults().setType(TaskAttribute.TYPE_LONG_TEXT).setReadOnly(false);
      mapper.setValue(child, getText());
    }

    TaskAttribute child = taskAttribute.createMappedAttribute(PtTaskAttribute.ATTR_TASK_STATUS);
    child.getMetaData().defaults().setType(TaskAttribute.TYPE_BOOLEAN).setReadOnly(false);

    mapper.setBooleanValue(child, isStatus());

  }

}
