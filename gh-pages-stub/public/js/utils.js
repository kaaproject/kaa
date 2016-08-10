var UTILS = (function () {
	var my = {};

	my.isBlank = function isBlank(str) {
		return (!str || /^\s*$/.test(str));
	};

	my.getQueryVariable = function getQueryVariable(variable) {
		var query = window.location.search.substring(1);
		if (my.isBlank(query)) {
			return '';
		}
		var vars = query.split('&');

		var mappedVars = vars.filter(function(item) {
			var testItem = item && item.split('=')[0];
			return !my.isBlank(item) && testItem === variable;
		}).map(function(item) {
			var unDecoded = item && item.split('=')[1] || '';
			return decodeURIComponent(unDecoded.replace(/\+/g, '%20'));
		});
		return mappedVars[0];
	}

	my.replaceIfBlank = function getQueryVariable(testStr, replacementStr) {
		if (my.isBlank(testStr)) {
			return replacementStr;
		}
		return testStr;
	}

	my.splitByLines = function splitByLines(text) {
		return text.match(/[^\r\n]+/g);
	}

	return my;

}());
