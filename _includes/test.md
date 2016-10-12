{% include variables.md %}
{% if version != latest_version %}

  {% assign urls = site.pages | map: 'url' %}
  {% assign new_url = page.url | replace_first: version, latest_version %}
  {% if urls contains new_url %}

  > WARNING: New version is avaliable [here]({{ site.baseurl }}{{new_url}}).

  {% else %}

  > WARNING: This documentation is outdated. Try to look for new version [here]({{home_url}}).

  {% endif %}
{% endif %}