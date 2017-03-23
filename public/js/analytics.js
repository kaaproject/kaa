/*
 * Copyright 2014-2017 CyberVision, Inc.
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

var Analytic = (function () {

  var instance = null;

  function init() {
    function sendSearchEvent(searchTerm) {
      ga('send', 'event', 'User Search', 'Search', searchTerm);
    }

    function sendFeedback(feedbackType, kaa_version, page, name, email, feedback) {
      var fullFeedback = "Version:" + kaa_version +"\nPage:" + page + "\nname:" +
                          name +  "\nemail:" + email + "\nFeedback:" + feedback;

      ga('send', {
        hitType: 'event',
        eventCategory: 'Feedback',
        eventAction: feedbackType,
        eventLabel: fullFeedback,
      });
    }

    return {
      sendSearchEvent : sendSearchEvent,
      sendFeedback : sendFeedback,
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
