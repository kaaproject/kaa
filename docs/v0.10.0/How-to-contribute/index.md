---
layout: page
title: Contribution guide
permalink: /:path/
sort_idx: 45
---

{% include variables.md %}

* TOC
{:toc}

Welcome to the Kaa community!

As an open-source project, [Kaa platform]({{root_url}}Glossary/#kaa-platform) thrives from contributions by people like you.
We would love to see you mastering the Kaa source code; however, writing code is not the only way you can contribute to Kaa.

Feel free to use all of the contribution options:

- Join [Kaa Stack Overflow](http://stackoverflow.com/questions/tagged/kaa) to find answers to technical Kaa-related questions, and help others in the community.
Make sure to include the `kaa` tag in your post.
- Join [Kaa forum](https://www.kaaproject.org/forum/) to find answers to common user questions, give your feedback, ideas and suggestions, as well as for general discussions.
- Use [Jira](http://jira.kaaproject.org/) to report issues and request new features.
When reporting issues, please make sure you provide as much detail as possible so that the reported issue can be resolved effectively.
See [Jira flow]({{root_url}}How-to-contribute/Jira-flow/).
- Contribute to Kaa project repositories on [GitHub](https://github.com/kaaproject/).
See [Git flow]({{root_url}}How-to-contribute/Git-flow/).

## Contribute to source code

If you are willing to contribute to the Kaa code base, or submit sample [applications]({{root_url}}Glossary/#kaa-application) for [Kaa Sandbox]({{root_url}}Glossary/#kaa-sandbox):

1. Register at [Kaa public Jira](http://jira.kaaproject.org/).

2. Fill out and sign the [Individual Contributor Licensing Agreement](http://www.kaaproject.org/Uploads/ICLA.pdf) (ICLA).

	>**NOTE:** If you are contributing on behalf of an organization, you would also need to sign the [Corporate Contributor License Agreement](http://www.kaaproject.org/Uploads/CCLA.pdf) (CCLA).
	>Please sign, scan and send the forms to [legal@kaaproject.org](mailto:legal@kaaproject.org).
	{:.note}

3. Create a Jira ticket describing your finding and/or the proposed change.

	>**NOTE:** When you contribute to Kaa project on GitHub, make sure you post your [pull request](https://help.github.com/articles/using-pull-requests/) link in the comments section of the related Jira ticket and change the ticket status to **In review**.
	>See [Git flow]({{root_url}}How-to-contribute/Git-flow/) and [Jira flow]({{root_url}}How-to-contribute/Jira-flow/).
	{:.note}

## Contribute to documentation

Documentation is a part of the Kaa code base.
You can find the documentation files in the `doc/` subdirectory of the [main repository](https://github.com/kaaproject/kaa).
This means that the contribution process is the same for both the source code and documentation.

Contributing to the Kaa documentation, make sure to complete the following:

* Select `Component: Documentation` in the corresponding Jira ticket.
* Generate and check documentation locally before committing.
See [Preview documentation]({{root_url}}How-to-contribute/#preview-documentation).
* Check with the [Documentation style guide]({{root_url}}How-to-contribute/Style-guide/) to make sure your contribution complies with the Kaa requirements.

### Preview documentation

To preview the documentation locally and make sure it meets the Kaa requirements:

1. Install the required software:
  * [Git](https://git-scm.com/)
  * [Ruby](https://www.ruby-lang.org)
  * [Jekyll](https://jekyllrb.com/)
  * [Jekyll-sitemap](https://github.com/jekyll/jekyll-sitemap)
  * [Jekyll-gist](https://github.com/jekyll/jekyll-gist)
  * [Rouge](https://github.com/jneen/rouge)

   Follow the instructions for your platform below.

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Platform1">Ubuntu 14.04</a></li>
  <li><a data-toggle="tab" href="#Platform2">Ubuntu 16.04</a></li>
  <li><a data-toggle="tab" href="#Platform3">CentOS 7</a></li>
  <li><a data-toggle="tab" href="#Platform4">Nix</a></li>
</ul>

<div class="tab-content">
<div id="Platform1" class="tab-pane fade in active" markdown="1" >

```bash
sudo apt-get update
sudo apt-get install -y git ruby2.0 ruby2.0-dev build-essential
sudo gem2.0 install -N jekyll jekyll-gist jekyll-sitemap rouge
```

</div><div id="Platform2" class="tab-pane fade" markdown="1" >

```bash
sudo apt-get update
sudo apt-get install -y git build-essential ruby ruby-dev ruby-ffi
sudo gem install -N jekyll jekyll-gist jekyll-sitemap rouge
```

</div><div id="Platform3" class="tab-pane fade" markdown="1" >

```bash
sudo yum install -y ruby git ruby-devel
sudo yum groupinstall -y 'Development Tools'
sudo gem install -N jekyll jekyll-gist jekyll-sitemap rouge
```

</div><div id="Platform4" class="tab-pane fade" markdown="1" >

```bash
nix-shell doc/shell.nix --run ./test-gh-pages.sh
```

</div></div>
</li>
</ul>

{: start="2"}
2. Open the root directory of your git code branch and delete the `test-gh-pages-*/` folder if it exists.
3. Run the `test-gh-pages.sh` script to generate documentation and access it at your [http://localhost:4000/](http://127.0.0.1:4000/kaa/).

   ```bash
   user@host:/kaa$ ./test-gh-pages.sh
   ...
   Server address: http://127.0.0.1:4000/kaa/
   Server running... press ctrl-c to stop.
   ```
