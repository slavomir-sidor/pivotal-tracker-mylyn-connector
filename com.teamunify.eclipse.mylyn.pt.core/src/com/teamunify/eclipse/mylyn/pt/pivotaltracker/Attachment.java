package com.teamunify.eclipse.mylyn.pt.pivotaltracker;

/**
 * 
 * @author DL
 */
public class Attachment {

  private int id;
  private String filename;
  private String description;
  private String uploaded_by;
  private String uploaded_at;
  private String url;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getUploaded_by() {
    return uploaded_by;
  }

  public void setUploaded_by(String uploaded_by) {
    this.uploaded_by = uploaded_by;
  }

  public String getUploaded_at() {
    return uploaded_at;
  }

  public void setUploaded_at(String uploaded_at) {
    this.uploaded_at = uploaded_at;
  }
}
