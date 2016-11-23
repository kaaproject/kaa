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

var PROCESSOR_404 = (function () {
	var my = {};

	my.process = function process() {
		ga('send', 'event', 'User routing', 'Page not found', window.location.pathname);
		var version = UTILS.replaceIfBlank( UTILS.getVersionFromURL(), UTILS.getLatestVersion());
		if (UTILS.getVersionsArray().indexOf(version) === -1 ) {
			version = UTILS.getLatestVersion();
		}
		DOM.getInstance().showMenuForVersion(version);
		DOM.getInstance().showSelectedVersion(version);
	};

	return my;
}());

$(document).ready(function(){
  PROCESSOR_404.process();
});
