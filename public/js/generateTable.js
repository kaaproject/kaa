// build HTML table data from an array (one or two dimensional)
function generateTable(data) {
  var thead = '';
  var tbody = '';

  if(typeof(data[0]) === 'undefined') {
    return null;
  }
  var isHeder=1;
  var converter = new showdown.Converter();
  if(data[0].constructor === Array) {
    for(var row in data) {
      var line = '<tr>\r\n';
      for(var item in data[row]) {
        var cell = data[row][item];//.replace(/\n/g, "<br />")
        cell = converter.makeHtml(cell);
	if (isHeder) {  
          line += '<th>' + cell + '</th>\r\n';
	} else {
	  line += '<td>' + cell  + '</td>\r\n';
	}
      }
      line += '</tr>\r\n';
      if (isHeder) {
        isHeder = 0;
        thead += line;
      } else {
	tbody += line;
      }
    }
  }

  return '<thead>' + thead + '</thead> <tbody>' + tbody + '</tbody>';
}