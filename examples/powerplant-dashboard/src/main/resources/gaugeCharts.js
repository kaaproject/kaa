
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