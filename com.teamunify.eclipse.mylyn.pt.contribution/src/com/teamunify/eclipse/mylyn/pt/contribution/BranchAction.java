package com.teamunify.eclipse.mylyn.pt.contribution;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.ui.views.TaskListView;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import org.tigris.subversion.subclipse.ui.actions.WorkbenchWindowAction;
import org.tigris.subversion.subclipse.ui.operations.BranchTagOperation;
import org.tigris.subversion.subclipse.ui.wizards.ClosableWizardDialog;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import com.teamunify.eclipse.mylyn.pt.ui.PtConnectorUi;

/**
 * 
 * Class to open a Branch/Tag wizard and create a Branch in SVN
 * 
 * @author DL
 * 
 */
public class BranchAction extends WorkbenchWindowAction implements
		IViewActionDelegate {
	private IAction action;
	private IResource[] iResource = null;
	public static String svnUrl = null;

	protected void execute(IAction action) throws InvocationTargetException,
			InterruptedException {

		TaskListView taskListView = TaskListView.getFromActivePerspective();
		if (taskListView != null) {
			ISelection selection = taskListView.getViewer().getSelection();
			synchronizeSelected((IStructuredSelection) selection);
		}
		iResource = getSelectedIResources();
		if (iResource != null) {
			SVNBranchTagWizard wizard = new SVNBranchTagWizard(iResource);
			WizardDialog dialog = new ClosableWizardDialog(getShell(), wizard);
			if (dialog.open() == 0) {
				try {
					SVNUrl[] sourceUrls = wizard.getUrls();
					SVNUrl destinationUrl = wizard.getToUrl();
					String message = wizard.getComment();
					boolean createOnServer = wizard.isCreateOnServer();
					BranchTagOperation branchTagOperation = new BranchTagOperation(
							getTargetPart(), getSelectedIResources(),
							sourceUrls, destinationUrl, createOnServer,
							wizard.getRevision(), message);
					branchTagOperation.setMakeParents(wizard.isMakeParents());
					branchTagOperation.setNewAlias(wizard.getNewAlias());
					branchTagOperation.switchAfterTagBranchOperation(wizard
							.isSwitchAfterBranchTag());
					branchTagOperation.run();
				} catch (Exception localException) {
					localException.printStackTrace();
				}
			}
		}
	}

	private void synchronizeSelected(IStructuredSelection selection) {
		AbstractTask repositoryTask;
		for (@SuppressWarnings("rawtypes")
		Iterator localIterator = selection.toList().iterator(); localIterator
				.hasNext();) {
			Object obj = localIterator.next();
			if (obj instanceof ITask) {
				repositoryTask = (AbstractTask) obj;
				svnUrl = readSvnUrl() + (readSvnUrl().endsWith("/") ? "" : "/")
						+ "branches/" + repositoryTask.getTaskId();
			}
		}
	}

	public Shell getShell() {
		IWorkbench workbench = SVNUIPlugin.getPlugin().getWorkbench();
		if (workbench == null)
			return null;
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window == null)
			return null;
		return window.getShell();
	}

	private IResource[] getSelectedIResources() {
		ArrayList<IResource> resourceArray = new ArrayList<IResource>();
		IResource[] resources = (IResource[]) getSelectedAdaptables(
				SVNPtRepository.iStructuredSelection, IResource.class);
		for (int i = 0; i < resources.length; ++i)
			resourceArray.add(resources[i]);
		ResourceMapping[] resourceMappings = (ResourceMapping[]) getSelectedAdaptables(
				SVNPtRepository.iStructuredSelection, ResourceMapping.class);
		for (int i = 0; i < resourceMappings.length; ++i) {
			ResourceMapping resourceMapping = resourceMappings[i];
			try {
				ResourceTraversal[] traversals = resourceMapping.getTraversals(
						null, null);
				for (int j = 0; j < traversals.length; ++j) {
					IResource[] traversalResources = traversals[j]
							.getResources();
					for (int k = 0; k < traversalResources.length; ++k)
						if (!(resourceArray.contains(traversalResources[k])))
							resourceArray.add(traversalResources[k]);
				}
			} catch (CoreException e) {
				SVNUIPlugin.log(4, e.getMessage(), e);
			}
		}
		IResource[] selectedResources = new IResource[resourceArray.size()];

		resourceArray.toArray(selectedResources);
		return selectedResources;
	}

	@Override
	public void init(IViewPart view) {
		IActionBars actionBars = view.getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(),
				this.action);
		actionBars.updateActionBars();
	}

	@Override
	public void selectionChanged(IAction arg0, ISelection arg1) {

	}

	private String readSvnUrl() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(System.getProperty("user.dir")
					+ File.separator + "repository.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop.getProperty("repoUrl");
	}
}
