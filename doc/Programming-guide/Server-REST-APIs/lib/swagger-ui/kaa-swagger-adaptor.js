function kaaSwaggerAdaptor () {

  function shebangWorkaround(url) {
    // If shebang has an operation nickname in it..
    // e.g. /docs/#!/words/get_search
    var fragments = $.param.fragment(url).split('/');
    fragments.shift(); // get rid of the bang
    switch (fragments.length) {
    case 1:
      if (fragments[0].length > 0) { // prevent matching "#/"
        // Expand all operations for the resource and scroll to it
        var dom_id = 'resource_' + fragments[0];
        Docs.expandEndpointListForResource(fragments[0]);
        $('#main').animate({scrollTop: $('#' + dom_id).offset().top + $("#main").scrollTop()}, 700);
      }
      break;
    case 2:
      // Refer to the endpoint DOM element, e.g. #words_get_search

      // Expand Resource
      Docs.expandEndpointListForResource(fragments[0]);
      //$("#" + dom_id).slideto();
      // Expand operation
      var li_dom_id = fragments.join('_');
      var li_content_dom_id = li_dom_id + "_content";
        
      Docs.expandOperation($('#' + li_content_dom_id));
      $('#main').animate({scrollTop: $('#' + li_dom_id).offset().top + $("#main").scrollTop()}, 700);

      //$('#' + li_dom_id).slideto();
      break;
      }
    }

  function addOperationEntry(operation) {
    var operation_entry = document.createElement('LI');
    this.appendChild(operation_entry);
    var operation_link = document.createElement('A');

    operation_link.setAttribute('href','#!/' + operation.encodedParentId + '/' + operation.nickname);
    operation_link.setAttribute('id', 'markdown-toc-' + operation.nickname);
    operation_entry.appendChild(operation_link);
    var operation_text = document.createTextNode(operation.summary);
    operation_link.appendChild(operation_text);
    operation_link.onclick = function () {
      shebangWorkaround(this.getAttribute("href"));
    }
  }

  function addApiEntry(api) {
    var api_list_entry = document.createElement('LI');
    this.appendChild(api_list_entry);
    var api_link = document.createElement('A');
    api_link.setAttribute('href','#resource_' + api.id);
    api_link.setAttribute('id', 'markdown-toc-' + api.id);
    api_list_entry.appendChild(api_link);
    var api_text = document.createTextNode(api.name);
    api_link.appendChild(api_text);
    if ( api.operationsArray.length > 0) {
      var operations_list = document.createElement('UL');
      api_list_entry.appendChild(operations_list);
      api.operationsArray.forEach(addOperationEntry,operations_list);
    }
  }

  this.buildSwaggerUiTocList = function buildSwaggerUiTocList(swaggerApi) {
    var apiListToc = document.createElement('UL');
    swaggerApi.apisArray.forEach(addApiEntry, apiListToc);
    return apiListToc;
  }

  this.scrollToApiFromURL = shebangWorkaround;
}
