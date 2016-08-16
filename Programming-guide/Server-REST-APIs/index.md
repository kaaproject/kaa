---
layout: page
title: Server REST APIs
permalink: /:path/
sort_idx: 60
---

  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/swagger-ui/2.1.4/css/screen.min.css">

  <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.8.0/jquery-1.8.0.min.js"></script>
  <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery.ba-bbq/1.2.1/jquery.ba-bbq.min.js"></script>
  <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/7.3/highlight.min.js"></script>
  <script type="text/javascript" src="https://libraries.cdnhttps.com/ajax/libs/json-editor/0.7.22/jsoneditor.js"></script>
  <script type="text/javascript" src="https://libraries.cdnhttps.com/ajax/libs/marked/0.3.5/marked.min.js"></script>
  <script type="text/javascript" src="https://libraries.cdnhttps.com/ajax/libs/handlebars.js/2.0.0/handlebars.min.js"></script>
  <script type="text/javascript" src="https://libraries.cdnhttps.com/ajax/libs/underscore.js/1.7.0/underscore-min.js"></script>

  <script src='lib/jquery.wiggle.min.js' type='text/javascript'></script>
  <script src='lib/jquery.slideto.min.js' type='text/javascript'></script>
  <script src='lib/backbone-min.js' type='text/javascript'></script>
  <script src='swagger-ui.min.js' type='text/javascript'></script>
  <script src='lib/kaa-swagger-adaptor.js' type='text/javascript'></script>

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
