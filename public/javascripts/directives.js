//adapted from http://alxhill.com/blog/articles/angular-scrollspy/
var jobFoundaryDirectives = angular.module('jobFoundryDirectives', []);  
jobFoundaryDirectives.directive('spy', function($location) {
    return {
      restrict: "A",
      require: "^scrollSpy",
      link: function(scope, elem, attrs, scrollSpy) {
        var _ref;
        if ((_ref = attrs.spyClass) == null) {
          attrs.spyClass = "selected";
        }
        elem.click(function() {
          scope.$apply(function() {
        	  var scrollPos = $("#" + attrs.spy).offset().top - 100;
          	  $('html, body').animate({
                  scrollTop: scrollPos
              }, 250);
          });
        });
        scrollSpy.addSpy({
          id: attrs.spy,
          "in": function() {
            return elem.addClass(attrs.spyClass);
          },
          out: function() {
            return elem.removeClass(attrs.spyClass);
          }
        });
      }
    };
  });

jobFoundaryDirectives.directive('scrollSpy', function($window) {
    return {
      restrict: 'A',
      controller: function($scope) {
        $scope.spies = [];
        this.addSpy = function(spyObj) {
          return $scope.spies.push(spyObj);
        };
      },
      link: function(scope, elem, attrs) {
        var spyElems;
        spyElems = [];
        scope.$watch('spies', function(spies) {
          var spy, _i, _len, _results;
          _results = [];
          for (_i = 0, _len = spies.length; _i < _len; _i++) {
            spy = spies[_i];
            if (spyElems[spy.id] == null) {
              _results.push(spyElems[spy.id] = elem.find('#' + spy.id));
            } else {
              _results.push(void 0);
            }
          }
        }, true);
        $($window).scroll(function() {
          var highlightSpy, pos, spy, _i, _len, _ref;
          highlightSpy = null;
          _ref = scope.spies;
          for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            spy = _ref[_i];
            spy.out();
            spyElems[spy.id] = spyElems[spy.id].length === 0 ? elem.find('#' + spy.id) : spyElems[spy.id];
            if (spyElems[spy.id].length !== 0) {
              if ((pos = spyElems[spy.id].offset().top - 100) - $window.scrollY <= 0) {
                spy.pos = pos;
                if (highlightSpy == null) {
                  highlightSpy = spy;
                }
                if (highlightSpy.pos < spy.pos) {
                  highlightSpy = spy;
                }
              }
            }
          }
          if (highlightSpy != null)
        	  highlightSpy["in"]();
        });
      }
    };
  });