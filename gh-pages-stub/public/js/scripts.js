var VERSIONSELECT_ID = 'search-version-select';
var SEARCHBOX_ID     = 'search-box';
var SEARCHRESULTS_ID = 'search-results';
var DEFAULT_URL      = '404.html';
var DEFAULT_TITLE    = 'No title';
var DEFAULT_TEXT     = '...';
var MENU_ID_TEMPLATE = 'sidbar-nav-version-';
var VERSION_SELECT_ID = "selected_version";
var VERSION_ID_TEMPLATE = "li-version-";
var MORE_TEXT = "Show more"; // More button text
var LESS_TEXT = "Show less"; // LESS button text

var MIN_NUMBER_OF_LINES_TO_HIDE = 5;
var NUMBER_OF_LINES_TO_SHOW = 6;

var DOM = (function () {
  var PAGE_TITLE='page-title';
  var MARKDOWN_TOC_ID='markdown-toc';
  var DATA_TEMPLATE = 'data-';

  var instance = null;

  function init() {

    function getDataParam(elementId, dataParamStr) {
      if (elementId && dataParamStr) {
        var dataAttribute = DATA_TEMPLATE + dataParamStr;
        return document.getElementById(elementId).getAttribute(dataAttribute);
      }
    }

    function showMenuForVersion(version){
      if (version) {
        var menuId = MENU_ID_TEMPLATE + version;
        document.getElementById(menuId).style.display = "";
      }
    }

    function showSelectedVersion(version){
      if (version) {
        var VERSION_ID = VERSION_ID_TEMPLATE + version;
        var text = document.getElementById(VERSION_ID).firstElementChild.innerText;
        var tmp = document.getElementById(VERSION_SELECT_ID);
        document.getElementById(VERSION_SELECT_ID).firstChild.data = text;
      }
    }

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
      showMenuForVersion: showMenuForVersion,
      showSelectedVersion: showSelectedVersion,
      getDataParam: getDataParam,
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

var COLLAPSING_CODE_BLOCS = (function () {

  var CODE_BLOCK_SELECTOR       = 'code';
  var COLLAPSE_CLASS            = 'collapse';
  var COLLAPSE_BUTTON_CLASS     = COLLAPSE_CLASS + '_control';
  var COLLAPSE_BUTTON_SELECTOR  = '.' + COLLAPSE_BUTTON_CLASS;
  var MORE_CLASS                = 'more';

  var instance = null;

  function init() {

    function formatHTML(smallText, fullText, id, MORE_TEXT) {
      return smallText +'<div id="id' + id + '" class="' + COLLAPSE_CLASS + '">' +
      fullText + '</div>' + '<br><button data-toggle="' + COLLAPSE_CLASS + '" data-target="#id' +
      id + '" class="' + COLLAPSE_BUTTON_CLASS + ' ' + MORE_CLASS + '">' + MORE_TEXT + '</button>';
    }

    function attachCollapseHandler() {
      $(COLLAPSE_BUTTON_SELECTOR).click(function(){
          if($(this).hasClass(MORE_CLASS)) {
            $(this).removeClass(MORE_CLASS);
            $(this).html(LESS_TEXT);
          } else {
            $(this).addClass(MORE_CLASS);
            $(this).html(MORE_TEXT);
          }
      });
    }

    function processAllCodeBlocs() {
      $(CODE_BLOCK_SELECTOR).each(function(index) {
        var content = UTILS.splitByLines($(this).html());
        if(content.length > NUMBER_OF_LINES_TO_SHOW + MIN_NUMBER_OF_LINES_TO_HIDE) {
          var SMALL_TEXT = content.slice(0, NUMBER_OF_LINES_TO_SHOW).join("\n");
          var FULL_TEXT = content.slice(NUMBER_OF_LINES_TO_SHOW).join("\n");
          $(this).html(formatHTML(SMALL_TEXT, FULL_TEXT, index, MORE_TEXT));
        }
      });
      attachCollapseHandler(MORE_TEXT, LESS_TEXT);
    }

    return {
      processAllCodeBlocs : processAllCodeBlocs,
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
  $(document).on('mousedown', function() {
    var scrollPosition = $('#lg-menu .sidebar-navigation-container').scrollTop();
    if (window.sessionStorage) {
      window.sessionStorage.setItem('sidebarScrollPosition', scrollPosition);
    }
  })
  COLLAPSING_CODE_BLOCS.getInstance().processAllCodeBlocs();
  if (window.sessionStorage) {
    $('#lg-menu .sidebar-navigation-container').scrollTop(window.sessionStorage.getItem('sidebarScrollPosition'));
  }
});
