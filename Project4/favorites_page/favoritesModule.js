
(function(angular)
{
  var favoritesModule = angular.module('travelSearchMvc.favoritesModule', ['ngRoute']);
  favoritesModule.config(['$routeProvider', function($routeProvider)
  {
    $routeProvider.when('/favorites_page', {
      templateUrl: 'favorites_page/favoritesView.html',
      controller: 'favoritesController'
    });
  }]);

  favoritesModule.service('favoriteDataService', function()
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

  favoritesModule.controller('favoritesController', ['$scope', '$http', '$rootScope', '$location', 'favoriteDataService', '$q', function($scope, $http, $rootScope, $location, favoriteDataService, $q)
  {
    //localStorage.clear();
    $rootScope.moveToRight = true;
    $scope.myStorage = window.localStorage;
    $scope.sortedStorage = [];
    var tempKey;
    var tempArr;
    for (var i = 0; i < $scope.myStorage.length; i++)
    {
      tempKey = $scope.myStorage.key(i);
      tempArr = JSON.parse(localStorage.getItem(tempKey));
      tempArr[6] = tempKey;
      $scope.sortedStorage.push(tempArr);
    }

    $scope.sortedStorage.sort(function(x, y)
    {
      return x[5] - y[5];
    })
    //console.log($scope.sortedStorage);

    $scope.showPrevious = true;
    $scope.showNext = true;
    if ($scope.myStorage.length === 0)
    {
      $scope.ifHasFavoriteItems = false;
    }
    else
    {
      $scope.ifHasFavoriteItems = true;
    }

    $rootScope.favoriteRows = [];
    for (var i = 0; i < $scope.sortedStorage.length; i++)
    {
      $rootScope.favoriteRows.push($scope.sortedStorage[i][2]);
    }
    for (var i = 0; i < $rootScope.favoriteRows.length; i++)
    {
      $rootScope.favoriteRows[i]['ifHighlight'] = false;
    }
    for (var i = 0; i < $rootScope.favoriteRows.length; i++)
    {
      if ($rootScope.favoriteRows[i]['place_id'] === $rootScope.savedKey)
      {
        //console.log($rootScope.favoriteRows[i]);
        $rootScope.favoriteRows[i]['ifHighlight'] = true;
      }
    }

    $scope.favoriteRowData = [];
    for (var i = 0; i < $rootScope.favoriteRows.length && i < 20; i++)
    {
      $scope.favoriteRowData[i] = $rootScope.favoriteRows[i];
    }

    $rootScope.allFavoriteData = [];
    $rootScope.favoriteCurrentPage = 1;
    $scope.arrangePages = function()
    {
      $rootScope.totalFavoritePage = ~~($rootScope.favoriteRows.length / 20) + 1;

      for (var i = 0; i < $rootScope.totalFavoritePage; i++)
      {
        $rootScope.allFavoriteData[i] = [];
        for (var j = i * 20; j < 20 * (i+1); j++)
        {
          if (typeof $rootScope.favoriteRows[j] !== 'undefined')
          {
            $rootScope.allFavoriteData[i].push($rootScope.favoriteRows[j]);
          }
        }
      }
    }

    $scope.arrangePages();
    //console.log($rootScope.allFavoriteData);
    $scope.numOfFavorite = ($rootScope.totalFavoritePage-1) * 20 + $rootScope.allFavoriteData[$rootScope.totalFavoritePage-1].length;
    if ($rootScope.favoriteCurrentPage === 1)
    {
      if ($scope.numOfFavorite <= 20)
      {
        $scope.showPrevious = false;
        $scope.showNext = false;
      }
      else
      {
        $scope.showPrevious = false;
        $scope.showNext = true;
      }
    }

    $scope.getNextPage = function()
    {
      if ($rootScope.favoriteCurrentPage === 1)
      {
        if ($scope.numOfFavorite <= 20)
        {
          $scope.showPrevious = false;
          $scope.showNext = false;
        }
        else
        {
          $scope.showPrevious = true;
          $scope.showNext = true;
        }
        $scope.favoriteRowData = $rootScope.allFavoriteData[$rootScope.favoriteCurrentPage];
        $rootScope.favoriteCurrentPage++;
        if ($rootScope.favoriteCurrentPage === $rootScope.totalFavoritePage)
        {
          $scope.showPrevious = true;
          $scope.showNext = false;
        }
      }
      else
      {
        $scope.showPrevious = true;
        $scope.showNext = true;
        $scope.favoriteRowData = $rootScope.allFavoriteData[$rootScope.favoriteCurrentPage];
        $rootScope.favoriteCurrentPage++;
        if ($rootScope.favoriteCurrentPage === $rootScope.totalFavoritePage)
        {
          $scope.showPrevious = true;
          $scope.showNext = false;
        }
      }
    }

    $scope.getPreviousPage = function()
    {
      if ($rootScope.favoriteCurrentPage === 2)
      {
        $scope.showPrevious = false;
        $scope.showNext = true;
      }
      else
      {
        $scope.showPrevious = true;
        $scope.showNext = true;
      }
      $rootScope.favoriteCurrentPage--;
      $scope.favoriteRowData = $rootScope.allFavoriteData[$rootScope.favoriteCurrentPage-1];
    }

    $scope.removeLocalStorage = function(index)
    {
      var deleteIndex = ($rootScope.favoriteCurrentPage-1)*20 + index;
      var deleteKey = $rootScope.favoriteRows[deleteIndex].place_id;
      $rootScope.favoriteRows.splice(deleteIndex, 1);
      $scope.arrangePages();
      $scope.favoriteRowData = $rootScope.allFavoriteData[$rootScope.favoriteCurrentPage-1];

      if (index === 0 && $rootScope.favoriteCurrentPage !== 1
        && ($rootScope.allFavoriteData[[$rootScope.favoriteCurrentPage-1]].length === 0))
      {
        $scope.getPreviousPage();
      }
      if ($rootScope.favoriteRows.length === 0)
      {
        $scope.ifHasFavoriteItems = false;
      }
      else
      {
        $scope.ifHasFavoriteItems = true;
      }

      if (typeof $rootScope.jsonData !== 'undefined')
      {
        for (var i = 0; i < $rootScope.jsonData.length; i++)
        {
          for (var j = 0; j < $rootScope.jsonData[i]['results'].length; j++)
          {
            if ($rootScope.jsonData[i]['results'][j]['place_id'] === $scope.sortedStorage[deleteIndex][6])
            {
              $rootScope.jsonData[i]['results'][j]['ifSaved'] = false;
              $rootScope.jsonData[i]['results'][j]['starClass'] = "glyphicon glyphicon-star-empty";
            }
          }
        }
      }
      if ($rootScope.favoriteRows.length <= 40 && $rootScope.favoriteRows.length > 20 && $rootScope.favoriteCurrentPage !== 1)
      {
        $scope.showPrevious = true;
        $scope.showNext = false;
      }
      if ($rootScope.favoriteRows.length <= 20)
      {
        $scope.showPrevious = false;
        $scope.showNext = false;
      }
      $rootScope.detailStarClass = "glyphicon glyphicon-star-empty";
      localStorage.removeItem(deleteKey);
    }

    $scope.sendKey = function(index)
    {
      $rootScope.ifSlide = true;

      //console.log($rootScope.favoriteRows);
      var sendIndex = ($rootScope.favoriteCurrentPage-1)*20 + index;
      var myKey = $rootScope.favoriteRows[sendIndex].place_id;
      var parsedData = JSON.parse(localStorage.getItem(myKey));
      var myLocationOption = parsedData[3];
      var locationInfo = parsedData[4];
      $scope.dataPack = [];
      $scope.dataPack[0] = myKey;
      $scope.dataPack[1] = myLocationOption;
      $scope.dataPack[2] = locationInfo;
      $scope.dataPack[3] = JSON.parse(localStorage.getItem(myKey))[0];
      $scope.dataPack[4] = JSON.parse(localStorage.getItem(myKey))[1];
      //console.log($scope.dataPack);
      $rootScope.favoriteRowIndex = sendIndex;
      favoriteDataService.setData($scope.dataPack);
      $rootScope.ifClickedDetails = false;
      $rootScope.ifClickedFavoriteDetails = true;
      for (var i = 0; i < $rootScope.favoriteRows.length; i++)
      {
        $rootScope.favoriteRows[i]['ifHighlight'] = false;
      }
      $rootScope.favoriteRows[sendIndex]['ifHighlight'] = true;
      $rootScope.savedKey = $rootScope.favoriteRows[sendIndex].place_id;
    }

    $scope.redirect = function(myPath)
    {
      $location.path(myPath);
    };

    $scope.redirectFavoriteDetailsPage = function()
    {
      $rootScope.ifSlide = true;
      $rootScope.moveToRight = true;
      if ($location.path() === '/favorites_page' && $rootScope.ifClickedFavoriteDetails === true)
      {
        $location.path('/favoriteDetails_page');
      }
      else
      {
        $location.path('/details_page');
      }
    }

    $scope.addAnimation = function()
    {
      $rootScope.ifSlide = true;
    }
  }]);
})(angular);
