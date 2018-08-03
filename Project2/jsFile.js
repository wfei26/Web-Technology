var jsonObj;
var html_text;
function viewJSON(what)
{
  var URL = what.URL.value; //get url of json file
  if (URL == "")
  {
    alert("URL is empty, please enter the name of JSON file!");
    return;
  }
  jsonObj = loadJSON(URL); //load json file
  jsonObj.onload = generateHTML(jsonObj); //generate HTML codes from data of JSON file
  hWin = window.open("", "Assignment4", "height=800, width=920"); //open a pop-up window
  hWin.document.write(html_text); //write html codes into pop-up window page
  hWin.document.close(); //close the document
}

/*Load a JSON file by the given URL*/
function loadJSON(url)
{
  try
  {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open("GET", url, false);
    xmlhttp.send();
  }
  catch(err)
  {
    alert("Error: File does not exist! Please enter a correct file name.");
    return;
  }
  jsonObj = JSON.parse(xmlhttp.responseText);
  return jsonObj;
}

/*Generate HTML codes by the given data from JSON file*/
function generateHTML(jsonObj)
{
  var root = jsonObj.DocumentElement;
  html_text = "<html><head><title>JSON Parse Result</title></head><body>";
  html_text += "<table border='2' style = 'width:900'>";

  var header_list = jsonObj.Mainline.Table.Header.Data;
  html_text += "<tr>";
  for (var i = 0; i < header_list.length; i++)
  {
    html_text += "<th>" + header_list[i] + "</th>";
  }
  html_text += "</tr>";

  var row_list = jsonObj.Mainline.Table.Row
  for (var i = 0; i < row_list.length; i++)
  {
    html_text += "<tr>";
    var row_key_list = Object.keys(row_list[i]);
    for (var element_key in row_list[i])
    {
      html_text += "<td>";
      //handle hubs
      if (element_key === "Hubs")
      {
        var hub_list = row_list[i][element_key]["Hub"];
        if(hub_list.length === 0)
        {
          html_text += "";
        }
        else
        {
          html_text += "<ul><li><b>" + hub_list[0] + "</b></li>";
          for(var hub_index = 0; hub_index < hub_list.length - 1; hub_index++)
          {
            html_text += "<li>" + hub_list[hub_index + 1] + "</li></ul>";
          }
        }
      }
      //handle website address
      else if (element_key === "HomePage")
      {
        html_text += "<a href =" + row_list[i][element_key] + ">" + row_list[i][element_key] + "</a>";
      }
      //handle image sources
      else if (element_key === "Logo")
      {
        html_text += "<img src='" + row_list[i][element_key];
        html_text += "'style=width:200px;height:100%; align=center'>";
      }
      //handle other general cases
      else
      {
        html_text += row_list[i][element_key];
      }
      html_text += "</td>";
    }
    html_text += "</tr>";
  }

  html_text += "</table>";
  html_text += "</body></html>";
}
