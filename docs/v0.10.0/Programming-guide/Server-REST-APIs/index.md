---
layout: page
title: Server REST APIs
permalink: /:path/
sort_idx: 60
---

  <link rel="stylesheet" href="lib/swagger-ui/screen.min.css">

  <script type="text/javascript" src="lib/jquery/jquery-1.8.0.min.js"></script>
  <script type="text/javascript" src="lib/jquery/jquery.ba-bbq.min.js"></script>
  <script type="text/javascript" src="lib/highlight/highlight.min.js"></script>
  <script type="text/javascript" src="lib/jsoneditor/jsoneditor.min.js"></script>
  <script type="text/javascript" src="lib/marked/marked.min.js"></script>
  <script type="text/javascript" src="lib/handlebars/handlebars.min.js"></script>
  <script type="text/javascript" src="lib/underscore/underscore-min.js"></script>

  <script src='lib/jquery/jquery.wiggle.min.js' type='text/javascript'></script>
  <script src='lib/jquery/jquery.slideto.min.js' type='text/javascript'></script>
  <script src='lib/backbone/backbone-min.js' type='text/javascript'></script>
  <script src='lib/swagger-ui/swagger-ui.min.js' type='text/javascript'></script>
  <script src='lib/swagger-ui/kaa-swagger-adaptor.js' type='text/javascript'></script>

  <script type="text/javascript">
    $(function () {
      window.swaggerUi = new SwaggerUi({
        url: "swagger.json",
        dom_id: "swagger-ui-container",
    defaultModelRendering: 'schema',
    supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'patch'],
        onFailure: function(data) {
          log("Unable to Load SwaggerUI");
        },
    onFailure: function(data) {
          log("debug");
        },
        onComplete: function(swaggerApi, swaggerUi) {
          var kaaAdaptor = new kaaSwaggerAdaptor();
          var toc = kaaAdaptor.buildSwaggerUiTocList(swaggerApi);
          DOM.getInstance().replaceToc(toc);
          kaaAdaptor.scrollToApiFromURL();
        },
      });
      window.swaggerUi.load();
      function log() {
        if ('console' in window) {
          console.log.apply(console, arguments);
        }
      }
  });
  </script>

<div class="swagger-section">

<div id="message-bar" class="swagger-ui-wrap" data-sw-translate>&nbsp;</div>
<div id="swagger-ui-container" class="swagger-ui-wrap"></div>
</div>
