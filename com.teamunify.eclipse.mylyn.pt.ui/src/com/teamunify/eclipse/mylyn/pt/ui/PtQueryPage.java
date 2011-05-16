package com.teamunify.eclipse.mylyn.pt.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import com.teamunify.eclipse.mylyn.pt.core.PtConnector;
import com.teamunify.eclipse.mylyn.pt.core.PtCorePlugin;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PivotalTracker;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PtConfiguration;

public class PtQueryPage extends AbstractRepositoryQueryPage2 {

  private List storyTypeList;

  private List requestedByList;

  private List ownedByList;

  private List stateList;

  private Combo iterationTypeCombo;

  private String[] storyTypes;
  private String[] requestedBy;
  private String[] ownedBy;
  private String[] state;
  private String iterationType;
  private List labelList;
  private String[] labels;
  private Button invertLabelsCheckbox;

  boolean restoreCalled = false;

  public PtQueryPage(TaskRepository repository, IRepositoryQuery query) {
    super("pivotaltracker", repository, query);
    setTitle("Pivotal Tracker Search");
    setDescription("Specify search parameters.");
  }

  @Override
  protected void createPageContent(Composite parent) {
    Composite composite = new Composite(parent, SWT.BORDER);
    composite.setLayout(new GridLayout(4, false));

    // Row 1
    Label label = new Label(composite, SWT.NONE);
    label.setText("Iteration:");
    iterationTypeCombo = new Combo(composite, SWT.NONE);
    GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).applyTo(iterationTypeCombo);

    label = new Label(composite, SWT.NONE);
    label.setText("Type:");
    storyTypeList = new List(composite, SWT.MULTI | SWT.BORDER);
    storyTypeList.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
    GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(storyTypeList);

    // Row 2
    label = new Label(composite, SWT.NONE);
    label.setText("State:");
    stateList = new List(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(stateList);

    label = new Label(composite, SWT.NONE);
    label.setText("Owned By:");
    ownedByList = new List(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    GridDataFactory.fillDefaults().hint(100, 100).applyTo(ownedByList);

    // Row 3
    label = new Label(composite, SWT.NONE);
    label.setText("Requested By:");
    requestedByList = new List(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    GridDataFactory.fillDefaults().hint(100, 100).applyTo(requestedByList);

    label = new Label(composite, SWT.NONE);
    label.setText("Labels:");
    labelList = new List(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    GridDataFactory.fillDefaults().hint(100, 100).applyTo(labelList);

    // Final row
    invertLabelsCheckbox = new Button(composite, SWT.CHECK);
    invertLabelsCheckbox.setText("Inverse Label Match");
    GridDataFactory.fillDefaults().span(4, 1).align(SWT.RIGHT, SWT.TOP).applyTo(invertLabelsCheckbox);

    removeAddValue();
  }

  @Override
  protected void doRefresh() {
    // set default values to all fields
    if (restoreCalled == false) {
      removeAddValue();
    }
  }

  private void removeAddValue() {
    PtConfiguration configuration = getClient().getConfiguration();
    storyTypeList.removeAll();
    for (String member : configuration.getStoryTypes()) {
      storyTypeList.add(member);
    }
    requestedByList.removeAll();
    for (String member : configuration.getMembers()) {
      requestedByList.add(member);
    }
    ownedByList.removeAll();
    for (String member : configuration.getMembers()) {
      ownedByList.add(member);
    }
    stateList.removeAll();
    for (String member : configuration.getStoryStates()) {
      stateList.add(member);
    }
    iterationTypeCombo.removeAll();
    for (String member : configuration.getIterationTypes()) {
      iterationTypeCombo.add(member);
    }
    iterationTypeCombo.select(0);
    if (configuration.getLabels() != null) {
      labelList.removeAll();
      for (String member : configuration.getLabels()) {
        labelList.add(member);
      }
    }
    invertLabelsCheckbox.setSelection(false);
  }

  private PivotalTracker getClient() {
    try {
      return ((PtConnector) getConnector()).getPivotalTracker(getTaskRepository());
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  @Override
  protected boolean hasRepositoryConfiguration() {
    return getClient().hasConfiguration();
  }

  @Override
  protected boolean restoreState(IRepositoryQuery query) {
    storyTypes = query.getAttribute(PtCorePlugin.QUERY_KEY_STORY_TYPE).split("\\|");
    requestedBy = query.getAttribute(PtCorePlugin.QUERY_KEY_REQUESTED_BY).split("\\|");
    ownedBy = query.getAttribute(PtCorePlugin.QUERY_KEY_OWNED_BY).split("\\|");
    state = query.getAttribute(PtCorePlugin.QUERY_KEY_STATE).split("\\|");
    labels = query.getAttribute(PtCorePlugin.QUERY_KEY_LABEL).split("\\|");
    iterationType = query.getAttribute(PtCorePlugin.QUERY_KEY_ITERATION_TYPE);
    invertLabelsCheckbox.setSelection(booleanAttribute(query, PtCorePlugin.QUERY_KEY_LABEL_INVERSION));

    if (labels.length > 0) {
      labelList.setSelection(labels);
    }

    if (storyTypes.length > 0) {
      storyTypeList.setSelection(storyTypes);
    }
    if (requestedBy.length > 0) {
      requestedByList.setSelection(requestedBy);
    }
    if (ownedBy.length > 0) {
      ownedByList.setSelection(ownedBy);
    }

    if (state.length > 0) {
      stateList.setSelection(state);
    }
    if (iterationType != null) {
      iterationTypeCombo.setText(iterationType);
    }
    restoreCalled = true;
    return true;
  }

  private boolean booleanAttribute(IRepositoryQuery query, String key) {
    String value = query.getAttribute(key);
    if (value == null) { return false; }
    return Boolean.parseBoolean(value);
  }

  @Override
  public void applyTo(IRepositoryQuery query) {
    if (getQueryTitle() != null) {
      query.setSummary(getQueryTitle());
    }
    query.setAttribute(PtCorePlugin.QUERY_KEY_STORY_TYPE, getSelectionString(storyTypeList));
    query.setAttribute(PtCorePlugin.QUERY_KEY_REQUESTED_BY, getSelectionString(requestedByList));
    query.setAttribute(PtCorePlugin.QUERY_KEY_OWNED_BY, getSelectionString(ownedByList));
    query.setAttribute(PtCorePlugin.QUERY_KEY_STATE, getSelectionString(stateList));
    query.setAttribute(PtCorePlugin.QUERY_KEY_ITERATION_TYPE, iterationTypeCombo.getText());
    query.setAttribute(PtCorePlugin.QUERY_KEY_LABEL, getSelectionString(labelList));
    query.setAttribute(PtCorePlugin.QUERY_KEY_LABEL_INVERSION, Boolean.toString(invertLabelsCheckbox.getSelection()));
  }

  private String getSelectionString(List list) {
    String selectionString = "";
    int[] selection = list.getSelectionIndices();
    for (int element : selection) {
      selectionString += list.getItem(element) + "|";
    }
    if (selectionString.endsWith("|")) {
      selectionString = selectionString.substring(0, selectionString.length() - 1);
    }
    return selectionString;
  }
}
