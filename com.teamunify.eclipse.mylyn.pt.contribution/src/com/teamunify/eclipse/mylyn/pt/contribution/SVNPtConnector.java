package com.teamunify.eclipse.mylyn.pt.contribution;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.tigris.subversion.subclipse.ui.SVNUIPlugin;
import com.teamunify.eclipse.mylyn.pt.ui.IPtConnectorUi;
import com.teamunify.eclipse.mylyn.pt.ui.IPtRepositoryPage;

/**
 * Class to add selection listener in SVNUIPlugin to track the project selection
 * 
 * @author DL
 * 
 */
public class SVNPtConnector implements IPtConnectorUi {
	//public static IStructuredSelection iStructuredSelection = null;
	
	public void intializeSelectionListener(){
	  try {
	      SVNUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getSelectionService()
	                      .addSelectionListener(listener);
	   } catch (Exception e) {

	    }

	}
	
	private final ISelectionListener listener = new ISelectionListener() {
	    public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
	      // we ignore our own selections
	      if (sourcepart.getTitle().equalsIgnoreCase("Project Explorer")
	          || sourcepart.getTitle().equalsIgnoreCase("Navigator")
	          || sourcepart.getTitle().equalsIgnoreCase("Package Explorer")) {
	        if (selection instanceof IStructuredSelection) {
	          
	          SVNPtRepository.iStructuredSelection = (IStructuredSelection) selection;
	        }
	      }

	    }
	  };
	
	}
