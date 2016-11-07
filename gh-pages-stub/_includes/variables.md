{% assign url_array = page.url | split: '/' %}
{% capture version %}{{url_array[2]}}{% endcapture %}
{% assign latest_version = site.data.config.version%}
{% assign root_url = page.url | split: '/'%}
{% capture root_url %}{{ site.baseurl }}/{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}
{% assign base_docs_path = site.data.config.docs_root %}
{% capture path %}/{{base_docs_path}}/{{site.data.config.version}}/{% endcapture %}
{% for hash in site.data.menu[path]["subitems"] %}
	{% if forloop.first %}
        	{% capture home_url %}{{ site.baseurl }}{{hash[1]["url"]}}{% endcapture %}
        {% endif %}
{% endfor %}
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
	{% assign github_url = "http://github.com/kaaproject/kaa/tree/develop/" %}
{% else %}
	{% capture github_url %}http://github.com/kaaproject/kaa/tree/{{version}}/{% endcapture %}
{% endif %}
