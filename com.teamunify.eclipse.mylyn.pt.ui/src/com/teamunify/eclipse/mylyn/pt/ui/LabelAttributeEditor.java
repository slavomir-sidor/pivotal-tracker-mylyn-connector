package com.teamunify.eclipse.mylyn.pt.ui;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.teamunify.eclipse.mylyn.pt.core.PtTaskAttribute;

/**
 * 
 * @author DL
 */

public class LabelAttributeEditor extends AbstractAttributeEditor {

  private List unselectedList;
  private List selectedList;
  private Button addButton;
  private Button removeButton;

  private TaskAttribute attrLabel;

  public LabelAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
    super(manager, taskAttribute);
    setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.MULTIPLE));
  }

  @Override
  public void createControl(Composite parent, FormToolkit toolkit) {
    GridComposite composite = new GridComposite(parent);
    setControl(composite);
  }

  class GridComposite extends Composite {
    public GridComposite(Composite c) {
      super(c, SWT.NO_FOCUS);
      GridLayout gl = new GridLayout();
      gl.numColumns = 3;
      this.setLayout(gl);

      unselectedList = new List(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
      attrLabel = getModel().getTaskData().getRoot().getMappedAttribute(PtTaskAttribute.TASK_LABEL);

      addButton = new Button(this, SWT.PUSH | SWT.BORDER);
      addButton.setText("> Add");
      addButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          String selected[] = unselectedList.getSelection();
          for (String element : selected) {
            System.out.println(element);
            unselectedList.remove(element);
            selectedList.add(element);
          }
          String selection[] = new String[selectedList.getItemCount()];
          for (int i = 0; i < selectedList.getItemCount(); i++) {
            selection[i] = selectedList.getItem(i);
          }
          attrLabel.setValues(Arrays.asList(selection));
        }
      });

      selectedList = new List(this, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
      Map<String, String> labelMap = attrLabel.getOptions();
      for (String item : attrLabel.getValues()) {
        selectedList.add(item);
      }

      Set<String> keys = labelMap.keySet();
      Iterator<String> keyIterator = keys.iterator();
      while (keyIterator.hasNext()) {
        String item = keyIterator.next();
        if (!attrLabel.getValues().contains(item)) {
          unselectedList.add(item);
        }

      }

      removeButton = new Button(this, SWT.PUSH | SWT.BORDER);
      removeButton.setText("< Remove");
      removeButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          String selected[] = selectedList.getSelection();
          for (String element : selected) {
            selectedList.remove(element);
            unselectedList.add(element);
          }
          String selection[] = new String[selectedList.getItemCount()];
          for (int i = 0; i < selectedList.getItemCount(); i++) {
            selection[i] = selectedList.getItem(i);
          }
          attrLabel.setValues(Arrays.asList(selection));
        }
      });

      GridData gd = new GridData(GridData.FILL_BOTH);
      gd.widthHint = 50;
      gd.heightHint = 100;
      gd.verticalSpan = 2;
      unselectedList.setLayoutData(gd);

      gd = new GridData(GridData.FILL_BOTH);
      gd.widthHint = 50;
      gd.heightHint = 100;
      gd.verticalSpan = 2;
      selectedList.setLayoutData(gd);

      gd = new GridData();
      gd.verticalIndent = 35;
      gd.widthHint = 55;
      gd.heightHint = 20;
      addButton.setLayoutData(gd);

      gd = new GridData();
      gd.widthHint = 55;
      gd.heightHint = 20;
      removeButton.setLayoutData(gd);

    }
  }

}
