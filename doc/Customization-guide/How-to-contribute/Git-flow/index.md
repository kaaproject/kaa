---
layout: page
title: Git flow
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

* TOC
{:toc}

To contribute to Kaa project on [GitHub](https://github.com/kaaproject/kaa), use the [GitHub flow](https://guides.github.com/introduction/flow/).
In a nutshell, it means that you should branch from the main repository and contribute back by making [pull requests](https://help.github.com/articles/using-pull-requests/).
That is a generally accepted process flow on GitHub.

## Getting started

To follow the instructions in this guide and start contributing to Kaa project on GitHub:

<ol>
<li markdown="1">
[Fork](https://help.github.com/articles/fork-a-repo/) the [Kaa main repository](https://github.com/kaaproject/kaa).
</li>

<li markdown="1">
[Clone](https://help.github.com/articles/cloning-a-repository/) your new repository.
To do this, run the following command.

```sh
git clone git@github.com:<your_github_name>/kaa.git # Replace <your_github_name> with your GitHub profile name.
cd kaa
```
</li>

<li markdown="1">
To synchronize with the main repository, add it to the remotes.

```sh
git remote add upstream https://github.com/kaaproject/kaa.git
```

Now your **upstream** points to **kaaproject/kaa**.

</li>
</ol>

## Branches

The **master** branch represents the latest development version of Kaa.
Most changes should go there.

The **release-x.x** branches are used as stabilizing branches for maintenance releases.
All actual releases are tagged.

If you want to backport a bug fix, open your pull request (PR) against the appropriate release branch.

## Merge requirements

### No merge conflicts

Resolve any merge conflicts that may arise.
If conflict occurs, a corresponding message will be displayed on the PR page on GitHub.

To resolve a conflict, run the following commands.

```sh
# checkout a branch you open PR from
git fetch upstream # assuming upstream is kaaproject/kaa
git merge upstream/merge_branch # Where merge_branch is a branch you open PR against.
# resolve pull requests
git add changed_files
git commit
git push
```

GitHub will automatically update your pull request.

### Testing

All pull requests are automatically tested using [Travis](https://travis-ci.org/) and [Jenkins](https://jenkins.io/).
In case some tests fail, fix the issues or describe why the fix cannot be done.

### Review

Every pull request is reviewed by the assigned team members as per standard [GitHub procedure](https://help.github.com/articles/about-pull-request-reviews/).
Reviewers can comment on a PR, approve it, or request changes to it.
A PR can be merged when it is approved by at least two assigned reviewers and has no pending requests for changes.

## Gatekeepers

Gatekeeper is a person responsible for the final review and merge.
They are also responsible for managing Git repositories.
Only gatekeepers can write to **master** and release branches.

If a pull request meets all merge requirements, one of the gatekeepers performs the final review and merges the PR branch.