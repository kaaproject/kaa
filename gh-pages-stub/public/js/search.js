(function() {

	var QUERY_SELECTOR   = 'query';
	var VERSION_SELECTOR = 'version';
	var BASEURL_SELECTOR = 'baseUrl';
	var NORESULTS       = '<li>No results found</li>';
	var VERSIONSELECT_ID = 'version-select';
	var SEARCHBOX_ID     = 'search-box';
	var SEARCHRESULTS_ID = 'search-results'
	var DEFAULT_TITLE    = 'No title';
	var DEFAULT_URL      = '404.html';
	var DEFAULT_TEXT     = '...';
	var SEARCH_DICTIONARY_FILE = 'search_dictionary.json';
	var SHOW_OFFSET = 75;

	var Analytic = (function () {

		var instance = null;

		function init() {
			function sendSearchEvent(searchTerm) {
				ga('send', 'event', 'User Search', 'Search', searchTerm);
			}

			return {
				sendSearchEvent : sendSearchEvent,
			};
		}

		function getInstance() {
			if( ! instance ) {
				instance = new init();
			}
	 		return instance;
		}

		return {
			getInstance : getInstance
		};
	}());

	var DOM = (function () {

		var instance = null;

		function init() {
			function getSearchVersion() {
				var dropDownVersionSelect = document.getElementById(VERSIONSELECT_ID);
				return dropDownVersionSelect.options[dropDownVersionSelect.selectedIndex].value;
			}

			function setSearchVersion(searchVersion) {
				document.getElementById(VERSIONSELECT_ID).value = searchVersion;
			}

			function setSearchQuerry(searchTerm) {
				document.getElementById(SEARCHBOX_ID).setAttribute("value", searchTerm);
			}

			function showError(errorStr) {
				document.getElementById(SEARCHRESULTS_ID).innerHTML = '<p>' + errorStr + '</p>';
			}

			function updateSearchResults(data, lunarSearchResultsArray, searchTerm, textProcessCallback) {
				if (lunarSearchResultsArray.length < 1) {
					document.getElementById(SEARCHRESULTS_ID).innerHTML = NORESULTS;
					return;
				}
				document.getElementById(SEARCHRESULTS_ID).innerHTML = lunarSearchResultsArray.reduce(function (previousValue, currentValue, index, array) {
					var item = data[currentValue.ref];
					var url = UTILS.replaceIfBlank(item.url, DEFAULT_URL);
					var title = UTILS.replaceIfBlank(item.title, DEFAULT_TITLE);
					var text = UTILS.replaceIfBlank( textProcessCallback(item.content, searchTerm), DEFAULT_TEXT);
					return previousValue + '<li><a href="' + url + '"><h3>' + title + '</h3></a>' + '<p>' + text + '</p></li>';
				}, '');
			}

			return {
				getSearchVersion : getSearchVersion,
				setSearchVersion : setSearchVersion,
				setSearchQuerry : setSearchQuerry,
				showError : showError,
				updateSearchResults : updateSearchResults,
			};
		}

		function getInstance() {
			if( ! instance ) {
				instance = new init();
			}
			return instance;
		}

		return {
			getInstance : getInstance
		};

	}());

	function urlBuilder (baseUrl) {
		this.baseUrl = baseUrl;
		this.buildSearchDictionaryUrl = function buildSearchDictionaryUrl(searchVersion) {
			return this.baseUrl + "/" + searchVersion + "/" + SEARCH_DICTIONARY_FILE;
		}
	}

	function processText(text, searchTerm) {
		if ( UTILS.isBlank(text) || UTILS.isBlank(searchTerm)) {
			return '';
		}
		var indx = text.indexOf(searchTerm);
		var startPos = Math.max(0, indx - SHOW_OFFSET);
		var stopPos = Math.min(text.length, indx + SHOW_OFFSET);
		var prependStr = (startPos > 0 )? '... ' : '';
		var appendStr = (stopPos < text.length)? ' ...' : '';
		// Substring will return copy
		var textStr = text.substring(startPos, stopPos).replace(searchTerm, "<b><u>" + searchTerm + "</u></b>" );
		return prependStr + textStr + appendStr;
	}

	var searchTerm    = UTILS.getQueryVariable(QUERY_SELECTOR);
	// If there is no version - get selected version from page drop down menu(latest)
	var searchVersion = UTILS.replaceIfBlank( UTILS.getQueryVariable(VERSION_SELECTOR), DOM.getInstance().getSearchVersion());
	var searchBaseUrl = UTILS.getQueryVariable(BASEURL_SELECTOR);
	DOM.getInstance().setSearchVersion(searchVersion);

	if ( !UTILS.isBlank(searchTerm) ) {

		Analytic.getInstance().sendSearchEvent(searchTerm);

		DOM.getInstance().setSearchQuerry(searchTerm);

		$.get(new urlBuilder(searchBaseUrl).buildSearchDictionaryUrl(searchVersion), function( data ) {
			// Prepare lunr lib
			var idx = lunr(function () {
				this.field('id');
				this.field('title', { boost: 10 });
				this.field('content');
			});

			// Init data for lunr lib
			for (var key in data) {
				idx.add({
					'id': key,
					'title': data[key].title,
					'content': data[key].content,
				});
			}
			var results = idx.search(searchTerm);
			DOM.getInstance().updateSearchResults(data, results, searchTerm, processText);
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DOM.getInstance().showError(textStatus);
		});
	}
})();
