package com.teamunify.eclipse.mylyn.pt.pivotaltracker;

/**
 * @author DL
 * 
 */
public class NoteData {
  private int id;
  private String text;
  private String author;
  private String noted_at;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getNoted_at() {
    return noted_at;
  }

  public void setNoted_at(String noted_at) {
    this.noted_at = noted_at;
  }

}
