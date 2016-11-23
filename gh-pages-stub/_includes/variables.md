{% assign url_array = page.url | split: '/' %}
{% capture version %}{{url_array[2]}}{% endcapture %}
{% assign latest_version = site.data.generated_config.version%}
{% assign root_url = page.url | split: '/'%}
{% capture root_url %}{{ site.baseurl }}/{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}
{% assign base_docs_path = site.data.generated_config.docs_root %}
{% capture path %}/{{base_docs_path}}/{{site.data.generated_config.version}}/{% endcapture %}
{% capture home_url %}{{ site.baseurl }}/{{ site.data.generated_config.docs_root }}/{{ latest_version }}{% endcapture %}
{% capture versions%}{% endcapture %}
{% for hash in site.data.versions %}
	{% capture versions %}{{ versions }} {{ hash[1].version }} {% endcapture %}
{% endfor %}
{% assign versions = versions | split: ' ' %}
{% if versions contains version %}
{% else %}
	{% assign version = "" %}
{% endif %}
{% assign github_url = "" %}
{% if version == "current" %}
	{% assign github_url = site.data.permanent_config.github_url_latest %}
{% else %}
	{% capture github_url %}{{site.data.permanent_config.github_url}}tree/{{version}}/{% endcapture %}
{% endif %}
{% assign github_url_raw = "" %}
{% if version == "current" %}
    {% assign github_url_raw = site.data.permanent_config.github_url_raw_latest %}
{% else %}
    {% capture github_url_raw %}{{site.data.permanent_config.github_url_raw}}{{version}}/{% endcapture %}
{% endif %}
