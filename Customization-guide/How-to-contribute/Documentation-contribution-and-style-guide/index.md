---
layout: page
title: Documentation contribution and style guide
permalink: /:path/
sort_idx: 30
---
{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

* TOC
{:toc}

This guide introduces documentation conventions applicable to the Kaa project.

The guide contains instructions on how to write and contribute documentation content.

The target audience of this guide is Kaa development team.

The purpose of this document is to achieve as much consistency as possible in terms of format, style, content, and structure of the Kaa project documentation.

# Documentation style guide

## Page layout
* Start every page with a section title followed by a _table of contents (TOC)_.
* TOC represents a list of the page headings and subheadings.
If your page contains only one heading, TOC is not required.
* Right below the TOC, include a short introduction explaining the purpose of the page and what the reader can learn from it.
* The introduction should be 1-10 sentences long, but not more than 5 lines of text.

## Content structure
* Keep your titles and headings short and precise, 1-5 words will do.
* Keep sentence structure simple.
Try to limit sentences to one clause.
Individuals who have language difficulties, non-native English speakers, and some people who are deaf or hard of hearing may have difficulty understanding longer, complicated sentences.
* If you have sentences longer than 25 words, try to break them up or condense them.
If you can’t, make sure they are in plain English.
* Keep paragraphs short or create small sections or text groupings.
* Put key information near the beginning of the content.
* Provide descriptions that do not require pictures, or provide both pictures and written descriptions.
To test whether the writing is effective, try removing, one at a time, first the words and then the pictures.
With only one method, can you still figure out what to do?
* Use numbered lists for step-by-step procedures and bulleted list for an unordered set of items.
* Use a bulleted list or headings to emphasize important points.
* Minimize the number of steps in a procedure.
Individuals who have cognitive impairments may have difficulty following procedures that have many steps.
Keep the steps simple, and keep the user oriented.
* Provide links to related content within the documentation to help readers get more information.

## Style and tone
* Use _please_ and _thank you_ judiciously.
Avoid using _please_ in instructions unless some step in the procedure causes inconvenience for the user or represents a workaround for some system limitation.
* Use _sorry_ only in error messages that result in serious problems for the user.
Don’t use _sorry_ if the problem occurred during the normal functioning of the program, such as when the user needs to wait for a network connection to be found.
* Don’t try to be funny.
Jokes, slang, and sarcasm are context-specific and hard to translate and localize.
What’s funny to you might offend or alienate some portion of your audience, so it’s best to avoid these rhetorical ­approaches.
* Avoid overusing subjective terms such as _easy_, _fast_, _fun_, and so on.
Instead, demonstrate that something is easy or fun by providing relevant information.
* Use everyday words when you can, and avoid formal language that you wouldn’t use when speaking to someone in person.
* Avoid inventing new words or assigning words an unusual meaning.
* Use short, plain words as much as possible.
* Don’t make generalizations about people, countries, regions, and cultures, especially if the generalizations could be considered derogatory.
* Avoid using culturally sensitive terms.
* Omit needless words.
Don’t use two or three words when one will do.
* Use questions sparingly. While questions may support a friendlier tone, overuse of questions doesn’t support a trustworthy voice.
Questions can work well when users actually do have them, not when we invent them on their behalf.
* Use exclamation points sparingly.
The overuse of exclamation points can make content seem effusive or ingratiating.
Save exclamation points for when they count.
* Be mindful of globalization and cultural considerations before you use colloquialisms or idioms.
* Do not use non-English words or phrases, such as _de facto_ or _ad hoc_, in English content, even if you think these terms are generally known and understood.

## Use of terminology
* Whenever possible, you should get your point across by using common English words.
It is all right to use technical terms when they are necessary for precise communication, even with a general audience, but do not write as if everybody understands these terms or will immediately grasp their meaning.
* Define terms in the text as you introduce them.
* Spell out an acronym on its first mention on the page, for example, _Common Type Library (CTL)_.
* Provide links from the main text to the glossary.
* Do not create a new term if a term describing a concept already exists in the glossary.
* If you need to create a new term, verify that the term that you select is not already in use to mean something else.


## Grammar
* Give preference to present tense over past and future tenses.
* Give preference to simple verb forms over perfect and continuous forms.
* Put the action of the sentence in the verb, not in the nouns.
* Choose single-word verbs over multiple-word verbs.
* Don’t convert verbs to nouns and nouns to verbs.
* Do not use a transitive verb without a direct object.
Either supply a direct object or use an intransitive verb instead.
* Avoid using weak, vague verbs such as _be_, _have_, _make_, and _do_.
Such verbs are sometimes necessary, but use a more descriptive verb whenever you can.
* Use imperative mood for procedures, formulate your instructions as giving commands to the user.
* Use indicative mood for conveying information such as facts, questions, assertions, or explanations.
* Omit unnecessary adverbs.
* Use context to reduce ambiguity.
Some words can be read as both verbs and nouns, such as _file_, _post_, _input_, etc.
When using these terms, ensure that the context and sentence structure reduce ambiguity.
* In general, use second person (pronoun _you_) also known as _direct address_.
Second person supports a friendly tone because it connects you with the user.
* Use first person sparingly.
First person is appropriate when writing from the point of view of the user.
* Avoid using more than 3 nouns in a noun string as it may be confusing or ambiguous even for native English speakers.

## Text formatting
* Use **bold** for:
  * Titles of windows and dialog boxes.
  * Names of commands, attributes, constants, methods, fields, predefined classes, databases, events, UI elements.
  * User input.
* Use _italic_ for:
  * Technical terms on first mention.
  * Emphasis.
  * Placeholders and parameter names.
* Use links for names of pages and sections.
* Capitalize only the first word in titles and headings.
* Do not use capitalization for no apparent reason. Use lowercase unless uppercase is justified.
* Use regular text (capitalized where applicable) for all other purposes, such as:
  * Names of user-defined classes, folders, files and file extensions.
  * Key names and combinations.
  * Strings (enclose in quotation marks) and values.
  * Environment variables and error message names.
  * Names of programs and utilities.

## Tables
* Introduce a table with a sentence that ends with a period, not a fragment that ends with a colon.
* If a table is titled, an introductory sentence does not have to immediately precede the table.
* Put the table title above the table.
* Table dimensions must be visible on a minimum screen resolution, typically 800 pixels by 600 pixels.
* Put footnote explanations at the end of the table, not at the bottom of the page.
* Tables can be used to simulate frames.
In this case, tables are better because older browsers cannot always process frames correctly.

## Lists
* Introduce a list with a heading or with a sentence or fragment ending with a colon.
* Begin each entry in a bulleted or numbered list with a capital letter.
* End each entry with a period if all entries are complete sentences, if they are a mixture of fragments and complete sentences, or if they all complete the introductory sentence or fragment.
* Do not use ending punctuation for entries that are short phrases (three words or fewer) or single words.
* If all entries are fragments that together with the introductory phrase do not form a complete sentence, do not end them with periods.
* If you introduce a list with a fragment, do not treat the list and its introduction as one continuous sentence.
That is, do not use semicolons or commas to end list items, and do not insert _and_ before the last list element.

## Notes, tips, and cautions
* Use notes sparingly so that they remain effective in drawing the user’s attention.
* In general, try to use only one note in a Help topic.
For example, if you must have two notes of the same type, such as a tip and a caution, combine them into one note with two paragraphs, or integrate one or both of the notes in the main text.
* Never include two or more paragraphs formatted as notes without intervening text.
If you need to put two notes together, format them as an unordered list within the note heading.
* You can include lists within notes.
* Use _note_ with the heading “Note” to indicate neutral or positive information that emphasizes or supplements important points of the main text.
A note supplies information that may apply only in special cases.
* Use _tip_ with the heading “Tip” to help users apply the techniques and procedures described in the text to their specific needs.
A tip suggests alternative methods that may not be obvious and helps users understand the benefits and capabilities of the product.
A tip is not essential to the basic understanding of the text.
* Use _important note_ with the heading “Important” to provide information that is essential to the completion of a task.
Users can disregard information in a note and still complete a task, but they should not disregard an important note.
* Use _caution_ with the heading “Caution” to advise users that failure to take or avoid a specific action could result in loss of data, damage of hardware, and other issues of similar importance.

## Code examples
* Format all JSON files with this [online tool](https://jsonformatter.curiousconcept.com/) using the "4 space tab" profile.
* Code example should follow [Code style]({{root_url}}Customization-guide/Code-style/) for the given language.
* When a code example is available in several programming languages (as with SDK usage examples), represent the alternatives using a tabbed container with tab names presenting the language name ("Java", "C++", etc.).
See [Jekyll formatting](#jekyll-formatting).
* Enable the syntax highlight for the language of the code example whenever available.
* Use _org.kaaproject.kaa.schema.sample_ prefix to start a schema namespace used for examples and documentation purposes.
* Use inline code formatting in the documentation for:
  * Short code examples.
  * Reference to code related entities (function names, variables, parameters, arguments, etc.).
  * Source file names.
  * Numbers when they are used as code.

## Markdown formatting
* Use [GitHub Flavored Markdown](https://guides.github.com/features/mastering-markdown/) for all documentation.
* Use pure Markdown without HTML tags when possible.
* Start every sentence with a new line to make future merging easier.
* Do not use Emoji.
* Use Heading 4 (i.e. `####`) as the last level heading. All other levels are not displayed in table of contents.
* Use the following template to auto-generate the TOC:

{% raw %}
  ```
  * TOC
  {:toc}
```
{% endraw %}

* There is an easy way to generate tables in Markdown.
Click the **Src** tab in the table below to see the source code used to generate the table.

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
* Refer to the [Markdown Cheatsheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet) for information and examples on how to create tables and other content elements.

## Jekyll formatting
* Kaa project uses [Jekyll](https://jekyllrb.com/) to publish the documentation on the web.
* Include a YAML front matter block with a `layout`, `title` and `permalink` keys at the beginning of each page.

  ```
---
layout: page
title: Documentation style guide
permalink: /:path/
---
```

* Capture `root_url` to refer to another page.

{% raw %}
  ```
{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}
```
{% endraw %}

* Refer to another page using `root_url`, e.g.,  [Glossary]({{root_url}}Glossary).

{% raw %}
  ```
[Glossary]({{root_url}}Glossary)
```
{% endraw %}

* Use tabs to describe same instructions/code examples for different platforms/languages.
Click the **Src** tab to see the source code used to generate the tabs.
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

Kaa documentation is a part of [Kaa source code](https://github.com/kaaproject/kaa) and is located in the doc/ folder.
For full description of the contributing process, see [How to contribute]({{root_url}}Customization-guide/How-to-contribute/).
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

   See the detailed installation instructions in the table below. Click the appropriate tab to select your platform:
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
   Server address: http://127.0.0.1:4000//
   Server running... press ctrl-c to stop.
   ```
