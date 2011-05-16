/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.teamunify.eclipse.mylyn.pt.core;

import java.io.InputStream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import com.teamunify.eclipse.mylyn.pt.pivotaltracker.PivotalTracker;

public class PtAttachmentHandler extends AbstractTaskAttachmentHandler {

  private final PtConnector connector;

  public PtAttachmentHandler(PtConnector connector) {
    this.connector = connector;
  }

  @Override
  public boolean canGetContent(TaskRepository repository, ITask task) {
    // ignore
    return PtConnector.hasAttachmentSupport(repository, task);
  }

  @Override
  public boolean canPostContent(TaskRepository repository, ITask task) {
    // ignore
    return PtConnector.hasAttachmentSupport(repository, task);

  }

  @Override
  public InputStream getContent(TaskRepository repository, ITask task, TaskAttribute attachmentAttribute,
                                IProgressMonitor monitor) throws CoreException {
    // ignore

    TaskAttachmentMapper mapper = TaskAttachmentMapper.createFrom(attachmentAttribute);
    String filename = mapper.getFileName();
    if (filename == null || filename.length() == 0) { throw new CoreException(
                                                                              new RepositoryStatus(
                                                                                                   repository.getRepositoryUrl(),
                                                                                                   IStatus.ERROR,
                                                                                                   PtCorePlugin.ID_PLUGIN,
                                                                                                   RepositoryStatus.ERROR_REPOSITORY,
                                                                                                   "Attachment download from "
                                                                                                       + repository.getRepositoryUrl()
                                                                                                       + " failed, missing attachment filename.")); }

    try {
      // ITracClient client = connector.getClientManager().getTracClient(repository);
      PivotalTracker pivotalTracker = connector.getPivotalTracker(repository);
      int id = Integer.parseInt(task.getTaskId());
      // return client.getAttachmentData(id, filename, monitor);
      return pivotalTracker.getAttachment(id + "", mapper.getUrl());

    } catch (OperationCanceledException e) {
      throw e;
    } catch (Exception e) {
      // throw new CoreException(TracCorePlugin.toStatus(e, repository));
    }

    return null;
  }

  @Override
  public void postContent(TaskRepository repository, ITask task, AbstractTaskAttachmentSource source, String comment,
                          TaskAttribute attachmentAttribute, IProgressMonitor monitor) throws CoreException {
    // ignore

    if (!PtConnector.hasAttachmentSupport(repository, task)) { throw new CoreException(
                                                                                       new RepositoryStatus(
                                                                                                            repository.getRepositoryUrl(),
                                                                                                            IStatus.INFO,
                                                                                                            PtCorePlugin.ID_PLUGIN,
                                                                                                            RepositoryStatus.ERROR_REPOSITORY,
                                                                                                            "Attachments are not supported by this repository access type")); //$NON-NLS-1$
    }

    String filename = source.getName();
    String description = source.getDescription();
    if (attachmentAttribute != null) {
      TaskAttachmentMapper mapper = TaskAttachmentMapper.createFrom(attachmentAttribute);
      if (mapper.getFileName() != null) {
        filename = mapper.getFileName();
      }
      if (mapper.getDescription() != null) {
        description = mapper.getDescription();
      }

    }
    if (description == null) {
      description = ""; //$NON-NLS-1$
    }

    PivotalTracker pivotalTracker = connector.getPivotalTracker(repository);
    int id = Integer.parseInt(task.getTaskId());

    pivotalTracker.addAttachment(id + "", filename, source.getContentType(), source.createInputStream(monitor));

    /*
     * monitor = Policy.monitorFor(monitor);
     * try {
     * monitor.beginTask(Messages.TracAttachmentHandler_Uploading_attachment, IProgressMonitor.UNKNOWN);
     * try {
     * ITracClient client = connector.getClientManager().getTracClient(repository);
     * int id = Integer.parseInt(task.getTaskId());
     * client.putAttachmentData(id, filename, description, source.createInputStream(monitor), monitor);
     * if (comment != null && comment.length() > 0) {
     * TracTicket ticket = new TracTicket(id);
     * client.updateTicket(ticket, comment, monitor);
     * }
     * } catch (OperationCanceledException e) {
     * throw e;
     * } catch (Exception e) {
     * throw new CoreException(TracCorePlugin.toStatus(e, repository));
     * }
     * } finally {
     * monitor.done();
     * }
     */

  }

}
