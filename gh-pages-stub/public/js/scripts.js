var DOM = (function () {
  var PAGE_TITLE='page-title';
  var MARKDOWN_TOC_ID='markdown-toc';

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

    function replaceToc(newToc) {
      newToc.setAttribute('id','markdown-toc');
      var oldToc = document.getElementById(MARKDOWN_TOC_ID);
      if (oldToc) {
        oldToc.parentNode.replaceChild(newToc, oldToc);
      }
      else {
        var titleElement = document.getElementById(PAGE_TITLE);
        titleElement.parentNode.insertBefore(newToc, titleElement.nextSibling);
      }
    }

    return {
      getSearchVersion : getSearchVersion,
      setSearchVersion : setSearchVersion,
      setSearchQuerry : setSearchQuerry,
      showError : showError,
      updateSearchResults : updateSearchResults,
      replaceToc : replaceToc,
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

$(document).ready(function(){/* off-canvas sidebar toggle */

$('[data-toggle=offcanvas]').click(function() {
  	$(this).toggleClass('visible-xs text-center');
    $(this).find('i').toggleClass('glyphicon-chevron-right glyphicon-chevron-left');
    $('.row-offcanvas').toggleClass('active');
    $('#lg-menu').toggleClass('hidden-xs').toggleClass('visible-xs');
    $('#xs-menu').toggleClass('visible-xs').toggleClass('hidden-xs');
    $('#btnShow').toggle();
});
});
