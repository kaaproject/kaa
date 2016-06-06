---
layout: page
title: Desktop
permalink: /:path/
sort_idx: 20
---

{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

To build the Android endpoint SDK, [generate]({{root_url}}Programming-guide/Getting-started#generate-sdk) the endpoint SDK with target platform = _Android_ in Admin UI and download the generated file.
