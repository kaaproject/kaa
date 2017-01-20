---
layout: page
title: Jira flow
permalink: /:path/
sort_idx: 20
---

* TOC
{:toc}

{% include variables.md %}

The Kaa project team uses [Atlassian Jira](https://www.atlassian.com/software/jira) for issue tracking in the development process.
This section describes when and how you can create Jira issues at [http://jira.kaaproject.org/](http://jira.kaaproject.org/) is you decided to contribute to Kaa project.

## When to create issues

To decide whether you should create a Jira issue, use [Kaa Stack Overflow](http://stackoverflow.com/questions/tagged/kaa) to consult with Kaa team and find out whether your issue:

* Is indeed an issue by nature.
* Is reproducible.
* Has not been spotted by other contributors yet.

If you confirmed all above statements to be true, proceed to the next section to find out which information you should provide when creating a Jira issue.

## Information to provide

To create a Jira issue, go to [http://jira.kaaproject.org/](http://jira.kaaproject.org/), click the **Create** button.
In the **Create Issue** pop-up, provide the following information about the issue:

| Field | Description |
|-------|-------------|
| Project | Select the project from the drop-down list depending on which [Kaa project repository](https://github.com/kaaproject) the issue is related to. When in doubt, select **Kaa**. |
| Issue Type | See [Issue types](#issue-types) to make the right choice. When in doubt, select **Task**. |
| Summary | Provide a one-line summary of the issue in clear and concise language. |
| Component/s | The drop-down list of this field depends on the project you selected in the **Project** field. |
| Affects Version/s | Select the version affected by the issue. |
| Description | Provide a meaningful description of the issue with all the details you find relevant. Describe the steps to reproduce the issue, and the environment in which you found it. In general, feel free to share any information you think is related to the issue. See also [Issue types](#issue-types) for specific information to be provided depending on the issue type. |
| Attachment | Feel free to attach images, documents and other files you think can help understand the issue-related information. |

>**NOTES:** Please do not fill any other fields, they will be managed by the Kaa team members.
{:.note}

### Issue types

#### Epic

Epics typically target large implementations that may span across several sprints.
Epic descriptions must contain a brief summary of the requested functionality and link(s) to the gh-pages documentation where more details would be added along the path of implementing the Epic.
Features break down to Stories and Tasks.
No code check-ins are allowed directly under Epics.

#### Story

A Story is a well scoped-out, independent, and finished improvement to the codebase that must not take more than one sprint to implement and validate.
A Story description must contain the design decisions taken, as well as the rationale.
It must also contain links to the gh-pages documentation that must be updated in the course of the implementation.

#### Task

Tasks are for product improvements that do not affect the actual code behavior.
Good examples of tasks are: creating or updating a documentation page; adding more details to the user prompts in a script; setting up a special type of a code build.

#### Bug

Bugs are usually code or documentation deficiencies.
As a minimum, a Bug must contain the description of the problem encountered, steps to reproduce, version of the code tested, and provide the corresponding logs.
Useful additions may include a reference to the test scenario, links to the documentation that show the expected system behavior, etc.
It is allowed to check-in code directly in the Bug type issues.
A Bug may be filed under an Epic if the affected functionality was related to the scope of that Epic.

## Working on issues

After you submitted an issue, it will appear in the selected project space for the Kaa team members to spot.
The work on the issue can begin after the team lead included it in a sprint and a person is assigned to the issue.

When there is a pull request on GitHub related to your issue, the relevant information will appear under the **Development** section of the issue. See also [Git flow]({{root_url}}Customization-guide/How-to-contribute/Git-flow/).

![Development section](attach/development_section.png)

You can track the issue from Jira, review the fix code, post your comments, and check your email for the issue-related notifications.