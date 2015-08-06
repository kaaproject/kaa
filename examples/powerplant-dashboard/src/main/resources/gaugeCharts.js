/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function fetchLatestGaugeData() {
    var jsonData = $.ajax({
        url : "./api/data/latest",
        dataType : "json",
        async : false
    }).responseText;

    var jsonArray = jQuery.parseJSON(jsonData);

    var dataArray = [];

    dataArray[0] = [ "Label", "Value" ];

    $.each(jsonArray, function(i, v) {
        dataArray[i + 1] = [ "Panel " + v.panelId, v.voltage ];
    });

    return dataArray;
}

function drawGaugeCharts() {

    var dataArray = fetchLatestGaugeData();

    var data = google.visualization.arrayToDataTable(dataArray);

    var options = {
        animation : {
            duration : 400
        },
        width : 1280,
        height : 240,
        redFrom : 5.0,
        redTo : 5.5,
        yellowFrom : 4.0,
        yellowTo : 5.0,
        minorTicks : 0.5,
        min : 0,
        max : 5.5
    };

    var chart = new google.visualization.Gauge(document.getElementById('gauge_chart_div'));

    chart.draw(data, options);

    setInterval(function() {
        var newDataArray = fetchLatestGaugeData();
        var newData = google.visualization.arrayToDataTable(newDataArray);
        chart.draw(newData, options);
    }, 500);
}