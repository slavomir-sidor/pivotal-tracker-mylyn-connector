// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(5) braces fieldsfirst noctor nonlb space lnc 
// Source File Name:   TaskEditorNewCommentPart.java

package com.teamunify.eclipse.mylyn.pt.ui;

import java.util.Map;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import com.teamunify.eclipse.mylyn.pt.core.PtTaskAttribute;

public class PtAddTask extends AbstractTaskEditorPart {
  private static final int COLUMN_MARGIN = 5;

  public PtAddTask() {
    setPartName("Add Tasks");
  }

  @Override
  public void initialize(AbstractTaskEditorPage taskEditorPage) {
    super.initialize(taskEditorPage);
    // getModel().getTaskData().getRoot().getMappedAttribute(PtTaskAttribute.TASK_NEW);
  }

  @Override
  public void createControl(Composite parent, FormToolkit toolkit) {
    Section section = createSection(parent, toolkit, true);

    Composite sectionComposite = toolkit.createComposite(section);
    GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginTop = 2;
    layout.marginBottom = 8;
    layout.numColumns = 2;
    sectionComposite.setLayout(layout);

    Map<String, TaskAttribute> attributes = getTaskData().getRoot().getAttributes();

    addAttribute(sectionComposite, toolkit, attributes.get(PtTaskAttribute.TASK_NEW));
    toolkit.paintBordersFor(sectionComposite);
    section.setClient(sectionComposite);
    setSection(toolkit, section);

  }

  private AbstractAttributeEditor addAttribute(Composite composite, FormToolkit toolkit, TaskAttribute attribute) {
    AbstractAttributeEditor editor = createAttributeEditor(attribute);
    if (editor != null) {
      editor.createLabelControl(composite, toolkit);
      GridDataFactory.defaultsFor(editor.getLabelControl()).indent(COLUMN_MARGIN, 0).align(SWT.LEFT, SWT.TOP)
                     .applyTo(editor.getLabelControl());
      editor.createControl(composite, toolkit);
      getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
      String type = attribute.getMetaData().getType();
      if (TaskAttribute.TYPE_LONG_TEXT.equals(type)) {
        GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).hint(80, 95).minSize(SWT.DEFAULT, 40)
                       .applyTo(editor.getControl());
      } else {
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(editor.getControl());
      }
    }
    return editor;
  }
}
