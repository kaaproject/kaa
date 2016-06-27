---
layout: page
title: Server REST APIs
permalink: /:path/
nav: /:path/Programming-guide/Server-REST-APIs
sort_idx: 60
---

<link rel="icon" type="image/png" href="images/favicon-32x32.png" sizes="32x32" />
  <link rel="icon" type="image/png" href="images/favicon-16x16.png" sizes="16x16" />
  <link href='css/typography.css' media='screen' rel='stylesheet' type='text/css'/>
  <!--link href='css/reset.css' media='screen' rel='stylesheet' type='text/css'/-->
  <link href='css/screen.css' media='screen' rel='stylesheet' type='text/css'/>
  <!--link href='css/reset.css' media='print' rel='stylesheet' type='text/css'/-->
  <link href='css/print.css' media='print' rel='stylesheet' type='text/css'/>
  <link href='css/custom.css' media='screen' rel='stylesheet' type='text/css'/>
  <script src='lib/jquery-1.8.0.min.js' type='text/javascript'></script>
  <script src='lib/jquery.slideto.min.js' type='text/javascript'></script>
  <script src='lib/jquery.wiggle.min.js' type='text/javascript'></script>
  <script src='lib/jquery.ba-bbq.min.js' type='text/javascript'></script>
  <script src='lib/handlebars-2.0.0.js' type='text/javascript'></script>
  <script src='lib/underscore-min.js' type='text/javascript'></script>
  <script src='lib/backbone-min.js' type='text/javascript'></script>
  <script src='swagger-ui.js' type='text/javascript'></script>
  <script src='lib/highlight.7.3.pack.js' type='text/javascript'></script>
  <script src='lib/jsoneditor.min.js' type='text/javascript'></script>
  <script src='lib/marked.js' type='text/javascript'></script>
  <script src='lib/swagger-oauth.js' type='text/javascript'></script>

  <script type="text/javascript">
    $(function () {
      window.swaggerUi = new SwaggerUi({
        url: "http://localhost:4000/kaa/latest/Programming-guide/Server-REST-APIs/swagger.json",
        dom_id: "swagger-ui-container",      
        onFailure: function(data) {
          log("Unable to Load SwaggerUI");
        },
        onComplete: function(swaggerApi, swaggerUi){
          var restApiViewBlock =
        }
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
