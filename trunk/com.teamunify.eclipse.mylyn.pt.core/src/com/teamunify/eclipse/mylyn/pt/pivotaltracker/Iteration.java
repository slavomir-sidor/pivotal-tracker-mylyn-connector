package com.teamunify.eclipse.mylyn.pt.pivotaltracker;

/**
 * @author DL
 * 
 */

public class Iteration {

  private Stories stories[];
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public String getStart() {
    return start;
  }

  public void setStart(String start) {
    this.start = start;
  }

  public String getFinish() {
    return finish;
  }

  public void setFinish(String finish) {
    this.finish = finish;
  }

  int number;
  String start;
  String finish;

  public Stories[] getStories() {
    return stories;
  }

  public void setStories(Stories[] stories) {
    this.stories = stories;
  }

}
