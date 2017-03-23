---
layout: page
title: Documentation style
permalink: /:path/
sort_idx: 30
---
{% include variables.md %}

* TOC
{:toc}

This guide introduces documentation conventions applicable to the Kaa project.

The guide contains instructions on how to contribute to Kaa documentation while preserving consistency in terms of format, style, content, and structure of the documentation.

The target audience of this guide is Kaa development team and anyone contributing to Kaa documentation.

## Documentation style guide

### Markdown formatting
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
| Java SDK |[Java SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Java/)|{{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Java/|

</div><div id="Src" class="tab-pane fade " markdown="1" >

```
|Page    | link | url |
|------- | ---- | --- |
| Programming guide |[Programming guide]({{root_url}}Programming-guide/) | {{root_url}}Programming-guide/ |
| Java SDK |[Java SDK]({{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Java/)|{{root_url}}Programming-guide/Using-Kaa-endpoint-SDKs/Java/|
```
</div></div>
</li>
</ul>
* Refer to the [Markdown Cheatsheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet) for information and examples on how to create tables and other content elements.

### Page layout
* Start every page with a section title followed by a _table of contents (TOC)_.
* TOC represents a list of the page headings and subheadings.
If your page contains only one heading, TOC is not required.
* Right below the TOC, include a short introduction explaining the purpose of the page and what the reader can learn from it.
* As a rule of thumb, the introduction should be 1-10 sentences long, but not more than 5 lines of text.

### Content structure
* Keep your titles and headings short and precise, 1-5 words will do.
* Use simple one-clause sentence structure whenever you can.
* If you have sentences longer than 25 words, try to break them up or condense them.
If you can’t, make sure they are in plain English.
* Avoid using long paragraphs.
Instead, try to break them into groups of 1-10 lines of text.
* Place the key information at the beginning of each content element.
* Supplement your text with pictures only to illustrate complex ideas.
Avoid redundant imagery.
Do not use pictures without descriptions.
* Use numbered lists for step-by-step procedures and bulleted lists for unordered sets of items.
* Emphasize important points with a bulleted list or use headings.
* Keep the minimum number of steps for procedure descriptions.
* Provide links to related content within the documentation to help readers get more information.
* Provide links to auto-generated documentation (API, etc.).

### Style and tone
* Avoid using _please_ in instructions unless some step in the procedure causes inconvenience for the user or represents a workaround for some system limitation.
* Use _sorry_ only to inform about serious software or hardware problems for the user.
Don’t use _sorry_ if the problem appeared during normal software operation.
* Do not invoke humor in your content.
Humor is a culture-specific and subjective matter that doesn't belong in technical content.
* Do not use subjective words like _simple_, _great_, _superb_, and such.
Instead, demonstrate that something is simple or superb by providing relevant information.
* Use short, plain words when possible, avoid formal or elevated style of writing.
* Don't make up new words or modify their usual meaning.
* Don't use a more complicated term when there is a simpler one having the exact same meaning.
* Don’t express opinions about cultures, countries, people, etc.
* Try to use words that are culturally neutral.
* Omit needless words.
Use the minimum number of words required to get the message across.
* Do not overuse questions.
If you must use a question, make sure it's relevant to the user and the context.
Do not make up questions for the sake of creating an imaginary dialog with the user.
* Do not overuse exclamation points.
This can make your writing seem emotional and rather subjective to the user.
Use exclamation points when they are required to emphasize something really critical and important.
* Do not use colloquialisms, idioms, sayings, proverbs, etc.
Keep globalization and cultural considerations in mind.
* Avoid using non-English words or phrases, such as _ergo_ or _bona fide_, even if you think they are widely used.
However, there are two exceptions to this rule: _etc._ and _e.g._

### Use of terminology
* Whenever possible, use common English instead of using terminology.
Use terminology when you need to be particular and precise about something.
* Provide term definition on its first mention.
* Link to the Glossary for terms that are in the Glossary.
For others, use **bold** formatting on the first mention.
* When using defined terms, make sure you use them exactly as defined.
Shortcutting only works when it's defined.
E.g.: "Administrative UI" is not equal to "Admin UI" unless defined so, "tenant administrator" is not the same as "tenant", etc.
* Spell out an acronym on its first mention, for example, _Common Type Library (CTL)_.
* Do not make a new term if the Glossary contains another term describing the same idea.
* When creating a new term, make sure it does not already mean something else in the Glossary.

### Grammar
* Give preference to present tense over past and future tenses.
* Give preference to simple verb forms over perfect and continuous forms.
* Use call-to-action approach instead of simple statements to help user find the information.

  **_Good example_**
    Click the **Src** tab below to see the source code used to generate the table.

  **_Bad example_**
    Below is the **Src** tab that contains the source code used to generate the table.
* Avoid using multiple-word verbs, use one-word verbs when you can.
* Always provide a direct object after a transitive verb, otherwise use an intransitive verb.

  **_Good example_**
    Your application cannot connect to the server.
    Installation process will stop.

  **_Bad example_**
    Your application cannot connect.
    Installation process will stop running.
* Avoid using neutral verbs such as _do_, _make_, _have_, etc. when you can use a preciser verb to denote the action.

  **_Good example_**
    Connect to the cloud service.
    Update the application regularly.

  **_Bad example_**
    Make a connection to the cloud service.
    Do regular application updates.
* Use imperative mood for procedures, formulate your instructions as giving commands to the user.

  **_Example_**
    Click the **Management** button, enter a new IP in the **Kaa host/IP** block and click **Update**.
* Use indicative mood to provide facts, questions, assertions, or explanations.

  **_Example_**
    Kaa endpoint connects to your Kaa Sandbox by using the address built into the SDK.
* Do not use adverbs that do not contribute to the meaning of a sentence.

  **_Good example_**
    The server sends a request.

  **_Bad example_**
    The server quickly sends a request.
* Reduce ambiguity be providing the right context.
Some verbs and nouns look the same in writing, for example _access_, _click_, _process_, etc., which can be ambiguous to the user.
Make sure such words appear in the context that gives them specific meaning.
* Use second person (pronoun _you_) to support a friendly tone and relation to the user.
* Use first person in special cases only.
For example, when you write from the user's point of view.
* Avoid using more than 3 nouns in a noun string as it may be confusing or ambiguous even for native English speakers.

  **_Good example_**
    This application demonstrates functionality of the Kaa Configuration extension.
    Configuration service for client application.

  **_Bad example_**
    This application demonstrates Kaa Configuration extension functionality.
    Client application configuration service.

### Text formatting
* Use **bold** for:
  * Technical terms that are not in the Glossary, on their first mention.
  * Titles of windows and dialog boxes.
  * Names of UI elements, commands, attributes, constants, methods, fields, predefined classes, databases, events.
  * User input.
* Use _italic_ for:
  * Emphasis.
  * Placeholders and parameter names.
* Use [link]({{root_url}}How-to-contribute/Style-guide/#text-formatting) formatting for:
  * Technical terms that are in the Glossary, on their first mention.
  * Reference to Kaa documentation pages.
  * External web pages.
* Capitalize only the first word in titles and headings.
* Do not capitalize every word in a sentence for the sake of emphasis.
Use lowercase unless uppercase is justified (e. g. names of UI elements).
* Use regular text (capitalized where applicable) for all other purposes, such as:
  * Names of folders, files and file extensions.
  * Key names and combinations.
  * Strings (enclose in quotation marks) and values.
  * Environment variables and error message names.
  * Names of programs and utilities.
* Use `inline code` formatting for:
  * Short code examples.
  * Code related entities (function names, variables, parameters, arguments, etc.).
  * Names of files and directories.
  * Numbers when they are used as code.

### Tables
* Start every table with at least one introductory sentence or a table title.
End the introductory sentence with a period, not a colon.
* Stick to around 800x600 pixels resolution for tables to make them visible on most screens.
* Include table explanations right after the table.

### Lists
* Start every list either with a heading or with a sentence followed by a colon.
* Capitalize the first word of each list entry.
* End each list entry with a period.
However, do not use ending punctuation for lists where all entries are three words or fewer.
* Do not end list entries with semicolons or commas.
* Do not put _and_ before the last list entry.

### Notes, tips, and cautions
* Do not use notes too often as this distracts the user's attention.
* Try using not more than one note per subheading.
* If you have two notes of different types put together, always separate them with some text.
* Notes can be formatted as lists.
* Start every note with a heading in bold.
* You can use four types of notes:
  * Notes
  * Tips
  * Important note
  * Caution
* >**NOTE:** displays information that is supplementary to the main text or reminds the user of some related information explained before.
  {:.note}
* >**TIP:** explains how the readers can use the information in the main text for their specific needs, or how they can be more effective and get the best out of your product.
  {:.tip}
* >**IMPORTANT:** displays information that is critical the completion of a procedure (unlike the note, which is not critical and is for information only).
  {:.important}
* >**CAUTION:** alerts users of potential risk or damage in case they do not take some action or consider some information.
  {:.caution}

### Code examples

* Code examples should follow [Code style]({{root_url}}How-to-contribute/Code-style/) for the given language.
* When a code example is available in several programming languages (as with SDK usage examples), represent the alternatives using a tabbed container with tab names presenting the language name ("Java", "C++", etc.).
See [Jekyll formatting](#jekyll-formatting).
* Enable the syntax highlight for the language of the code example whenever available.
Click the **Src** tab to see the source code used to highlight the code example.

<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Preview1">Preview</a></li>
  <li><a data-toggle="tab" href="#Src1">Src</a></li>
</ul>

<div class="tab-content">
<div id="Preview1" class="tab-pane fade in active" markdown="1" >

```javascript
function fancyAlert(arg) {
  if(arg) {
    $.facebox({div:'#foo'})
  }
}
```

</div><div id="Src1" class="tab-pane fade " markdown="1" >

<pre>
```javascript
function fancyAlert(arg) {
  if(arg) {
    $.facebox({div:'#foo'})
  }
}
```
</pre>

</div></div>
</li>
</ul>

* Use `org.kaaproject.kaa.schema.sample` prefix to start a schema namespace used for examples and documentation purposes.
* Format all JSON files with this [online tool](https://jsonformatter.curiousconcept.com/) using the "4 space tab" profile.
* Use inline code formatting in the documentation for:
  * Short code examples.
  * Code related entities (function names, variables, parameters, arguments, etc.).
  * Names of files and directories.
  * Numbers when they are used as code.

### Jekyll formatting
* Kaa project uses [Jekyll](https://jekyllrb.com/) to publish the documentation on the web.
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


* Align code blocks in a list to the first character in a list item to force proper rendering.
Separate the code block from the list item with an empty line.

        1. List item

           ```c
           /* Some code */
           void foo(void);
           ```

* Refer to another page using `root_url`, e.g.,  [Glossary]({{root_url}}Glossary).

{% raw %}
  ```
[Glossary]({{root_url}}Glossary)
```
{% endraw %}

* Refer to files in github repository using `github_url`, e.g., [.gitignore]({{github_url}}.gitignore)

{% raw %}
  ```
[.gitignore]({{github_url}}.gitignore)
```
{% endraw %}

* Refer to raw files in github repository using `github_url_raw`, e.g., [README.md]({{github_url_raw}}README.md)

{% raw %}
  ```
[README.md]({{github_url_raw}}README.md)
```
{% endraw %}

* Refer to Sample Apps using `sample_apps_url`, e.g., [Sample Apps]({{sample_apps_url}})

{% raw %}
  ```
[Sample Apps]({{sample_apps_url}})
```
{% endraw %}

* Refer to Sandbox Frame using `sandbox_frame_url`, e.g., [Sandbox Frame]({{sandbox_frame_url}})

{% raw %}
  ```
[Sandbox Frame]({{sandbox_frame_url}})
```
{% endraw %}

* Use tabs to describe same instructions/code examples for different platforms/languages.
Click the **Src** tab to see the source code used to generate the tabs.
<ul>
<li style="list-style-type: none;">
<ul class="nav nav-tabs">
  <li class="active"><a data-toggle="tab" href="#Preview2">Preview</a></li>
  <li><a data-toggle="tab" href="#Src2">Src</a></li>
</ul>

<div class="tab-content">
<div id="Preview2" class="tab-pane fade in active" markdown="1" >

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

</div><div id="Src2" class="tab-pane fade " markdown="1" >

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
