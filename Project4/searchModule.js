
(function(angular)
{
  var searchApp = angular.module('travelSearchMvc', [
    'ngRoute',
    'travelSearchMvc.resultsModule',
    'travelSearchMvc.detailsModule',
    'travelSearchMvc.favoritesModule',
    'travelSearchMvc.favoriteDetailsModule'
  ]);

  searchApp.config(['$qProvider', function ($qProvider)
  {
    $qProvider.errorOnUnhandledRejections(false);
  }]);

  searchApp.controller('searchController', ['$scope', '$http', '$log', '$location', '$rootScope', function($scope, $http, $log, $location, $rootScope)
  {
    $rootScope.ifSlide = false;
    $rootScope.ifClickedFavoriteDetails = false;
    $rootScope.slideAnimation = false;
    $scope.myCategory = "default";
    $scope.clickedSearch = false;
    $scope.clickedResults = true;
    $rootScope.tempFavoriteRow = [];
    if ($location.path() === '/favorites_page')
    {
      $scope.clickedResults = false;
      $scope.clickedFavorite = true;
    }
    $scope.ifSearchSuccess = true;
    $rootScope.ifClickedDetails = true;
    $rootScope.ifClickedFavoriteDetails = true;

    $scope.validateForm = function()
    {
      if (document.getElementById('location_option1').checked)
      {
        document.getElementById('input_location').disabled = true;
        $scope.myInputLocation = "";
      }
      if (document.getElementById('location_option2').checked)
      {
        document.getElementById('input_location').disabled = false;
      }
    };

    $scope.checkDisableCondition = function()
    {
      if ($scope.myLocation === 1)
      {
        $scope.myForm.inputLocation.$setPristine();
        $scope.myForm.inputLocation.$setUntouched();
        if ($scope.myForm.keyword.$invalid)
        {
          return true;
        }
      }
      else if ($scope.myLocation === 2)
      {
        if ($scope.myForm.keyword.$invalid || $scope.myForm.inputLocation.$invalid)
        {
          return true;
        }
      }
    };

    $scope.clearInputs = function()
    {
      $rootScope.ifSlide = false;
      $location.path('/');
      $scope.myForm.$setPristine();
      $scope.myForm.$setUntouched();
      $scope.myForm.$submitted = false;
      $scope.myKeyword = "";
      $scope.myCategory = "default";
      $scope.myDistance = "";
      $scope.myLocation = 1;
      $scope.myInputLocation = "";
      document.getElementById('input_location').disabled = true;
      $scope.showTable = false;
      $scope.clickedResults = true;
      $scope.clickedFavorite = false;
      $rootScope.ifClickedDetails = true;
      $rootScope.ifClickedFavoriteDetails = true;
    };

    $scope.getInputs = function(myPath)
    {
      $rootScope.ifSlide = false;
      $location.path('/');
      $scope.showTable = true;
      $scope.clickedSearch = true;
      $rootScope.showProgressBar = true;
      $scope.clickedResults = true;
      $scope.clickedFavorite = false;
      $scope.ifSearchSuccess = true;
      $rootScope.ifClickedDetails = true;
      $rootScope.ifClickedFavoriteDetails = true;

      var inputData;
      if (typeof $scope.myDistance === 'undefined' || $scope.myDistance == "")
      {
        $scope.myDistance = 10;
      }

      if ($scope.myLocation === 1)
      {
        $scope.locationOption = "option1";
        $http({
          method: 'GET',
          url: 'http://ip-api.com/json'
        })
        .then (function (response)
        {
          $scope.currentlat = response.data.lat;
          $scope.currentlng = response.data.lon;
          var url_params = "http://cs571placesearch-env.us-east-2.elasticbeanstalk.com/?"
          url_params += "category=" + $scope.myCategory + "&distance=" + $scope.myDistance +
          "&keyword=" + $scope.myKeyword + "&latitude=" + $scope.currentlat + "&longitude=" + $scope.currentlng;
          console.log(url_params);

          $http({
            method: 'GET',
            url: url_params,
          })
          .then (function (response)
          {
            $scope.jsonObj = response.data;
            //console.log($scope.jsonObj);
            $rootScope.showProgressBar = false;
            $rootScope.ifSlide = false;
            $location.path(myPath);
          },
          function(response)
          {
            console.error("Request error!");
            $rootScope.showProgressBar = false;
            $scope.ifSearchSuccess = false;
          });
        },
        function(response)
        {
          console.error("Request error!");
        });
      }
      else if ($scope.myLocation === 2)
      {
        $scope.locationOption = "option2";
        //console.log($scope.autocompleteObj);
        if (typeof $scope.autocompleteObj.getPlace() !== 'undefined')
        {
          $scope.myInputLocation = $scope.autocompleteObj.getPlace().formatted_address;
        }

        var url_params = "http://cs571placesearch-env.us-east-2.elasticbeanstalk.com/?"
        url_params += "category=" + $scope.myCategory + "&distance=" + $scope.myDistance +
        "&keyword=" + $scope.myKeyword + "&location=" + $scope.myInputLocation;
        console.log(url_params);

        $http({
          method: 'GET',
          url: url_params,
        })
        .then (function (response)
        {
          $scope.jsonObj = response.data;
          //console.log($scope.jsonObj);
          $scope.myLat = $scope.jsonObj.myLat;
          $scope.myLng = $scope.jsonObj.myLng;
          $rootScope.showProgressBar = false;
          $scope.ifSearchSuccess = true;
          $rootScope.slideAnimation = true;
          $rootScope.ifSlide = false;
          $location.path(myPath);
        },
        function(response)
        {
          console.error("Request error!");
        });
      }
    };

    $scope.ifClickedSearch = function()
    {
      $rootScope.ifSlide = false;
      if ($scope.clickedSearch === true)
      {
        $scope.clickedResults = true;
        $scope.clickedFavorite = false;
        $location.path('/results_page');
      }
      else
      {
        if ($scope.clickedFavorite !== true)
        {
          alert("Please click Search button to get results table before clicking Resluts Tab!");
        }
        else
        {
          $scope.clickedResults = false;
          $scope.clickedFavorite = true;
          alert("Please click Search button to get results table before clicking Resluts Tab!");
        }
      }
    }

    $scope.ifClickFavorite = function()
    {
      $scope.clickedFavorite = true;
      $scope.clickedResults = false;
      $rootScope.ifSlide = false;
      $location.path('favorites_page');
    }

    $scope.redirect = function(myPath)
    {
      $location.path(myPath);
    };

    $scope.autoComplete = function()
    {
      var input = document.getElementById('input_location');
      var options = {types: ['address']};
      $scope.autocompleteObj = new google.maps.places.Autocomplete(input, options);
    };

    $scope.cleanAnimation = function()
    {
      $rootScope.ifSlide = false;
    }

  }]);
})(angular);
