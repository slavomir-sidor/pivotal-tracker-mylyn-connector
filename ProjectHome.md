## Contributors Wanted ##

This open-source plugin was originally a funded (outsourced) project at TeamUnify, LLC. We have contributed it to the open-source community in hopes that others with a similar need will pony up with some contributions as well. There are no in-house plans to add features or otherwise continue development.

## Description ##

This is an eclipse plug-in that allows one to manage the stories in a Pivotal Tracker page on the public website. It allows one to edit the stories, reorder them, manage attachments, attach/retrieve Mylyn context, and optionally do branching and switching based on the story ID.

## Basic Setup ##

  * Create an account at www.pivotaltracker.com
  * Edit your Pivotal Tracker Profile, and generate an API Token
  * Create one or more projects in Pivotal Tracker
  * Install this Eclipse Plugin (Add Update Site, using the URL http://ptmylyn.teamunify.com)
  * Add a Pivotal Tracker Task Repository by Project ID. If you navigate to a PT project using a browser, your project ID will be at or near the end of the URL. It is an integer, like 201525 (which is the ID of this project).

**NOTE:** If you used single sign-on in PT, you must create a PT username for use with the connector. An external ID (like a Google account) will not work. You can set the username for an existing account in Profile.

## SVN Feature ##

A simple SVN feature is available. You must be using Subclipse for it to function, and you must also structure your repository with the traditional branches/trunk layout. If you meet these requirements, then you can create new branches and switch to them based on the PT Story ID in the Tasks view.

E.g., if you have a repository at http://example.com/svn, the extension will allow you to start the subclipse dialog so you can easily do:

`svn cp http://example.com/svn/trunk/proj http://example.com/svn/branches/<storyID>/proj`

for each selected project. It also eases the burden of switch back to one of these branches at a later time.

Really, all the extension does is plug in the base URL and story ID to the Subclipse branch/switch dialogs, so it is a very simple feature.

### Setup ###

  * Install the SVN feature
  * When adding a task repository, give the base SVN URL of your subversion space. E.g. http://domainname/svn. Do NOT include trunk or branches in this URL.

To create a new branch:

  * Select the projects you'd like to branch (they must all be from the same base URL given in setup)
  * Right click on the pivotal tracker story in Mylyn Tasks, and choose Create Branch for this Task
  * In the subclipse dialogs, be sure to check "create intermediate folders", and "switch to this branch" (if that is what you want to do).

To switch to an existing branch:

  * Select the projects
  * Right-click on the story and choose "Switch to this Branch"