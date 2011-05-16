package com.teamunify.eclipse.mylyn.pt.core.util;

import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import com.teamunify.eclipse.mylyn.pt.core.PtCorePlugin;

public class PtQueryFilter {

  private final Pattern storyTypePattern;

  private final Pattern requestedByPattern;

  private final Pattern ownedByPattern;

  private final Pattern statePattern;

  private final Pattern iterationTypePattern;

  private final Pattern labelPattern;
  private final boolean invertLabels;

  public PtQueryFilter(IRepositoryQuery query) {
    String storyType = query.getAttribute(PtCorePlugin.QUERY_KEY_STORY_TYPE);
    storyType = storyType.replaceAll("Not Yet Started", "unstarted");
    storyTypePattern = patternForString(storyType);
    requestedByPattern = patternForString(query.getAttribute(PtCorePlugin.QUERY_KEY_REQUESTED_BY));
    ownedByPattern = patternForString(query.getAttribute(PtCorePlugin.QUERY_KEY_OWNED_BY));
    statePattern = patternForString(query.getAttribute(PtCorePlugin.QUERY_KEY_STATE));
    iterationTypePattern = patternForString(query.getAttribute(PtCorePlugin.QUERY_KEY_ITERATION_TYPE));
    labelPattern = patternForString(query.getAttribute(PtCorePlugin.QUERY_KEY_LABEL));
    invertLabels = booleanAttribute(query, PtCorePlugin.QUERY_KEY_LABEL_INVERSION);
  }

  private Pattern patternForString(String str) {
    if (str == null || str.length() == 0) { return null; }
    return Pattern.compile(str, Pattern.CASE_INSENSITIVE);
  }

  private boolean booleanAttribute(IRepositoryQuery query, String key) {
    String value = query.getAttribute(key);
    if (value == null) { return false; }
    return Boolean.parseBoolean(value);
  }

  public boolean accepts(TaskData taskData) {
    if (!match(storyTypePattern, taskData.getRoot().getAttribute(TaskAttribute.TASK_KIND), false)) { return false; }
    if (!match(requestedByPattern, taskData.getRoot().getAttribute(TaskAttribute.USER_ASSIGNED), false)) { return false; }
    if (!match(ownedByPattern, taskData.getRoot().getAttribute(TaskAttribute.USER_REPORTER), false)) { return false; }
    if (!match(statePattern, taskData.getRoot().getAttribute(TaskAttribute.STATUS), false)) { return false; }
    if (!match(labelPattern, taskData.getRoot().getAttribute(TaskAttribute.KEYWORDS), invertLabels)) { return false; }
    if (!match(iterationTypePattern, taskData.getRoot().getAttribute(TaskAttribute.PRIORITY), false)) { return false; }
    return true;
  }

  private boolean match(Pattern pattern, TaskAttribute attribute, boolean invert) {
    if (pattern == null) { return true; }
    if (attribute == null) { return true; }
    boolean result = false;
    List<String> values = attribute.getValues();
    for (String value : values) {
      result |= pattern.matcher(value).find();
    }
    return invert ? !result : result;
  }

}
