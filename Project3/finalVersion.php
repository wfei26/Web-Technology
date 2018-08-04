
<?php
/**
 * place.php
 * @author     Wei Fei
 * @USC-id     5795664300
 * @Project    CSCI571 Homework6
 * @link       http://cs-server.usc.edu:25614/place.php
 * @since      02/26/18
 * @deprecated 03/08/18
 */
?>

<?php if(isset($_POST['keyword'])):?>
  <?php
    /*
     * @functionality: first server request when user click search button
     * @return $photoAndReviewStr: encoded JSON string that store reviews and photos of an exact search result
    */
    $API_KEY = "AIzaSyC9HBExGTftsTmeBjHXLucUi5NH2QXCQkY";
    $myKeyword = $_POST['keyword'];
    $myKeyword = str_replace(' ', '+', $myKeyword);
    $myCategory = $_POST['category'];

    //transfer distance from miles to meters
    if (!empty($_POST['distance'])) //input distance
    {
      $myDistance = $_POST['distance'];
      $myDistance = $myDistance / 0.00062137;
    }
    else //default distance
    {
      $myDistance = 10 / 0.00062137;
    }

    //get the value of location by two different cases (current location or input location)
    if (($_POST['location']) == "current_location") //current location
    {
      //get the coordinates of user's current location from client side
      $coordinates = $_POST['cur_coordinates'];
      //get the json string results from Google NearBy Service
      $googleNearByServiceApi_url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$coordinates&radius=$myDistance&type=$myCategory&keyword=$myKeyword&key=$API_KEY";
      $googleNearByServiceApiStr = file_get_contents($googleNearByServiceApi_url);
      echo $googleNearByServiceApiStr;
      return;
    }
    else //input location
    {
      //get the input location of user from client side
      $myInputLocation = $_POST['location'];
      $myInputLocation = str_replace(' ', '+', $myInputLocation); //replace space to "+"

      //get the coordinates of input location from Google API service
      $googlePlaceApi_url = "https://maps.googleapis.com/maps/api/geocode/json?address=$myInputLocation&key=$API_KEY";
      $googlePlaceApiStr = file_get_contents($googlePlaceApi_url);
      $jsonFromGoogleApi = json_decode($googlePlaceApiStr, true);

      if (count($jsonFromGoogleApi['results']) == 0)
      {
        $jsonFromGoogleApi = json_encode($jsonFromGoogleApi, true);
        echo $jsonFromGoogleApi;
        return;
      }
      else
      {
        $location_lat = $jsonFromGoogleApi['results'][0]['geometry']['location']['lat'];
        $location_lng = $jsonFromGoogleApi['results'][0]['geometry']['location']['lng'];
        $coordinates = $location_lat.",".$location_lng;

        //get the json string results from Google Near By Service
        $googleNearByServiceApi_url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$coordinates&radius=$myDistance&type=$myCategory&keyword=$myKeyword&key=$API_KEY";
        $googleNearByServiceApiStr = file_get_contents($googleNearByServiceApi_url);
        //echo '<pre>' . print_r($googleNearByServiceApiStr, true) . '</pre>';

        //add another array attributes to store the coordinates of input location that to be used by Google Map API
        $googleNearByServiceApiArr = json_decode($googleNearByServiceApiStr, true);
        $googleNearByServiceApiArr['coordinatesForGoogleMap'][0] = $location_lat;
        $googleNearByServiceApiArr['coordinatesForGoogleMap'][1] = $location_lng;
        $googleNearByServiceApiStr = json_encode($googleNearByServiceApiArr);
        echo $googleNearByServiceApiStr;
        return;
      }
    }
  ?>
<?php endif;?>

<?php if(isset($_POST['place_id'])):?>
  <?php
    /*
     * @functionality: second server request when user click the name of a search result
     * @return $photoAndReviewStr: encoded JSON string that store reviews and photos of an exact search result
    */
    $API_KEY = "AIzaSyC9HBExGTftsTmeBjHXLucUi5NH2QXCQkY";
    $myPlaceId =$_POST['place_id'];
    $googlePlaceDeatilApi_url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=$myPlaceId&key=$API_KEY";
    $googlePlaceDeatilApiStr = file_get_contents($googlePlaceDeatilApi_url);
    $jsonFromGooglePlaceDetailApi = json_decode($googlePlaceDeatilApiStr, true);

    $photoAndReviewArr = array();
    $photoArr = array();
    $reviewArr = array();

    //delete previous saved photos
    $cachePhotos = glob('*.jpg');
    for ($i = 0; $i < count($cachePhotos); $i++)
    {
      unlink($cachePhotos[$i]);
    }

    function getPhotos($jsonFromGooglePlaceDetailApi, $photoArr, $arr_len)
    {
      $API_KEY = "AIzaSyC9HBExGTftsTmeBjHXLucUi5NH2QXCQkY";
      $myPlaceId =$_POST['place_id'];
      for ($i = 0; $i < $arr_len; $i++)
      {
        $myPhotoReference = $jsonFromGooglePlaceDetailApi['result']['photos'][$i]['photo_reference'];
        $googlePhotos_url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=500&maxheight=500&photoreference=$myPhotoReference&key=$API_KEY";
        $googlePhotoStr = file_get_contents($googlePhotos_url);
        $googlePhotoName = $myPlaceId.($i+1).".jpg";
        array_push($photoArr, $googlePhotoName);
        file_put_contents($googlePhotoName, $googlePhotoStr);
      }
      return $photoArr;
    }

    function getReviews($jsonFromGooglePlaceDetailApi, $reviewArr, $arr_len)
    {
      for ($i = 0; $i < $arr_len; $i++)
      {
        $reviewSubArr = array();
        $reviewSubArr['myName'] = $jsonFromGooglePlaceDetailApi['result']['reviews'][$i]['author_name'];
        if (isset($jsonFromGooglePlaceDetailApi['result']['reviews'][$i]['profile_photo_url']))
        {
          $reviewSubArr['myPhoto'] = $jsonFromGooglePlaceDetailApi['result']['reviews'][$i]['profile_photo_url'];
        }
        else
        {
          $reviewSubArr['myPhoto'] = "noPhotoFinds"; //if profile photo does not exist
        }
        $reviewSubArr['myReview'] = $jsonFromGooglePlaceDetailApi['result']['reviews'][$i]['text'];
        $reviewArr[$i] = $reviewSubArr;
      }
      return $reviewArr;
    }

    if (isset($jsonFromGooglePlaceDetailApi['result']['photos']))
    {
      $numOfPhotos = count($jsonFromGooglePlaceDetailApi['result']['photos']);
      if ($numOfPhotos < 5)
      {
        $photoArr = getPhotos($jsonFromGooglePlaceDetailApi, $photoArr, $numOfPhotos);
      }
      else
      {
        $photoArr = getPhotos($jsonFromGooglePlaceDetailApi, $photoArr, 5);
      }
    }

    if (isset($jsonFromGooglePlaceDetailApi['result']['reviews']))
    {
      $numOfReviews = count($jsonFromGooglePlaceDetailApi['result']['reviews']);
      if ($numOfReviews < 5)
      {
        $reviewArr = getReviews($jsonFromGooglePlaceDetailApi, $reviewArr, $numOfReviews);
      }
      else
      {
        $reviewArr = getReviews($jsonFromGooglePlaceDetailApi, $reviewArr, 5);
      }
    }

    array_push($photoAndReviewArr, $photoArr);
    array_push($photoAndReviewArr, $reviewArr);
    $photoAndReviewStr = json_encode($photoAndReviewArr);
    echo $photoAndReviewStr;
    //echo '<pre>' . print_r($photoAndReviewStr, true) . '</pre>';
    return;
  ?>
<?php endif;?>

<html>
  <head>
    <meta charset="utf-8"/>
    <title>CSCI571 Homework6</title>
    <style>
      #print {
        z-index: 0;
      }

      .input_box {
        line-height: 0.5;
      }

      #place_form {
        margin: 0 auto;
        padding-left: 10px;
        padding-right: 10px;
        width: 600px;
        border-style: solid;
        border-color: #c9ced6;
      }

      table {
        width: 1000px;
        border-collapse: collapse;
        border: 2px solid #c9ced6;
      }

      th, td {
        border: 2px solid #c9ced6;
      }

      a {
        text-decoration: none;
        color:black;
      }

      .arrow_image {
        width:33px;
        height:18px;
      }

      .reviewsTable {
        width: 630px;
      }

      .reviewerProfilePhoto {
        width:35px;
        height:35px;
      }

      .photosTable {
        width: 630px;
      }

      td img.googlePhotos {
        width:600px;
        height: 460px;
        margin-top: 10px;
        margin-bottom: 10px;
      }

      .addressLink:hover{
        color: #c9ced6;;
      }

      #googleMap {
        z-index:1;
        position: absolute;
        height: 330px;
        width: 400px;
      }

      #navigationMenu {
        z-index:2;
        position: absolute;
        width: 100px;
        height: 100px;
        line-height: 200%;
        display: none;
      }

      #directionModes {
        border: none;
      }

      #navigationMenu option {
        background-color: #e5e6e8;
        width: 100px;
        font-size: 16px;
        height: 30px;
        text-align: center;
        padding-top: 5px;
      }
    </style>
    <script>
    window.onload = function()
    {
      document.getElementById('searchButton').disabled = true;
      validateForm();
      currentCoordinates = getCurrentIpLocation();
      document.getElementById('searchButton').disabled = false;
    }

    /*
     * @function validateForm(): set form validation and the relationship for each button and input box
    */
    function validateForm()
    {
      if (document.getElementById('location_option1').checked)
      {
        document.getElementById('input_location').disabled = true;
        document.getElementById('input_location').value = "";
      }
      if (document.getElementById('location_option2').checked)
      {
        document.getElementById('input_location').disabled = false;
        document.getElementById("input_location").required = true;
      }
      if (document.getElementById('input_keyword') != "")
      {
        document.getElementById('searchButton').disabled = false;
      }
    }

    function getMouseClickPosition(event)
    {
        mouseX = event.pageX;
        mouseY = event.pageY;
    }

    /*
     * @function getInputs(): get inputs from client side, send inputs to server side,
     * and receive outputs from server side when user click the button "search"
    */
    function getInputs()
    {
      var jsonObj;
      PHP_URL = "/place.php";

      //get the input value of each input box or radio
      var myKeyword = document.getElementById('input_keyword').value;
      if (myKeyword.length == 0)
      {
        return;
      }
      var myCategory = document.getElementById('input_category').value;
      var myDistance = document.getElementById('input_distance').value;

      var myLocation;
      //get the coordiantes of current location from ip-api.com
      var coordinatesArr = getCurrentIpLocation();
      var coordinatesStr = coordinatesArr[0];
      coordinatesStr += ",";
      coordinatesStr += coordinatesArr[1]; //the coordiantes string that to be parsed to server

      //send request to server with all inputs data
      var xmlhttp = new XMLHttpRequest();
      xmlhttp.open("POST", PHP_URL, true);
      xmlhttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      xmlhttp.onreadystatechange = function()
      {
        //receive data from server side when it is ready
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200)
        {
          //document.getElementById('print').innerHTML = xmlhttp.responseText;
          jsonObj = JSON.parse(xmlhttp.responseText);
          generateHTML(jsonObj);
        }
      }

      //if user select the radio "here"
      if (document.getElementById('location_option1').checked)
      {
        myLocation = document.getElementById('location_option1').value;
        var ip_parameter = "keyword=" + myKeyword + "&category=" + myCategory + "&distance=" + myDistance + "&location=" + myLocation + "&cur_coordinates=" + coordinatesStr;
        xmlhttp.send(ip_parameter);
      }
      //if user select another radio to input location information manually
      else if (document.getElementById('location_option2').checked)
      {
        myLocation = document.getElementById('input_location').value;
        if (myLocation.length == 0)
        {
          return;
        }
        var address_parameter = "keyword=" + myKeyword + "&category=" + myCategory + "&distance=" + myDistance + "&location=" + myLocation;
        xmlhttp.send(address_parameter);
      }
      document.addEventListener("click", getMouseClickPosition);
      //getPhotoAndReviewInfo();
    }

    /*
     * @function generateHTML(): generate html codes to show the result table
     * @param jsonObj: json object that parsed from server side (Google NearBy Service)
     * @Print show the entire table that covers 20 elements with three attributes
    */
    function generateHTML(jsonObj)
    {
      var MAX_SELECTION = 20;
      googleMapWindow = [];
      if ((jsonObj['status'] == "ZERO_RESULTS") || (jsonObj['results'].length == 0))
      {
        var html_text = "<table align='center' width='650'><tr><td bgcolor='#eaecef'>";
        html_text += "<center>No Records have been found</center>";
        html_text += "</td></tr></table>";
        document.getElementById('print').style.display = "block";
        document.getElementById('print').innerHTML = html_text;
      }
      else
      {
        if (document.getElementById('location_option2').checked)
        {
          inputLocationLat = parseFloat(jsonObj['coordinatesForGoogleMap'][0]);
          inputLocationLng = parseFloat(jsonObj['coordinatesForGoogleMap'][1]);
          //var inputLocationCoordinates = jsonObj['coordinatesForGoogleMap'];
        }

        //table header
        var html_text = "<table align='center'>";
        html_text += "<tr>";
        html_text += "<th><b>Catergory</b></th>";
        html_text += "<th><b>Name</b></th>";
        html_text += "<th><b>Address</b></th>";
        html_text += "</tr>";

        var nameIdArr = new Array(20);
        var placeIdArr = new Array(20);
        //show table elements (first 20 results)
        for (var i = 0; (i < MAX_SELECTION) && (i < jsonObj['results'].length); i++)
        {
          googleMapWindow[i] = false;
          //save coordinates of each search result by globle variables,
          //which will be used on Google Maps API
          var destinationLat = parseFloat(jsonObj['results'][i]['geometry']['location']['lat']);
          var destinationLng = parseFloat(jsonObj['results'][i]['geometry']['location']['lng']);

          html_text += "<tr>";

          //show icons of each category
          var myIcon = jsonObj['results'][i]['icon'];
          html_text += "<td>";
          html_text += "<img src='" + myIcon + "' height='40px' width='40px'>";
          html_text += "</td>";

          //show name of search results
          var myName = jsonObj['results'][i]['name'];
          //console.log(myName);
          //console.log(typeof(myName));
          //var myNameId = myName.replace(/[^A-Z0-9-]+/ig, "_"); //remove special character and space

          //replace spacial characters with html encode
          var htmlEncodingArr = [];
          var nameLength = myName.length;
          while (nameLength --)
          {
            var nameCharacter = myName[nameLength].charCodeAt();
            if ((nameCharacter < 65) || (nameCharacter > 127) || (nameCharacter > 90 && nameCharacter < 97))
            {
              htmlEncodingArr[nameLength] = '&#' + nameCharacter + ';';
            }
            else
            {
              htmlEncodingArr[nameLength] = myName[nameLength];
            }
          }
          var myNameId = htmlEncodingArr.join('');

          nameIdArr.push(myNameId);
          var myPlaceId = jsonObj['results'][i]['place_id'];
          placeIdArr.push(myPlaceId);
          html_text += "<td>";
          html_text += "<a href='javascript:getPhotoAndReviewInfo";
          html_text += "(&quot;" + myPlaceId + "&quot;" + "," + " &quot;" + myNameId + "&quot;);clearMap()'>"
          html_text += myName;
          html_text += "</a>";
          html_text += "</td>";

          //show address of search results
          var myAddress = jsonObj['results'][i]['vicinity'];
          html_text += "<td>";
          //html_text += "<p>" + myAddress + "</p>";
          html_text += "<a href='javascript:generateGoogleMaps(" + destinationLat + "," + destinationLng + "," + i + ");' class='addressLink'>";
          html_text += myAddress;
          html_text += "</a>";
          //html_text += "<div id='googleMap' style='display:none'>";
          html_text += "</td>";

          html_text +="</tr>";
        }
        html_text +="</table>";

        document.getElementById('print').style.display = "block";
        document.getElementById('print').innerHTML = html_text;
      }
    }

    /*
     * @function getCurrentIpLocation(): request the coordiantes of current ip address
     * @return coordiantesArr: the array that store the coordinates pair (latitude and longitude)
    */
    function getCurrentIpLocation()
    {
      IP_URL = "http://ip-api.com/json";
      var ipHttp = new XMLHttpRequest();
      ipHttp.open("GET", IP_URL, false);
      ipHttp.send();
      var ipObj= JSON.parse(ipHttp.responseText);
      var coordinatesArr = getIpGeoLocation(ipObj);
      return coordinatesArr;
    }

    /*
     * @function getIpGeoLocation(): get the coordiantes of current location
     * @param ipObj: json object that parsed from client side (ip-api.com)
     * @return cur_loc: coordinates of current location
    */
    function getIpGeoLocation(ipObj)
    {
      var lat = ipObj.lat;
      var lon = ipObj.lon;
      var coordinatesArr = [];
      coordinatesArr.push(lat);
      coordinatesArr.push(lon);
      return coordinatesArr;
    }


    var firstTimeToOpenMap = true;
    var destination_lat;
    var destination_lng;
    var currentGeoLocation;
    var destinationGeoLocation;
    /*
     * @function initMap(): initialize the Google Map API when click the address at the first time
    */
    function initMap()
    {
      directionsService = new google.maps.DirectionsService();
      directionsDisplay = new google.maps.DirectionsRenderer();

      if (document.getElementById('location_option1').checked)
      {
        //console.log("if");
        currentCoordinates = getCurrentIpLocation();
        var curLat = parseFloat(currentCoordinates[0]);
        var curLng = parseFloat(currentCoordinates[1]);
      }
      else if (document.getElementById('location_option2').checked)
      {
        //console.log("else if");
        var curLat = inputLocationLat;
        var curLng = inputLocationLng;
      }

      currentGeoLocation = {lat: curLat, lng: curLng};
      destinationGeoLocation = {lat: myDestinationLat, lng: myDestinationLng};
      newGoogleMap = new google.maps.Map(document.getElementById('googleMap'),
      {
        zoom: 12,
        center: currentGeoLocation
      });
      newMarker = new google.maps.Marker(
      {
        position: currentGeoLocation,
        map: newGoogleMap
      });
      directionsDisplay.setMap(newGoogleMap);
    }

    /*
     * @function generateGoogleMaps(): set the position and generate the required elements for Google Map API
     * @param desinationLat: latitude value of the target address
     * @param destinationLng: longitude value of the target address
     * @param objectNumber: position number of search result's elements (usually 0-19)
    */
    function generateGoogleMaps(destinationLat, destinationLng, objectNumber)
    {
      API_KEY = "AIzaSyC9HBExGTftsTmeBjHXLucUi5NH2QXCQkY";
      myDestinationLat = destinationLat;
      myDestinationLng = destinationLng;

      document.getElementById('googleMap').style.top = mouseY + "px";
      document.getElementById('googleMap').style.left = mouseX + "px";
      document.getElementById('navigationMenu').style.top = mouseY + "px";
      document.getElementById('navigationMenu').style.left = mouseX + "px";

      if (firstTimeToOpenMap)
      {
        //alert("firstTime- " + objectNumber + ": " + googleMapWindow[objectNumber]);
        var mapSrc = document.createElement("script");
        mapSrc.type = 'text/javascript';
        mapSrc.src = "https://maps.googleapis.com/maps/api/js?key=";
        mapSrc.src += API_KEY;
        mapSrc.src += "&callback=initMap";
        document.body.appendChild(mapSrc);
        firstTimeToOpenMap = false;
        googleMapWindow[objectNumber] = true;
        document.getElementById('googleMap').style.display = "block";
        document.getElementById('navigationMenu').style.display = "block";
      }
      else
      {
        if (googleMapWindow[objectNumber] == true)
        {
          //alert("if- " + objectNumber + ": " + googleMapWindow[objectNumber]);
          googleMapWindow[objectNumber] = false;
          document.getElementById('googleMap').style.display = "none";
          document.getElementById('navigationMenu').style.display = "none";
          document.getElementById('walking').selected = false;
          document.getElementById('bicycling').selected = false;
          document.getElementById('driving').selected = false;
          newMarker.setMap(null);
          if (directionsDisplay != null)
          {
            directionsDisplay.setMap(null);
            directionsDisplay = null;
          }
        }
        else
        {
          //alert("else- " + objectNumber + ": " + googleMapWindow[objectNumber]);
          googleMapWindow[objectNumber] = true;
          document.getElementById('googleMap').style.display = "block";
          document.getElementById('navigationMenu').style.display = "block";
          if (document.getElementById('location_option1').checked)
          {
            //console.log("if");
            currentCoordinates = getCurrentIpLocation();
            var curLat = parseFloat(currentCoordinates[0]);
            var curLng = parseFloat(currentCoordinates[1]);
          }
          else if (document.getElementById('location_option2').checked)
          {
            //console.log("else if");
            var curLat = inputLocationLat;
            var curLng = inputLocationLng;
          }
          currentGeoLocation = {lat: curLat, lng: curLng};
          destinationGeoLocation = {lat: myDestinationLat, lng: myDestinationLng};
          newCenter = new google.maps.LatLng(myDestinationLat, myDestinationLng);
          newGoogleMap = new google.maps.Map(document.getElementById('googleMap'),
          {
            zoom: 12,
            center: newCenter
          });
          newMarker.setMap(null);
          newGoogleMap.panTo(newCenter);
          newMarker = new google.maps.Marker(
          {
            position: newCenter,
            map: newGoogleMap
          });
          newMarker.setMap(newGoogleMap);
          directionsDisplay = new google.maps.DirectionsRenderer;
          directionsService = new google.maps.DirectionsService;
          document.getElementById('walking').selected = false;
          document.getElementById('bicycling').selected = false;
          document.getElementById('driving').selected = false;
          directionsDisplay.setMap(newGoogleMap);
        }
      }
    }

    /*
     * @function calcRoute(): generate direction routes on Google Map API if a user clicks any travel modes
    */
    function calcRoute()
    {
      var start = currentGeoLocation;
      var end = destinationGeoLocation;
      var directionMode = document.getElementById('directionModes').value;
      var request =
      {
        origin: start,
        destination: end,
        travelMode: directionMode
      };
      directionsService.route(request, function(result, status)
      {
        if (status == 'OK')
        {
          directionsDisplay.setDirections(result);
        }
      });
    }

    /*
     * @function clearMap(): clear the Google Map DIV if a user click the name of a search result
     * but forgot to close the Map API
    */
    function clearMap()
    {
      if (document.getElementById('googleMap').style.display == "block")
      {
        document.getElementById('googleMap').style.display = "none";
      }
      if (document.getElementById('navigationMenu').style.display == "block")
      {
        document.getElementById('navigationMenu').style.display = "none";
      }
    }

    /*
     * @function getPhotoAndReviewInfo(): send another request to server to get
     * the JSON string of photo and review information, and parse it to generate the output
    */
    function getPhotoAndReviewInfo(myPlaceId, myNameId)
    {
      //var myPlaceId = "ChIJ7aVxnOTHwoARxKIntFtakKo";
      var photoHttp = new XMLHttpRequest();
      photoHttp.open("POST", PHP_URL, true);
      photoHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      photoHttp.onreadystatechange = function()
      {
        //receive data from server side when it is ready
        if (photoHttp.readyState == 4 && photoHttp.status == 200)
        {
          //document.getElementById('print').innerHTML = photoHttp.responseText;
          photoReview_jsonObj = JSON.parse(photoHttp.responseText);
          generatePhotoAndReviewPage(photoReview_jsonObj, myNameId);
        }
      }
      var photo_parameter = "place_id=" + myPlaceId;
      photoHttp.send(photo_parameter);
    }

    /*
     * @function generatePhotoAndReviewPage(): generate the HTML output of photo and review page
     * @param photoReview_jsonObj: parsed JSON object that has photo and review information
     * @param myNameId: name of current clicking result, which will be showed as header of new page
    */
    function generatePhotoAndReviewPage(photoReview_jsonObj, myNameId)
    {
      //var reviewName = "University of Southern California";
      ARROW_DOWN_SRC = "http://cs-server.usc.edu:45678/hw/hw6/images/arrow_down.png";
      ARROW_UP_SRC = "http://cs-server.usc.edu:45678/hw/hw6/images/arrow_up.png";
      var photosContents = photoReview_jsonObj[0];
      var reviewContents = photoReview_jsonObj[1];
      //var reviewName = myNameId.replace(/_/g, ' ');
      var reviewName = myNameId;
      var html_text = "<div class='PhotoAndReviewPart'>";
      html_text += "<p><center><b>";
      html_text += reviewName;
      html_text += "</b></center></p>";
      html_text += "<br>";

      //show review part
      html_text += "<center><font size='3'><p id='title_reviews'>click to show reviews</p></font></center>";
      html_text += "<center><img src='" + ARROW_DOWN_SRC + "' class='arrow_image' id='arrow_reviews'></center>";
      html_text += "<div id='reviewDiv' style='display:none'>";
      html_text += showReviewOutput(reviewContents);
      html_text += "</div>"; //id = "reviewDetails"

      //show photo part
      html_text += "<center><font size='3'><p id='title_photos'>click to show photos</p></font></center>";
      html_text += "<center><img src='" + ARROW_DOWN_SRC + "' class='arrow_image' id='arrow_photos'></center>";
      html_text += "<div id='photoDiv' style='display:none'>";
      html_text += showPhotoOutput(photosContents);
      html_text += "</div>"; //id = "photoDetails"

      html_text += "</div>" //class = "photoAndReviewPart"

      document.getElementById('print').innerHTML = html_text;


      reviewState = false; //show -> true; not show -> false
      photoState = false; //show -> true; not show -> false
      //add click event for review DIV
      document.getElementById('arrow_reviews').addEventListener("click", function()
      {
        if ((reviewState == false) && (photoState == false))
        {
          document.getElementById('reviewDiv').style.display = "block";
          document.getElementById('title_reviews').innerHTML = "click to hide reviews";
          document.getElementById('arrow_reviews').src = ARROW_UP_SRC;
          reviewState = true;
        }
        else if ((reviewState == false) && (photoState == true))
        {
          document.getElementById('reviewDiv').style.display = "block";
          document.getElementById('title_reviews').innerHTML = "click to hide reviews";
          document.getElementById('arrow_reviews').src = ARROW_UP_SRC;
          reviewState = true;
          document.getElementById('photoDiv').style.display = "none";
          document.getElementById('title_photos').innerHTML = "click to show reviews";
          document.getElementById('arrow_photos').src = ARROW_DOWN_SRC;
          photoState = false;
        }
        else if ((reviewState == true) && (photoState == false))
        {
          document.getElementById('reviewDiv').style.display = "none";
          document.getElementById('title_reviews').innerHTML = "click to show reviews";
          document.getElementById('arrow_reviews').src = ARROW_DOWN_SRC;
          reviewState = false;
        }
      });

      //add click event for photo DIV
      document.getElementById('arrow_photos').addEventListener("click", function()
      {
        if ((reviewState == false) && (photoState == false))
        {
          document.getElementById('photoDiv').style.display = "block";
          document.getElementById('title_photos').innerHTML = "click to hide reviews";
          document.getElementById('arrow_photos').src = ARROW_UP_SRC;
          photoState = true;
        }
        else if ((reviewState == false) && (photoState == true))
        {
          document.getElementById('photoDiv').style.display = "none";
          document.getElementById('title_photos').innerHTML = "click to show reviews";
          document.getElementById('arrow_photos').src = ARROW_DOWN_SRC;
          photoState = false;
        }
        else if ((reviewState == true) && (photoState == false))
        {
          document.getElementById('reviewDiv').style.display = "none";
          document.getElementById('title_reviews').innerHTML = "click to show reviews";
          document.getElementById('arrow_reviews').src = ARROW_DOWN_SRC;
          reviewState = false;
          document.getElementById('photoDiv').style.display = "block";
          document.getElementById('title_photos').innerHTML = "click to hide reviews";
          document.getElementById('arrow_photos').src = ARROW_UP_SRC;
          photoState = true;
        }
      });
    }

    /*
     * @function showReviewOutput(): generate the HTML output of review DIV
     * @param reviewContents: subarray of review's part from parsed JSON object
    */
    function showReviewOutput(reviewContents)
    {
      var html_text = "<table class='reviewsTable' align='center'>";
      if (reviewContents.length == 0)
      {
        html_text += "<tr><td><center><b>No Reviews Found</b></center></td></tr>";
      }
      else
      {
        var myName;
        var myPhoto;
        var myReview;
        for (var i = 0; i < reviewContents.length; i++)
        {
          myName = reviewContents[i]['myName'];
          myPhoto = reviewContents[i]['myPhoto'];
          html_text += "<tr><td><center>";
          //if profile photo does not exist
          if (myPhoto == "noPhotoFinds")
          {
            html_text += "<b>" + myName + "</b>";
          }
          else
          {
            html_text += "<img src='" + myPhoto + "' class='reviewerProfilePhoto'>";
            html_text += "<b>" + myName + "</b>";
          }
          html_text += "</center></td></tr>";

          myReview = reviewContents[i]['myReview'];
          if (myReview.length != 0)
          {
            html_text += "<tr><td>";
            html_text += myReview;
            html_text += "</td></tr>";
          }
          else
          {
            html_text += "<tr><td><p></p></td></tr>";
          }
        }
      }
      html_text += "</table>";
      return html_text;
    }

    /*
     * @function showPhotoOutput(): generate the HTML output of photo DIV
     * @param photoContents: subarray of photo's part from parsed JSON object
    */
    function showPhotoOutput(photosContents)
    {
      var html_text = "<table class='photosTable' align='center'>";
      if (photosContents.length == 0)
      {
        html_text += "<tr><td><center><b>No Photos Found</b></center></td></tr>";
      }
      else
      {
        var googlePhoto;
        for (var i = 0; i < photosContents.length; i++)
        {
          googlePhoto = photosContents[i];
          html_text += "<tr><td><center>";
          html_text += "<a href='/" + googlePhoto + "' target='_blank'>";
          html_text += "<img src='/" + googlePhoto + "' class='googlePhotos'></a>"
          html_text += "</center></td></tr>";
        }
      }
      html_text += "</table>";
      return html_text;
    }

    /*
     * @function displayHiddenPart(): reverse the display value of review and photo DIV
     * @param styleId: review DIV or photo DIV
    */
    function displayHiddenPart(styleId)
    {
      var displayPart = document.getElementById(styleId);
      if (displayPart.style.display === "none")
      {
        displayPart.style.display = "block";
      }
      else
      {
        displayPart.style.display = "none";
      }
    }

    /*
     * @function clearInput(): set up all functionalities of clear button
    */
    function clearInput()
    {
      document.getElementById('input_keyword').value = "";
      document.getElementById('input_category').value = "default";
      document.getElementById('input_distance').value = "";
      document.getElementById('location_option1').checked = true;
      document.getElementById('input_location').value = "";
      document.getElementById('input_location').disabled = true;
      document.getElementById('print').innerHTML = "";
      document.getElementById('print').style.display = "none";
      document.getElementById('navigationMenu').style.display = "none";
      document.getElementById('googleMap').style.display = "none";
    }
    </script>
  </head>

  <body>
    <div class = "input_box">
      <form id="place_form" method="POST" onsubmit="return false;">
        <h1><i><center>Travel and Entertainment Search</center></i></h1>
        <hr width="600">
        <h4>Keyword <input type="text" name="keyword" id="input_keyword" onclick="validateForm()" required="required"></h4>
        <h4>Category
          <select name="category" id="input_category" required="required">
            <option value="default">default</option>
            <option value="cafe">cafe</option>
            <option value="bakery">bakery</option>
            <option value="restaurant">restaurant</option>
            <option value="beauty_salon">beauty salon</option>
            <option value="casino">casino</option>
            <option value="movie_theater">movie theater</option>
            <option value="lodging">lodging</option>
            <option value="airport">airport</option>
            <option value="train_station">train station</option>
            <option value="subway_station">subway station</option>
            <option value="bus_station">bus station</option>
          </select>
        </h4>
        <h4>Distance (miles) <input type="text" name="distance" id="input_distance" placeholder="10" pattern="[0-9]+">
            from <input type="radio" name="location" id="location_option1" onclick="validateForm()" checked="checked" value="current_location"> Here
        </h4>
        <input type="radio" name="location" id="location_option2" onclick="validateForm()" style="margin-left:288">
        <input type="text" name="location" id="input_location" required="required" placeholder="location">
        <p> </p>
        <button type="submit" name="search" id='searchButton' onclick="getInputs();validateForm();clearMap();" disabled style="margin-left:66">Search</button>
        <button type="button" name="clear" id='clearButton' onclick="clearInput()">Clear</button>
        <p> </p>
      </form>
      <br><br>
    </div>
    <div id="print"></div>
    <div id="googleMap"></div>
    <div id="navigationMenu">
      <select size="3" id="directionModes" onchange="calcRoute()">
        <option value="WALKING" id="walking">Walk there</option>
        <option value="BICYCLING" id="bicycling">Ride there</option>
        <option value="DRIVING" id="driving">Drive there</option>
      </select>
    </div>
  </body>
</html>
