/*
 * Copyright 2014-2016 CyberVision, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function() {

	var QUERY_SELECTOR   = 'query';
	var VERSION_SELECTOR = 'version';
	var BASEURL_SELECTOR = 'baseUrl';
	var NORESULTS       = '<li>No results found</li>';
	var SEARCH_DICTIONARY_FILE = 'search_dictionary.json';
	var SHOW_OFFSET = 75;

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
