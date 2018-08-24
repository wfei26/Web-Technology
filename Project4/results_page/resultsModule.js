
(function(angular)
{
  var resultsModule = angular.module('travelSearchMvc.resultsModule', ['ngRoute']);
  resultsModule.config(['$routeProvider', function($routeProvider)
  {
    $routeProvider.when('/results_page', {
      templateUrl: 'results_page/resultsView.html',
      controller: 'resultsController'
    });
  }]);

  resultsModule.service('resultsDataService', function()
  {
    this.setData = function(val)
    {
      this.myData = val;
    };
    this.getData = function()
    {
      return this.myData;
    };
  });

  resultsModule.controller('resultsController', ['$scope', '$http', '$rootScope', '$location', 'resultsDataService', '$q', function($scope, $http, $rootScope, $location, resultsDataService, $q)
  {
    $rootScope.ifSlide = false;
    $rootScope.moveToRight = true;
    $scope.myLocationOption = $scope.$parent.locationOption;
    $scope.showResultsTable = $scope.$parent.showTable;
    $scope.myStorage = window.localStorage;
    var storageKey;

    if (typeof resultsDataService.getData() !== 'undefined' && resultsDataService.getData()[1][0] === $scope.$parent.jsonObj)
    {
      $scope.ifHasTable = true;
      $rootScope.currentPage = resultsDataService.getData()[0];
      $rootScope.jsonData = resultsDataService.getData()[1];
      for (var i = 0; i < $rootScope.jsonData.length; i++)
      {
        for (j = 0; j < $rootScope.jsonData[0]['results'].length; j++)
        {
          if (typeof $rootScope.jsonData[i] !== 'undefined')
          {
            $rootScope.jsonData[i]['results'][j]['ifHighlight'] = false;
          }
        }
      }
      for (var i = 0; i < $rootScope.jsonData.length; i++)
      {
        for (j = 0; j < $rootScope.jsonData[0]['results'].length; j++)
        {
          if ($rootScope.jsonData[i]['results'][j]['place_id'] === $rootScope.savedKey)
          {
            $rootScope.jsonData[i]['results'][j]['ifHighlight'] = true;
          }
        }
      }
      if ($rootScope.currentPage === 1)
      {
        $scope.rowData = $rootScope.jsonData[0]['results'];
        $scope.showNext = true;
        $scope.showPrevious = false;
        $scope.rowData1 = $rootScope.jsonData[0]['results'];
        if (typeof $rootScope.jsonData[1] !== 'undefined')
        {
          $scope.rowData3 = $rootScope.jsonData[1]['results'];
        }
        if (typeof $rootScope.jsonData[2] !== 'undefined')
        {
          $scope.rowData3 = $rootScope.jsonData[2]['results'];
        }
      }
      else if ($rootScope.currentPage === 2)
      {
        $scope.rowData = $rootScope.jsonData[1]['results'];
        $scope.showNext = true;
        $scope.showPrevious = true;
        $scope.rowData1 = $rootScope.jsonData[0]['results'];
        $scope.rowData2 = $rootScope.jsonData[1]['results'];
        if (typeof $rootScope.jsonData[2] !== 'undefined')
        {
          $scope.rowData3 = $rootScope.jsonData[2]['results'];
        }
      }
      else if ($rootScope.currentPage === 3)
      {
        $scope.rowData = $rootScope.jsonData[2]['results'];
        $scope.showNext = false;
        $scope.showPrevious = true;
        $scope.rowData1 = $rootScope.jsonData[0]['results'];
        $scope.rowData2 = $rootScope.jsonData[1]['results'];
        $scope.rowData3 = $rootScope.jsonData[2]['results'];
      }
    }
    else
    {
      $rootScope.jsonData = [];
      $rootScope.jsonData[0] = $scope.$parent.jsonObj;
      $rootScope.currentPage = 1;

      if ($rootScope.jsonData[0].status === "ZERO_RESULTS")
      {
        $scope.ifHasTable = false;
      }
      else
      {
        $scope.ifHasTable = true;
        if ($rootScope.jsonData[0].hasOwnProperty('next_page_token'))
        {
          $scope.showNext = true;
        }
        else
        {
          $scope.showNext = false;
        }
        $scope.showPrevious = false;
        //console.log($rootScope.jsonData);
        $scope.rowData = $rootScope.jsonData[0]['results'];
        for (var i = 0; i < $scope.rowData.length; i++)
        {
          if ($scope.rowData[i]['ifSaved'] !== true)
          {
            $scope.rowData[i]['starClass'] = "glyphicon glyphicon-star-empty";
          }
        }
        for (var k = 0; k < $scope.myStorage.length; k++)
        {
          storageKey = $scope.myStorage.key(k);
          for (var i = 0; i < $scope.rowData.length; i++)
          {
            if ($scope.rowData[i]['place_id'] === storageKey)
            {
              $scope.rowData[i]['starClass'] = "glyphicon glyphicon-star";
            }
          }
        }
        //console.log($scope.rowData);
        $scope.rowData1 = $scope.rowData;
      }
    }

    $scope.getNextPageData = function()
    {
      if ($rootScope.currentPage === 1)
      {
        if (typeof $scope.rowData2 === 'undefined')
        {
          if ($rootScope.jsonData[0].hasOwnProperty('next_page_token'))
          {
            // console.log("jsonData[0]: ")
            // console.log($rootScope.jsonData[0]);
            $rootScope.currentPage++;
            $scope.showPrevious = true;
            $scope.nextPageToken1 = $rootScope.jsonData[0].next_page_token;
            var dataToPass = {
              nextPageToken: $scope.nextPageToken1
            }

            $http({
              method: 'GET',
              url: 'http://travelsearchnodejs-env.us-east-2.elasticbeanstalk.com/',
              params: dataToPass
            })
            .then (function (response)
            {
              $rootScope.jsonData[1] = response.data;
              if ($rootScope.jsonData[1].hasOwnProperty('next_page_token'))
              {
                $scope.showNext = true;
              }
              else
              {
                $scope.showNext = false;
              }
              // console.log("jsonData[1]: ")
              // console.log($rootScope.jsonData[1]);
              $scope.rowData = $rootScope.jsonData[1]['results'];
              for (var i = 0; i < $scope.rowData.length; i++)
              {
                if ($scope.rowData[i]['ifSaved'] !== true)
                {
                  $scope.rowData[i]['starClass'] = "glyphicon glyphicon-star-empty";
                }
              }
              for (var k = 0; k < $scope.myStorage.length; k++)
              {
                storageKey = $scope.myStorage.key(k);
                for (var i = 0; i < $scope.rowData.length; i++)
                {
                  if ($scope.rowData[i]['place_id'] === storageKey)
                  {
                    $scope.rowData[i]['starClass'] = "glyphicon glyphicon-star";
                  }
                }
              }
              $scope.rowData2 = $scope.rowData;
              //console.log($scope.rowData2);
            },
            function(response)
            {
              console.error("Request error!");
            });
          }
          else
          {
            $scope.showNext = false;
          }
        }
        else
        {
          $rootScope.currentPage++;
          if ($rootScope.jsonData[1].hasOwnProperty('next_page_token'))
          {
            $scope.showNext = true;
          }
          else
          {
            $scope.showNext = false;
          }
          $scope.showPrevious = true;
          $scope.rowData = $scope.rowData2;
        }
      }

      else if ($rootScope.currentPage === 2)
      {
        if (typeof $scope.rowData3 === 'undefined')
        {
          if ($rootScope.jsonData[1].hasOwnProperty('next_page_token'))
          {
            $rootScope.currentPage++;
            $scope.showNext = false;
            $scope.showPrevious = true;
            $scope.nextPageToken2 = $rootScope.jsonData[1].next_page_token;
            var dataToPass = {
              nextPageToken: $scope.nextPageToken2
            }

            $http({
              method: 'GET',
              url: 'http://travelsearchnodejs-env.us-east-2.elasticbeanstalk.com/',
              params: dataToPass
            })
            .then (function (response)
            {
              $rootScope.jsonData[2] = response.data;
              // console.log("jsonData[2]: ")
              // console.log($rootScope.jsonData[2]);
              $scope.rowData = $rootScope.jsonData[2]['results'];
              for (var i = 0; i < $scope.rowData.length; i++)
              {
                if ($scope.rowData[i]['ifSaved'] !== true)
                {
                  $scope.rowData[i]['starClass'] = "glyphicon glyphicon-star-empty";
                }
              }
              for (var k = 0; k < $scope.myStorage.length; k++)
              {
                storageKey = $scope.myStorage.key(k);
                for (var i = 0; i < $scope.rowData.length; i++)
                {
                  if ($scope.rowData[i]['place_id'] === storageKey)
                  {
                    $scope.rowData[i]['starClass'] = "glyphicon glyphicon-star";
                  }
                }
              }
              $scope.rowData3 = $scope.rowData;
            },
            function(response)
            {
              console.error("Request error!");
            });
          }
          else
          {
            $scope.showNext = false;
          }
        }
        else
        {
          $rootScope.currentPage++;
          $scope.showNext = false;
          $scope.showPrevious = true;
          $scope.rowData = $scope.rowData3;
        }
      }

      else
      {
        $scope.showNext = false;
        $scope.showPrevious = true;
      }
    };

    $scope.getPreviousPage = function()
    {
      if ($rootScope.currentPage === 2)
      {
        $rootScope.currentPage--;
        $scope.showNext = true;
        $scope.showPrevious = false;
        $scope.rowData = $scope.rowData1;
      }
      else if ($rootScope.currentPage === 3)
      {
        $rootScope.currentPage--;
        $scope.showNext = true;
        $scope.showPrevious = true;
        $scope.rowData = $scope.rowData2;

      }
      else
      {
        $scope.showNext = true;
        $scope.showPrevious = false;
      }
    };

    $scope.requestDetails = function(index)
    {
      $rootScope.ifSlide = true;
      $rootScope.moveToRight = true;
      $rootScope.ifClickedFavoriteDetails = false;
      $rootScope.showProgressBar = true;
      //console.log($rootScope.currentPage);
      $scope.myPlaceId = $scope.rowData[index]['place_id'];
      var map = new google.maps.Map(document.createElement('div'));
      service = new google.maps.places.PlacesService(map);

      var requestDetailsData = function()
      {
        var deferred = $q.defer();
        service.getDetails
        ({
          'placeId': $scope.myPlaceId
        },
        function(param)
        {
          deferred.resolve(param);
        });
        return deferred.promise;
      };

      requestDetailsData()
      .then(function(response)
      {
        $scope.passData = [];
        $scope.placeDetails = response;
        //console.log($scope.placeDetails);
        $scope.passData[0] = $scope.placeDetails;

        $scope.photoObj = $scope.placeDetails.photos;
        if (typeof $scope.photoObj !== 'undefined')
        {
          $scope.photoArr = [];
          for (var i = 0; i < $scope.photoObj.length; i++)
          {
            var max_height = $scope.photoObj[i].height;
            var max_width = $scope.photoObj[i].width;
            var photoUrl = $scope.photoObj[i].getUrl({'maxWidth': max_width, 'maxHeight': max_height});
            $scope.photoArr[i] = photoUrl;
            $scope.passData[1] = $scope.photoArr;
          }
          //console.log($scope.photoArr);
        }
        $rootScope.curRowData = $scope.rowData[index];
        $scope.passData[2] = $rootScope.currentPage;
        $scope.passData[3] = $rootScope.jsonData;
        //console.log($rootScope.curRowData);
        resultsDataService.setData($scope.passData);
        $rootScope.currentIndex = index;
        //console.log($scope.passData);
        $rootScope.ifClickedDetails = false;
        $rootScope.showProgressBar = false;
        $rootScope.savedKey = $scope.placeDetails.place_id;
        $location.path('/details_page');
        for (var i = 0; i < $rootScope.jsonData.length; i++)
        {
          for (j = 0; j < $rootScope.jsonData[0]['results'].length; j++)
          {
            if (typeof $rootScope.jsonData[i] !== 'undefined')
            {
              $rootScope.jsonData[i]['results'][j]['ifHighlight'] = false;
            }
          }
        }
        for (var i = 0; i < $rootScope.jsonData.length; i++)
        {
          for (j = 0; j < $rootScope.jsonData[0]['results'].length; j++)
          {
            if ($rootScope.jsonData[i]['results'][j]['place_id'] === $rootScope.savedKey)
            {
              $rootScope.jsonData[i]['results'][j]['ifHighlight'] = true;
            }
          }
        }
      });
    };

    $scope.redirect = function(myPath)
    {
      $location.path(myPath);
    };

    $scope.saveToLocalStorage = function(index)
    {
      if ($scope.rowData[index]['starClass'] === "glyphicon glyphicon-star-empty")
      {
        $scope.rowData[index]['starClass'] = "glyphicon glyphicon-star";
        $scope.rowData[index]['ifSaved'] = true;
        if ($rootScope.currentPage === 1)
        {
          $rootScope.jsonData[0]['results'][index]['ifSaved'] = true;
          $rootScope.jsonData[0]['results'][index]['starClass'] = "glyphicon glyphicon-star";
        }
        else if ($rootScope.currentPage === 2)
        {
          $rootScope.jsonData[1]['results'][index]['ifSaved'] = true;
          $rootScope.jsonData[1]['results'][index]['starClass'] = "glyphicon glyphicon-star";
        }
        else if ($rootScope.currentPage === 3)
        {
          $rootScope.jsonData[2]['results'][index]['ifSaved'] = true;
          $rootScope.jsonData[2]['results'][index]['starClass'] = "glyphicon glyphicon-star";
        }
        $scope.myPlaceId = $scope.rowData[index]['place_id'];
        var map = new google.maps.Map(document.createElement('div'));
        service = new google.maps.places.PlacesService(map);

        var requestDetailsData = function()
        {
          var deferred = $q.defer();
          service.getDetails
          ({
            'placeId': $scope.myPlaceId
          },
          function(param)
          {
            deferred.resolve(param);
          });
          return deferred.promise;
        };

        requestDetailsData()
        .then(function(response)
        {
          $scope.passData = [];
          $scope.placeDetails = response;
          //console.log($scope.placeDetails);
          $scope.passData[0] = $scope.placeDetails;

          $scope.photoObj = $scope.placeDetails.photos;
          if (typeof $scope.photoObj !== 'undefined')
          {
            $scope.photoArr = [];
            for (var i = 0; i < $scope.photoObj.length; i++)
            {
              var max_height = $scope.photoObj[i].height;
              var max_width = $scope.photoObj[i].width;
              var photoUrl = $scope.photoObj[i].getUrl({'maxWidth': max_width, 'maxHeight': max_height});
              $scope.photoArr[i] = photoUrl;
              $scope.passData[1] = $scope.photoArr;
            }
          }

          $scope.passData[2] = $scope.rowData[index];
          $scope.passData[3] = $scope.myLocationOption;

          if ($scope.myLocationOption === "option1")
          {
            $scope.currentLocation_lat = $scope.$parent.currentlat;
            $scope.currentLocation_lng = $scope.$parent.currentlng;
            $scope.startGeoLocation = {lat: $scope.currentLocation_lat, lng: $scope.currentLocation_lng};
            $scope.passData[4] = $scope.startGeoLocation;
          }
          else
          {
            $scope.myInputLocation = $scope.$parent.myInputLocation;
            $scope.passData[4] = $scope.myInputLocation;
          }

          var timeStamp = Date.now();
          $scope.passData[5] = timeStamp;
          //console.log($scope.passData);
          if(typeof(Storage) !== "undefined")
          {
            var key = $scope.myPlaceId;
            localStorage.setItem(key, JSON.stringify($scope.passData));
          }
          else
          {
            console.log("Sorry, your browser does not support web storage...");
          }
        });
      }

      else
      {
        $scope.rowData[index]['starClass'] = "glyphicon glyphicon-star-empty";
        if ($rootScope.currentPage === 1)
        {
          $rootScope.jsonData[0]['results'][index]['ifSaved'] = false;
          $rootScope.jsonData[0]['results'][index]['starClass'] = "glyphicon glyphicon-star-empty";
        }
        else if ($rootScope.currentPage === 2)
        {
          $rootScope.jsonData[1]['results'][index]['ifSaved'] = false;
          $rootScope.jsonData[1]['results'][index]['starClass'] = "glyphicon glyphicon-star-empty";
        }
        else if ($rootScope.currentPage === 3)
        {
          $rootScope.jsonData[2]['results'][index]['ifSaved'] = false;
          $rootScope.jsonData[2]['results'][index]['starClass'] = "glyphicon glyphicon-star-empty";
        }
        $scope.myPlaceId = $scope.rowData[index]['place_id'];
        $rootScope.detailStarClass = "glyphicon glyphicon-star-empty";
        localStorage.removeItem($scope.myPlaceId);
      }
    };

    $scope.redirectDetailsPage = function()
    {
      $rootScope.ifSlide = true;

      $location.path('/details_page');
    };

    $scope.changeAnimation = function()
    {

    }

  }]);
})(angular);
