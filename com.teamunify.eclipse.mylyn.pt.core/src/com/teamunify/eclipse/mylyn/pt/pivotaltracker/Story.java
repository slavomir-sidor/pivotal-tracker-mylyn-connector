package com.teamunify.eclipse.mylyn.pt.pivotaltracker;

/**
 * @author DL
 * 
 */

public class Story {

  private int id;
  private int project_id;
  private String name;
  private String description;
  private int estimate;
  private String story_type;
  private String requested_by;
  private String owned_by;
  private String current_state;
  private Notes notes;
  private String url;
  private String iterationType;
  private String updated_at;
  private Attachments attachments;
  private String labels;
  private Tasks tasks;

  public Tasks getTasks() {
    return tasks;
  }

  public void setTasks(Tasks tasks) {
    this.tasks = tasks;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getProject_id() {
    return project_id;
  }

  public void setProject_id(int project_id) {
    this.project_id = project_id;
  }

  public String getStory_type() {
    return story_type;
  }

  public void setStory_type(String story_type) {
    this.story_type = story_type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getEstimate() {
    return estimate;
  }

  public void setEstimate(int estimate) {
    this.estimate = estimate;
  }

  public String getCurrent_state() {
    return current_state;
  }

  public void setCurrent_state(String current_state) {
    this.current_state = current_state;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRequested_by() {
    return requested_by;
  }

  public void setRequested_by(String requested_by) {
    this.requested_by = requested_by;
  }

  public String getOwned_by() {
    return owned_by;
  }

  public void setOwned_by(String owned_by) {
    this.owned_by = owned_by;
  }

  public Notes getNotes() {
    return notes;
  }

  public void setNotes(Notes notes) {
    this.notes = notes;
  }

  public String getIterationType() {
    return iterationType;
  }

  public void setIterationType(String iterationType) {
    this.iterationType = iterationType;
  }

  public String getUpdated_at() {
    return updated_at;
  }

  public void setUpdated_at(String updated_at) {
    this.updated_at = updated_at;
  }

  public Attachments getAttachments() {
    return attachments;
  }

  public void setAttachments(Attachments attachments) {
    this.attachments = attachments;
  }

  public String getLabels() {
    return labels;
  }

  public void setLabels(String labels) {
    this.labels = labels;
  }

}
