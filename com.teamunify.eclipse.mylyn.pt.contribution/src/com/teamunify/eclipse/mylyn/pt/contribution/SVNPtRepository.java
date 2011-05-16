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
import com.teamunify.eclipse.mylyn.pt.ui.IPtRepositoryPage;

/**
 * Class to add a new field in Task Repository Page to receive the SVN Base URL
 * 
 * @author DL
 * 
 */
public class SVNPtRepository implements IPtRepositoryPage {
	public static StringFieldEditor svnBaseUrlEditor;
	public static IStructuredSelection iStructuredSelection = null;
	
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
	          
	          iStructuredSelection = (IStructuredSelection) selection;
	        }
	      }

	    }
	  };
	
	public void createSettingControls(Composite parent) {
		Label dummyLabel = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(dummyLabel);

		svnBaseUrlEditor = new StringFieldEditor(
				"", "SVN Base URL", StringFieldEditor.UNLIMITED, //$NON-NLS-1$
				parent) {

			@Override
			protected boolean doCheckState() {
				return true;

			}

			@Override
			protected void valueChanged() {
				super.valueChanged();
				try {
					writeSvnUrl(svnBaseUrlEditor.getStringValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public int getNumberOfControls() {
				return 3;
			}
		};

	}

	public void writeSvnUrl(String repoUrl) throws Exception {
		Properties properties = new Properties();
		properties.setProperty("repoUrl", repoUrl);
		properties.store(new FileOutputStream(System.getProperty("user.dir")
				+ File.separator + "repository.properties"), "");
	}
}
