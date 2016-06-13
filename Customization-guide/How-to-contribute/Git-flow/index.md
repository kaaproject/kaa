---
layout: page
title: Git flow
permalink: /:path/
nav: /:path/Customization-guide/How-to-contribute/Git-flow
sort_idx: 10
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

We use a [GitHub flow](https://guides.github.com/introduction/flow/). That means, you should branch from the main repo and contribute back via pull requests. That's de facto standard workflow on GitHub—nothing fancy here.

However, there is a thing you should know about Kaa branches. We have two main long-running branches: `master` and `develop`. `master` always points to the latest released version—**no pull requests are accepted to master**. `develop` is a main development branch; it reflects the latest development status. Usually, new changes should go there.

There may be a number of short-living branches; most of them are used for specific jira tickets. However, `release-xxx` branches are special. They are pre-release branches which are used to test and stabilize everything before actual release. (Actual releases are tagged.) **Only bugfixes are accepted to release branches.**

There may be develop branches for releases after next; they took a form of `develop-xxx` (e.g., `develop-1.0.0`). Usually, you should avoid committing to them and use branches for specific features.

## Clonning

The main Kaa repository is located here: <https://github.com/kaaproject/kaa>. To contribute to the Kaa you need to fork it (click fork button on the page), then clone your new repo:

```sh
git clone git@github.com:<your_github_name>/kaa.git # Replace <your_github_name> with your github name.
cd kaa
```

You may need to sync with the main repo, so it's good to add it to remotes:

```sh
git remote add upstream https://github.com/kaaproject/kaa.git
```

The rest of the guide assumes the above command was executed, so `upstream` points to `kaaproject/kaa`.

## Merge requirements

### Branch off from appropriate branch

First of all, you should decide what branch you will base your changes on. Generally, that's the `develop` branch.

The rules are next:

- `release-xxx` if your change is a **bugfix** that should go to the release *xxx* and there is such branch. That's either updates to patch releases or pre-release hotfixes.
- `develop` if you want your change in the next minor version bump. (Minor is a second version component. e.g. 0.8.1 -> 0.9.0 is a minor bump. Note also that 0.9.1 -> 1.0.0 is a minor bump as well.)
- `develop-xxx` if your change shouldn't go to the next release. Usually, you know that; if you don't, that's a wrong branch.
- `master` -- never.

Consult fix version field in Jira to see release version for your change.

Note also that that's is a branch you will open pull request against later.

It's always a good idea to fetch newest changes from the branch before starting:

```sh
git checkout develop # assuming you branch off from develop
git pull
```

### Opened against appropriate branch

You must open the pull request against the branch you've branched from. That means no pull requests to master are accepted.

### No merge conflicts

The pull request shouldn't have have any merge conflicts. If there is one, you should resolve it and update pull request.

You can do that with the following commands:

```sh
# checkout a branch you open PR from
git fetch upstream # assuming upstream is kaaproject/kaa
git merge upstream/merge_branch # Where merge_branch is a branch you open pull request against.
# resolve pull requests
git add changed_files
git commit
git push
```

GitHub will automatically update your pull request.

### Tests

All pull requests are automatically checked with Travis and Jenkins. The build should pass all tests to get merged.

### Review

The pull request must have at least two LGTM (Looks Good To Me) from the members of the team responsible for a changed component and shouldn't have any open review questions.

## Gatekeepers

Gatekeeper is a person who is responsible for the final review and merge; he is also responsible for managing git repos. That is the only person who can write to master, develop and release branches.

If a pull request satisfies all merge requirements, one of gatekeepers must do a final review and merge.
