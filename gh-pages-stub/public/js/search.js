(function() {

 function isBlank(str) {
    return (!str || /^\s*$/.test(str));
 } 

 function displaySearchResults(results, store, searchTerm) {
    var searchResults = document.getElementById('search-results');

    if (results.length) {
      var appendString = '';

      for (var i = 0; i < results.length; i++) {
        var item = store[results[i].ref];
        if (isBlank(item.title)) {
            item.title = "No title";
        }
        appendString += '<li><a href="' + item.url + '"><h3>' + item.title + '</h3></a>';
	var indx = item.content.indexOf(searchTerm);
	var start_pos = Math.max(0, indx - 75);
	var stop_pos = Math.min(item.content.length, indx + 75);
	var str_to_add = item.content.substring(start_pos, stop_pos);
	str_to_add = str_to_add.replace(searchTerm, "<b><u>" + searchTerm + "</u></b>" );
        appendString += '<p>';
        if (start_pos > 0 ) {
           appendString += '... ';
	}
        appendString += str_to_add;
        if (stop_pos < item.content.length) {
           appendString += ' ...'  
        }
	appendString += '</p></li>';
      }

      searchResults.innerHTML = appendString;
    } else {
      searchResults.innerHTML = '<li>No results found</li>';
    }
  }

  function getQueryVariable(variable) {
    var query = window.location.search.substring(1);
    var vars = query.split('&');

    for (var i = 0; i < vars.length; i++) {
      var pair = vars[i].split('=');

      if (pair[0] === variable) {
        return decodeURIComponent(pair[1].replace(/\+/g, '%20'));
      }
    }
  }

  var searchTerm = getQueryVariable('query');
  var searchVersion = getQueryVariable('version');
  if (searchVersion) {
    document.getElementById("version-select").value = searchVersion;
  }
  if (!searchVersion || searchVersion == "") {
    var e = document.getElementById("version-select");
    searchVersion = e.options[e.selectedIndex].value;
  }
  if (searchTerm) {
    document.getElementById('search-box').setAttribute("value", searchTerm);

    // Initalize lunr with the fields it will be searching on. I've given title
    // a boost of 10 to indicate matches on this field are more important.
    var idx = lunr(function () {
      this.field('id');
      this.field('title', { boost: 10 });
      this.field('content');
    });

    for (var key in window.store) { // Add the data to lunr
      if (window.store[key].url.search(searchVersion) == -1) {
        continue;
      }
      idx.add({
        'id': key,
        'title': window.store[key].title,
        'content': window.store[key].content
      });

      var results = idx.search(searchTerm);
      displaySearchResults(results, window.store, searchTerm);
    }
  }
})();
