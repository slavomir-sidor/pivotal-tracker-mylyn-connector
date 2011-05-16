package com.teamunify.eclipse.mylyn.pt.pivotaltracker;

/**
 * @author DL
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PtConfiguration {

  long updated = -1;

  private List<String> members = Collections.emptyList();

  private List<String> membersEmail = Collections.emptyList();

  private boolean bugsChoreEstimatable;

  private String[] estimates;

  private String[] labels;

  private final String[] storyStates = { "Not Yet Started", "Started", "Finished", "Delivered", "Accepted", "Rejected" };

  private final String[] storyTypes = { "Feature", "Bug", "Chore", "Release" };

  private final String[] iterationTypes = { "Current", "Backlog", "Done", "Icebox", "Current_Backlog" };

  private final String[] choreStates = { "Not Yet Started", "Started", "Accepted" };

  private final String[] releaseStates = { "Not Yet Started", "Accepted" };

  public String[] getChoreStates() {
    return choreStates;
  }

  public String[] getReleaseStates() {
    return releaseStates;
  }

  private String requestedBy;

  public String getRequestedBy() {
    return requestedBy;
  }

  public void setRequestedBy(String requestedBy) {
    this.requestedBy = requestedBy;
  }

  public String[] getStoryTypes() {
    return storyTypes;
  }

  public List<String> getMembers() {
    return members;
  }

  void setMembers(List<String> projects) {
    this.members = Collections.unmodifiableList(new ArrayList<String>(projects));
  }

  public void setEstimates(String[] estimates) {
    this.estimates = estimates;
  }

  public String[] getEstimates() {
    return estimates;
  }

  public String[] getStoryStates() {
    return storyStates;
  }

  public String[] getIterationTypes() {
    return iterationTypes;
  }

  public String[] getLabels() {
    return labels;
  }

  public void setLabels(String[] labels) {
    this.labels = labels;
  }

  public boolean isBugsChoreEstimatable() {
    return bugsChoreEstimatable;
  }

  public void setBugsChoreEstimatable(boolean bugsChoreEstimatable) {
    this.bugsChoreEstimatable = bugsChoreEstimatable;
  }

  public List<String> getMembersEmail() {
    return membersEmail;
  }

  public void setMembersEmail(List<String> membersEmail) {
    this.membersEmail = membersEmail;
  }
}
