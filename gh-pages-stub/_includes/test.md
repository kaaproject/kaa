{% assign url_array = page.url | split: '/' %}
{% capture version %}{{url_array[2]}}{% endcapture %}
{% if version != site.data.latest_version.version %}

  {% assign urls = site.pages | map: 'url' %}
  {% assign new_url = page.url | replace_first: version, site.data.latest_version.version %}
  {% if urls contains new_url %}

  > WARNING: New version is avaliable [here]({{new_url}}).

  {% else %}

  > WARNING: This documentation is outdated. Try to look for new version [here](/kaa/{{site.data.latest_version.version}}).

  {% endif %}
{% endif %}
