---
layout: page
title: Documentation contribution and style guide
permalink: /:path/
sort_idx: 30
---
{% include variables.md %}

* TOC
{:toc}

This guide introduces documentation conventions applicable to the Kaa project.
The intent of this guide is to unify the look and feel of the documentation, make navigation predictable, usage and contribution simple.

# Documentation style guide

## Sentences and grammar
* Use consistent terminology, so that every term means the same thing and is spelled in the same way throughout the entire documentation.
If not properly introduced, synonyms can confuse the reader.
* Give preference to present tense over past and future tenses; give preference to simple verb forms over perfect and continuous forms.
* Use numbered lists for step-by-step procedures and bulleted list for an unordered set of items. Capitalize the first word of each list entry.
* Use parallel sentence structures in bulleted and numbered lists.
* End each entry of the list with a period if all the entries are complete sentences or if they are a mixture of fragments and complete sentences.
If all entries are fragments, do not end them with periods or any other punctuation mark.
* Use imperative mood in procedures, that is, formulate your instructions as giving commands to the user.
* Avoid inventing new words or assigning words an unusual meaning.
* Avoid using please in instructions unless some step in the procedure causes inconvenience for the user or represents a workaround for some system limitation.

## Headings and capitalization
* Capitalize only the first word in titles and headings.
* Do not use capitalization for no apparent reason.
Use lowercase unless uppercase is justified.
* Use Heading 4 (i.e. `####`) as the last level heading.
All other levels are not displayed in table of contents.

## Technical terms and abbreviations
* Spell out an acronym on its first mention on the page, e.g. *Common Type Library* (CTL).
* Ensure that an important technical term or abbreviation is included in  [Glossary]({{root_url}}Glossary).

## Font conventions
* Use **bold** for the titles of windows and dialog boxes; for the names of commands, attributes, constants, methods, fields, predefined classes, databases, events, UI elements; for the user input.
* Use *italic* for technical terms on first mention and emphasis; for placeholders and parameter names; for section names.
* Use regular font for other purposes, like the names of user-defined classes, folders, files and file extensions; for key names and combinations; for strings (enclose in quotation marks) and values; for environment variables and error message names; for the names of programs and utilities.
* Use links for page names.

## Code formatting
* Format all JSON files with this [online tool](https://jsonformatter.curiousconcept.com/), "4 space tab" profile.
* Code example should follow [Code style]({{root_url}}Customization-guide/Code-style/) for the given language.
* When a code example is available in several programming languages (as with SDK usage examples), represent the alternatives using a tabbed container with tab names presenting the language name ("Java", "C++", etc.).
See detailed description in [Jekyll formatting](#jekyll-formatting).
* Enable the syntax highlight for the language of the code example, whenever available.
* Start the schema namespace, which we use for examples and documentation purposes, with the org.kaaproject.kaa.schema.sample prefix.
* Use inline code in documentation only for short code examples, for referring to code related entities (function names, variables, parameters, arguments, etc.), for source file names and for numbers when they are used as code.

## Page structure
* Add the table of contents to the top of every page having two or more headings.
* For every documentation page, include an introduction section that immediately follows the *TOC* (table of contents) (or top of the page for pages with no TOC).
In the introduction, explain the purpose of the page and what the reader can learn from it.
* Do not add copyright - it is generated automatically.
* Links are good!
Think of the documentation modularity similarly to how you think about the code modularity.
Rather than explaining the same topic multiple times in multiple locations, modularize and use links to fragment identifiers when applicable.


## Markdown formatting
* Use [GitHub Flavored Markdown](https://guides.github.com/features/mastering-markdown/) for all documentation.
* Start every sentence with a new line to make future merging easier.
* Do not use Emoji.
* Use the following template to auto-generate the TOC:

{% raw %}
  ```
  * TOC
  {:toc}
```
{% endraw %}

* Use pure Markdown without HTML tags when possible.
* Markdown table can be enhanced with [Jekyll](https://jekyllrb.com/).

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs"> 
  <li class="active"><a data-toggle="tab" href="#Preview">Preview</a></li> 
  <li><a data-toggle="tab" href="#Src">Src</a></li> 
</ul>

<div class="tab-content">
<div id="Preview" class="tab-pane fade in active" markdown="1" >

|Page    | link | url |
|------- | ---- | --- |
| Programming guide |[Programming guide]({{root_url}}Programming-guide/) | {{root_url}}Programming-guide/ |
| Android SDK |[Android SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/SDK-integration-instructions/SDK-Android/)|{{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/SDK-integration-instructions/SDK-Android/|

</div><div id="Src" class="tab-pane fade " markdown="1" >

```
|Page    | link | url |
|------- | ---- | --- |
| Programming guide |[Programming guide]({{root_url}}Programming-guide/) | {{root_url}}Programming-guide/ |
| Android SDK |[Android SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/SDK-integration-instructions/SDK-Android/)|{{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/SDK-integration-instructions/SDK-Android/|
```
</div></div>
</li>
</ul>


## Jekyll formatting
* [Jekyll](https://jekyllrb.com/) is used for documentation generation.
* Include a YAML front matter block with a `layout`, `title` and `permalink` keys at the beginning of each page.

  ```
---
layout: page
title: Documentation style guide
permalink: /:path/
---
```

* Include `variables.md` files to capture `root_url`. This will allow you to refer to other pages.

{% raw %}
  ```
{% include variables.md %}
```
{% endraw %}

* Refer to another page through `root_url`, e.g.,  [Glossary]({{root_url}}Glossary).

{% raw %}
  ```
[Glossary]({{root_url}}Glossary)
```
{% endraw %}

* Use tabs to describe same instructions/code examples for different platforms/languages.
<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Preview1">Preview</a></li>
  <li><a data-toggle="tab" href="#Src1">Src</a></li>
</ul>

<div class="tab-content">
<div id="Preview1" class="tab-pane fade in active" markdown="1" >

<div class="well">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Ubuntu">Ubuntu</a></li>
  <li><a data-toggle="tab" href="#Fedora">Fedora</a></li>
  <li><a data-toggle="tab" href="#CentOS">CentOS</a></li>
</ul>

<div class="tab-content">
<div id="Ubuntu" class="tab-pane fade in active" markdown="1" >

##### Ubuntu

Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.

</div><div id="Fedora" class="tab-pane fade" markdown="1" >

##### Fedora

Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam.

</div><div id="CentOS" class="tab-pane fade" markdown="1" >

##### CentOS

Eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.

</div></div></div>

</div><div id="Src1" class="tab-pane fade " markdown="1" >

```
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Ubuntu">Ubuntu</a></li>
  <li><a data-toggle="tab" href="#Fedora">Fedora</a></li>
  <li><a data-toggle="tab" href="#CentOS">CentOS</a></li>
</ul>

<div class="tab-content">
<div id="Ubuntu" class="tab-pane fade in active" markdown="1" >

##### Ubuntu

Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.

</div><div id="Fedora" class="tab-pane fade" markdown="1" >

##### Fedora

Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam.

</div><div id="CentOS" class="tab-pane fade" markdown="1" >

##### CentOS

Eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.

</div></div>
```

</div></div>
</li>
</ul>

# Documentation contribution

Kaa documentation is a part of [Kaa source code](https://github.com/kaaproject/kaa) - it can be found in the `doc/` folder.
A full description of contributing to Kaa project can be found at [How to contribute]({{root_url}}Customization-guide/How-to-contribute/).
Key differences between contributing source code and documentation are:

* Select `Component: Documentation` in the [Jira](http://jira.kaaproject.org/) ticket.
* Generate and check documentation locally.

## Documentation preview

1. Install required software:
  * [Git](https://git-scm.com/)
  * [Ruby](https://www.ruby-lang.org)
  * [Jekyll](https://jekyllrb.com/)
  * [Jekyll-sitemap](https://github.com/jekyll/jekyll-sitemap)
  * [Jekyll-gist](https://github.com/jekyll/jekyll-gist)
  * [Jekyll-paginate](https://github.com/jekyll/jekyll-paginate)
  * [Rouge](https://github.com/jneen/rouge)

   Detailed installation instructions for selected platforms:
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
sudo gem2.0 install -N jekyll jekyll-gist jekyll-sitemap jekyll-paginate rouge
```

</div><div id="Platform2" class="tab-pane fade" markdown="1" >

```bash
sudo apt-get update
sudo apt-get install -y git build-essential ruby ruby-dev ruby-ffi
sudo gem install -N jekyll jekyll-gist jekyll-sitemap jekyll-paginate rouge
```

</div><div id="Platform3" class="tab-pane fade" markdown="1" >

```bash
sudo yum install -y ruby git ruby-devel
sudo yum groupinstall -y 'Development Tools'
sudo gem install -N jekyll jekyll-gist jekyll-sitemap jekyll-paginate rouge
```

</div><div id="Platform4" class="tab-pane fade" markdown="1" >

```bash
nix-shell doc/shell.nix --run ./test-gh-pages.sh
```

</div></div>
</li>
</ul>

{: start="2"}
2. Delete `test-gh-pages-*/` if it exists.
3. Run `test-gh-pages.sh` script to generate documentation and start development server at [http://localhost:4000/](http://localhost:4000/).

   ```bash
   user@host:/kaa$ ./test-gh-pages.sh
   ...
   Server address: http://127.0.0.1:4000/kaa/
   Server running... press ctrl-c to stop.
   ```
