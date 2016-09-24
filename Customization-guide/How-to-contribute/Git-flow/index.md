---
layout: page
title: Git flow
permalink: /:path/
sort_idx: 10
---

{% include variables.md %}

We use a [GitHub flow](https://guides.github.com/introduction/flow/). That means, you should branch from the main repository and contribute back via pull requests. That's de facto standard workflow on GitHub.

## Branches

The `master` branch represents latest development version of Kaa. Most changes should go there.

There are also `release-x.x` branches, which are stabilizing branches used for releasing maintenance releases. (All actual releases are tagged.)

If you want to backport a bug fix, open your pull request against the appropriate release branch.

## Clonning

The main Kaa repository is located here: <https://github.com/kaaproject/kaa>. To contribute to the Kaa you need to fork it (click fork button on the page), then clone your new repository:

```sh
git clone git@github.com:<your_github_name>/kaa.git # Replace <your_github_name> with your github name.
cd kaa
```

You may need to sync with the main repository, so it's good to add it to remotes:

```sh
git remote add upstream https://github.com/kaaproject/kaa.git
```

The rest of the guide assumes the above command was executed, so `upstream` points to `kaaproject/kaa`.

## Merge requirements

### No merge conflicts

Resolve any merge conflicts that may arise. (GitHub will display a warning on the pull request page.)

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

All pull requests are automatically checked with Travis and Jenkins. If any of the tests fails, fix it or describe why that can't be done.

### Review

The pull request must have at least two LGTM (Looks Good To Me) from the members of the team responsible for a changed component and should have no unresolved review questions.

## Gatekeepers

Gatekeeper is a person who is responsible for the final review and merge; he is also responsible for managing git repositories. That is the only person who can write to master and release branches.

If the pull request satisfies all merge requirements, one of gatekeepers should do a final review and merge.
