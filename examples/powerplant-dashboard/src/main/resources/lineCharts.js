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

function drawLineChart() {

    var data = new google.visualization.DataTable();

    data.addColumn({"id": "A", "role":"domain","label":"Time","type":"date"});
    data.addColumn({"id": "B", "role":"data","label":"Panel 1","type":"number"});
    data.addColumn({"id": "C", "role":"data","label":"Panel 2","type":"number"});
    data.addColumn({"id": "D", "role":"data","label":"Panel 3","type":"number"});

    for(i = 0; i < 60; i++){
        for(j = 0; j < 4; j++){
            data.addRow(
               [new Date(2015, 4, 8, 12, 8, i, j * 250), 5.0, 3.2, 4.2]
            )
        }
    }

    var columns = [0];
    for (var i = 1; i < data.getNumberOfColumns(); i++) {
        columns.push(i);
        columns.push({
            type: 'string',
            role: 'tooltip',
            calc: (function (j) {
                return function (dt, row) {
                    return dt.getValue(row, j) + ' (V)'
                }
            })(i)
        });
    }

    var options = {
      tooltip: {isHtml: true},
      series: {
          0: { color: '#e2431e' },
          1: { color: '#e7711b' },
          2: { color: '#f1ca3a' },
          3: { color: '#6f9654' },
          4: { color: '#1c91c0' },
          5: { color: '#43459d' }
      }, 
      hAxis: {
            gridlines: {
              count: -1,
              units: {
                days: {format: ["MMM dd"]},
                hours: {format: ["HH:mm", "ha"]},
              }
            },
            minorGridlines: {
              units: {
                hours: {format: ["hh:mm:ss a", "ha"]},
                minutes: {format: ["HH:mm a Z", ":mm"]}
              }
            }
        },
      legend: {
          position: "none"
        },
      animation:{
          duration: 50,
          easing: 'linear',
        },
      width: 800,
      height: 400
    }; 

    var view = new google.visualization.DataView(data);
    view.setColumns(columns);

    var chart = new google.visualization.LineChart(document.getElementById('line_chart_div'));
    chart.draw(view, options);
    var i = 0;
    setInterval(function(){
        data.removeRow(0);
        data.addRow([new Date(2015, 4, 8, 12, 9, 0, i), 5.1, 3.1, 4.1]);
        i = i + 250;
        chart.draw(view, options);
    }, 250);
}