---
layout: page
title:  Documentation contribution guide
permalink: /:path/
sort_idx: 30
---
{% include variables.md %}

Kaa documentation is a part of the [Kaa source code](https://github.com/kaaproject/kaa) and is located in the `doc/` folder.
For a full description of the contribution process, see [How to contribute]({{root_url}}Customization-guide/How-to-contribute/).
The key differences between contributing source code and documentation are:

* Select `Component: Documentation` in the [Jira](http://jira.kaaproject.org/) ticket.
* Generate and check documentation locally before committing.
* Be sure to check the [Documentation style guide]({{root_url}}Customization-guide/How-to-contribute/Style-guide/).

## Preview documentation

1. Install required software:
  * [Git](https://git-scm.com/)
  * [Ruby](https://www.ruby-lang.org)
  * [Jekyll](https://jekyllrb.com/)
  * [Jekyll-sitemap](https://github.com/jekyll/jekyll-sitemap)
  * [Jekyll-gist](https://github.com/jekyll/jekyll-gist)
  * [Rouge](https://github.com/jneen/rouge)

   See the detailed installation instructions in the table below.
   Click the tab to select a platform:
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
2. Go to the root directory of your git code branch.
3. Delete `test-gh-pages-*/` if it exists.
4. Run `test-gh-pages.sh` script to generate documentation and start development server at [http://localhost:4000/](http://localhost:4000/).

   ```bash
   user@host:/kaa$ ./test-gh-pages.sh
   ...
   Server address: http://127.0.0.1:4000/kaa/
   Server running... press ctrl-c to stop.
   ```
