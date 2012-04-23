var Foosball = Foosball || {};
$(document).ready(function(){
    
    window.serverURL = "/api";
    
    
    Backbone.View.prototype.close = function(){
        this.remove();
        this.unbind();
        if (this.onClose){
            this.onClose();
        }
    }
    
    /************************************************************************/
    /*  MODELS
    /************************************************************************/
    
    
    Foosball.Player = Backbone.Model.extend
    ({
        initialize: function()
        {
          
        },
        
        isAuthenticated: function()
        {
            return this.get('id') !== undefined;
        },
                
        reset: function()
        {
            this.set(this.defaults);
        },
        
        equals: function(player)
        {
           return this.get('id') === player.get('id')
                  && this === player; 
        },
        
        parse: function(response) 
        {
            var attrs = {};
            attrs.id = response.id; 
            attrs.username = response.username; 
            attrs.image = response.image; 
            return attrs;
        },

        
        defaults: 
        {
            'id': undefined,
            'username' : undefined,
            'image': undefined,
            'password': undefined
        }
    }); 
    
    Foosball.Team = Backbone.Model.extend({
        url: window.serverURL + '/team',
        
        initialize: function(){
            this.set('player1', new Foosball.Player());
            this.set('player2', new Foosball.Player());
        },
        
        contains: function(player)
        {
            return this.get('player1').equals(player) || 
                   this.get('player2').equals(player);
        },
        
        find: function(comparator)
        {
            var players = [this.get('player1'), this.get('player2')]; 
            var results = _.find(players, comparator); 
            return results;
        },

        count: function()
        {
          var count = 0;
          if(this.get('player1').isAuthenticated())   
              count++; 
          if(this.get('player2').isAuthenticated())  
              count++
          
          return count;
        },
        
        authenticate: function()
        {
            if(!this.isAuthenticated())
            {
                this.save({},{
                async: false,
                success: function()
                {
                  console.log("team was successfully authenticated");
                },
                error: function()
                {
                  console.log("team was not authenticated");
                }
                });
            }
            
            return this.isAuthenticated();
        },
        
        isAuthenticated: function()
        {
            return this.get('id') !== undefined; 
        },
        
        parse: function(response) 
        {
            var attrs = {};
            attrs.id = response.id;
            
            // If the team name is not set, then 
            // we will use a combination of both player names.
//            if(!response.team_name)
//            { 
//               var p1 = response.player1.username;
//               var p2 = (!response.player2) ? undefined : ' + ' + response.player2.username; 
//               
//               response.team_name = p1; 
//               response.team_name += (!p2) ? '' : p2; 
//            }
//            
//            attrs.name = response.team_name;
            return attrs;
        },
        
        reset: function()
        {
            this.get('player1').reset();
            this.get('player2').reset();
            this.set(this.defaults);
        },
        
        defaults: 
        {
            'id': undefined,
            'team_name': ''
        }
    });

   Foosball.Goal = Backbone.Model.extend
   ({
        defaults:
        {
            'player': undefined, 
            'backfire': false, 
            'timestamp': 0
        }
   }); 
    
   Foosball.Goals = Backbone.Collection.extend
   ({
        model: Foosball.Goal,   
        
        pop: function(player) 
        {
            var goal; 
            if(this.length === 0)
            {
                return undefined;
            }
            
            // If no player is specified, get the last goal scored
            if(!player)
            {
             goal = this.at(this.length - 1);             
            }
            
            else
            { 
                // Find all goals scored by the player
                var filter = this.filter(function(goal){
                    return goal.get('player').equals(player);
                }); 
                // Get the last goal scored
                goal = _.last(filter);
            }
            
            if(goal !== undefined)
                this.remove(goal);	
            
             return goal;
        }
        
    });
     
   Foosball.Game = Backbone.Model.extend
   ({
        url: window.serverURL + '/game',
        
        initialize: function()
        {
            _.bindAll(this, 'isReady', 'start', 'finish', 'restart', 'reset');
            this.set('home_team', new Foosball.Team());
            this.set('visitor_team',new Foosball.Team());
        },
       
        isReady: function()
        {
            
            var home = this.get('home_team'),
                visitor = this.get('visitor_team');
                
            if(home.count() > 0 && visitor.count() > 0)
            {   
                /*
                // Checks if both teams have equal amount of players
                if(home.count() === visitor.count())
                {
                    if(this.get('state') !== 'started')
                    {                            
                       this.set('state', 'ready');
                    }
                    return true;
                }
                */
               
               if(this.get('state') !== 'started')
               {                            
                       this.set('state', 'ready');
               }
               return true;
            }
            
            return false;
        },
       
        start: function() 
        {
           if(!this.isReady())
               return false; 
           
           // Authenticate teams
           var teamsAuthenticated = this.get('home_team').authenticate();
           teamsAuthenticated = this.get('visitor_team').authenticate();
            
           if(teamsAuthenticated === true)
           {   
             if(this.authenticate() === true)
             {
                this.set('state', 'started');
                console.log('the game has started');
                return true;
             }
           }
           
           return false;
        },
       
        finish: function() 
        {
            var winner = this.get('home_score') > this.get('visitor_score') ? this.get('home_team') : this.get('visitor_team'); 
            this.set('state', 'end'); 
            this.save({}, {
                success: function()
                {
                   console.log('the game was saved');
                },
                error: function()
                {
                    throw new Error('game was not saved');
                }
            }); 
        },
       
        restart: function()
        {
            this.set
            ({
                id: undefined,
                home_score: 0,
                visitor_score: 0,
                state: 'ready'
            });
            this.trigger('restart');
            this.start();
        },
       
        reset: function()
        {
            this.set(this.defaults);
            this.get('home_team').reset(); 
            this.get('visitor_team').reset(); 
            this.trigger('reset');
        },
        
        authenticate: function()
        {
           this.save({}, {
               async: false,
               success: function()
               {
                 console.log("the game has been authenticated by the server");   
                 
               },
               error: function()
               {
                 throw new Error("the game was not been authenticated by the server");  
               }
           });
           
           return this.isAuthenticated();
        },
        
        isAuthenticated: function()
        {
            var id = this.get('id');
            return id !== undefined && id > 0; 
        },
        
        parse: function(response) 
        {
            var attrs = {};
            attrs.id = response.id;
            attrs.start_time = response.start_time;
            return attrs;
        },
        
        defaults:
        {
            'id': undefined,
            'home_score': 0,
            'visitor_score' : 0,
            'start_time': undefined,
            'end_time': undefined,
            'state': 'new'
        }
    });
    
    
    Foosball.Referee = Backbone.Model.extend({
        initialize: function()
        { 
            this.set('goals', new Foosball.Goals());
            this.set('time', 0);
            window.game.on('change:state', this.handleGameState, this);
        },
        
        handleGameState: function(game)
        {
            var state = game.get('state');
            if(state === 'started')
            {
                this.reset();
                this.startClock();
            }
        },
        
        addGoal: function(player, backfire)
        {
            
            var goal = new Foosball.Goal({
                                           player: player,
                                           backfire: backfire,
                                           timestamp: new Date().getTime()
                                         });
            this.get('goals').add(goal);

            var team = window.game.get('home_team').contains(player) ? 'home' : 'visitor';
            
            if(backfire)
            {
                team = team === 'home' ? 'visitor' : 'home';
            }
            
            window.game.set(team + '_score', window.game.get(team + '_score') + 1);
            
            this.isGameWon();
        },
       
        removeGoal: function(player)
        {
            // remove the last goal
            var goal = this.get('goals').pop(player);

            if(goal !== undefined)
            {
                var player = goal.get('player');
                var team = window.game.get('home_team').contains(player) ? 'home' : 'visitor';
           
                if(goal.get('backfire'))
                {
                    team = team === 'home' ? 'visitor' : 'home';
                }
                
                window.game.set(team + '_score', window.game.get(team + '_score') - 1); }
        },
        
        startClock: function(){
            // tick tock; 
        },
        
        stopClock: function(){
            
        },
        
        isGameWon: function ()
        {
            if(window.game.get('home_score') === 10 || window.game.get('visitor_score') === 10 )
            {
                window.game.finish();
            }
        },
        
        getMatchLeader: function()
        {
           var home_score = game.get('home_score');
           var visitor_score = game.get('visitor_score');
           if(home_score === visitor_score)
           {
               return undefined;
           }
           else if(home_score > visitor_score)
           {
               return 'home_team'; 
           }
           else
           {
               return 'visitor_team'; 
           }
        },
        
        reset:function()
        {
           this.get('goals').reset();
           this.set('time', 0);
        }
    });
    
    /************************************************************************/
    /*  VIEWS
    /************************************************************************/
    
      
    window.PlayerView = Backbone.View.extend
    ({
        className: 'player',
        template: _.template($('#player-template').html()),
        
        events: 
        {
          'click .player-login' : 'login',
          'click .player-logout' : 'logout',
          //'dblclick .player-score' : 'unscore',
          'click .player-score .player-image' : 'displayOptions', 
          'click .goal-options-cancel' : 'hideOptions',
          'click .goal-options-score' : 'score',
          'click .goal-options-backfire' : 'backfire',
          'click .goal-options-unscore' : 'unscore'
        },
      
        initialize: function()
        {
            _.bindAll(this, 'render', 'login', 'logout', 'score', 'unscore', 'handleGamestate', 'displayOptions', 'hideOptions', 'backfire');
            this.player = this.options.player;
            this.player.set('position', this.options.position);
            //this.clickTimer = undefined;
            
            this.player.on('change:id', this.render);
            window.game.on('change:state', this.handleGamestate);
        },
      
        render: function()
        {
            $(this.el).html(this.template(this.player.toJSON()));   
            return this;
        },
        
        
        login: function()
        {
          var login = new Foosball.LoginView({player: this.player});
          Foosball.board.show(login);
        },
        
        logout: function()
        {
          this.player.reset();  
        },
        
        displayOptions: function()
        {
            this.$el.find('div.goal-options').fadeIn(250);
        },

        hideOptions: function()
        {
            this.$el.find('div.goal-options').fadeOut(250);
        },
        
        score: function()
        {
//          var timer = this.clickTimer;
//          if (timer) 
//              clearTimeout(timer);
//         
//         var p = this.player;
//         var closure = function(player){ window.referee.addGoal(player);}; 
//         this.clickTimer = setTimeout(function(){ closure(p) }, 250); 

           window.referee.addGoal(this.player, false);
           this.hideOptions();
        },
        
        unscore: function()
        {
          //clearTimeout(this.clickTimer);
          window.referee.removeGoal(this.player);
          this.hideOptions();
        },
        
        backfire: function()
        {
            window.referee.addGoal(this.player, true);
            this.hideOptions();
        },
        
        handleGamestate: function(game)
        {
            if(game.get('state') === 'started')
            {
                if(!this.player.isAuthenticated())
                {
                    this.close(); 
                }
                else
                {
                    this.render(); //Re-render the existing views
                }
            }
        },
        
        onClose: function()
        {
            this.player.off('change:id', this.render);
            window.game.off('change:state', this.handleGamestate);
        }
    }); 

    window.TeamView = Backbone.View.extend({
        initialize: function()
        {   
            this.render();
        },
      
        render: function()
        {
            var player1View = new PlayerView({
                position: 1, 
                player: this.options.team.get('player1')
            });
            
            var player2View = new PlayerView({
                position: 2, 
                player: this.options.team.get('player2')
            });
            
            this.$el.empty();
            this.$el.append(player1View.render().el);
            this.$el.append(player2View.render().el);
            return this;
        }
    });
 

    Foosball.LoginView = Backbone.View.extend
    ({
        template: _.template($('#login-template').html()),
        events: 
        {
            'click #login' : 'authenticate',
            'click #register' : 'register'
        },
      
        initialize: function()
        {
            _.bindAll(this, 'authenticate', 'register', 'isPlayerPresent', 'success', 'error');
            this.player = this.options.player; 
        },
      
        render: function()
        {
            $(this.el).html(this.template);
            this.$el.find('#username').typeahead({
                items: 3, 
                source: function (typeahead, query) {
                    return $.get('/api/player/autocomplete/' + query, {
                        query: ''
                    }, function (data) 
                    {
                        return typeahead.process(data);
                    });
                }
            });
            return this;
        },
  
        isPlayerPresent: function(username)
        {
          /*
             Used to check if a player with a given username is 
             present in one if the teams.
           */
          var comparator = function(player)
          {
              return player.isAuthenticated() && player.get('username') === username;
          }; 
          
          var isPresent = window.game.get('home_team').find(comparator) !== undefined || 
                          window.game.get('visitor_team').find(comparator) !== undefined;
                      
          if(isPresent === true) 
            Foosball.message.showError('Woops! This player is allready logged in!');
                                
          return isPresent;  
        },
      
        authenticate: function()
        {
          // Get form values
          var username = $('#username').attr('value'); 
          var password = $('#password').attr('value'); 
          
          if(!username || !password)
          {
            Foosball.message.showError("Hei! Please provide us with an username and password!");    
            return;
          }
           
          if(this.isPlayerPresent(username) === false)
          {
              var options = 
                  {
                    url: window.serverURL + '/player/login/' + username + '/' + password,
                    async: true,
                    success: this.success,
                    error: this.error,
                    wait:true
                  };
               
               /* 
               Performs an GET request to the server
               based on the options provided. 
                
               If the user is found, and properly authenticated, 
               Then the server will send us a new instance of the user.
               */
               this.player.fetch(options);
            }
        },
        
        success: function()
        {
            this.close();
        },
        
        error: function(model, response)
        {
             Foosball.message.showError(response.responseText);    
        },
        
        register: function()
        {
          var username = $('#username').attr('value'); 
          var password = $('#password').attr('value'); 
          
          if(!username || !password)
          {
            Foosball.message.showError("Hei! Please provide us with an username and password!"); 
            return;
          }
          
          if(this.isPlayerPresent(username) === false)
          {
                            
              this.player.set({username: username, password: password}, {silent:true});
                  
              var options = 
                  {
                    url: window.serverURL + '/player/register',
                    async: true,
                    success: this.success,
                    error: this.error,
                    wait:true
                  };
                  
               this.player.save({}, options);
           }
        }
    }); 
    
    Foosball.GameStateView = Backbone.View.extend
    ({
        el: '#game-state',
        events:
        {
            'click #start-game-button': 'start',
            'click #cancel-game-button': 'cancel'
        },
        
        initialize: function()
        {
           _.bindAll(this, 'render');
           window.game.on('change:state', this.render, this);
           this.render();
        },
        
        render: function()
        {
           var state = window.game.get('state');
           if(state !== 'started')
           $(this.el).html('<button id="start-game-button" class="button green">start!</button>');
           else
           $(this.el).html('<button id="cancel-game-button" class="button yellow">cancel</button>');    
        },
        
        start: function()
        {
          window.game.start();
        },    
        
        cancel: function()
        {
          window.game.reset();
        }        
    });
    
    
    Foosball.ScoreBoardView = Backbone.View.extend
    ({
        id: 'score-board',
        template: _.template($('#scoreboard-template').html()),
        events: 
        {
            'click #regret-goal': 'regretGoal'
        },
        
        initialize: function()
        {
            _.bindAll(this, 'render', 'regretGoal');
            window.game.on('change:home_score change:visitor_score', this.render, this);
        },
        
        render: function()
        {
            var score = 
            {
                homeScore: window.game.get('home_score'),
                visitorScore: window.game.get('visitor_score')
            };
            
            $(this.el).html(this.template(score));
            return this;
        },
        
        regretGoal: function()
        {
            window.referee.removeGoal();
        },
        
        onClose: function()
        {
           window.game.off('change:home_score', this.render);  
           window.game.off('change:visitor_score', this.render);  
        }
    }); 
    
    Foosball.ResultView = Backbone.View.extend
    ({
        id: 'result',
        template: _.template($('#result-template').html()),
        events:
        {
            'click #finish-button': 'finishGame',
            'click #restart-button': 'restartGame'
        },
        
        initialize: function()
        {
            _.bindAll(this, 'render', 'finishGame', 'restartGame');
        },
        
        render: function()
        {
           var data = 
                {
                    homeTeam: window.game.get('home_team').get('name'),
                    visitorTeam: window.game.get('visitor_team').get('name'),
                    winner: window.referee.getMatchLeader()
                };
            
            this.$el.html(this.template(data));
            
            $('#result-container').css(
            {
                left: $(window).width() / 2 - $('#result-container').width() / 2
            });
            
            this.$el.fadeIn();
            return this;
        },
        
        finishGame: function()
        {
            window.game.reset(); 
            this.close();
        },
        
        restartGame: function()
        {
            window.game.restart(); 
            this.close();
        }
    }); 
    
    Foosball.BoardView = Backbone.View.extend
    ({
        el: '#board',
        
        initialize: function()
        {
          window.game.on('change:state', this.handleGameState, this);  
          this.showWelcomeMessage();
        },
        
        handleGameState: function(game)
        {
            var state = game.get('state');
            if(state === 'new')
            {
                this.showWelcomeMessage();
            }
            else if(state === 'started')
            {
                this.showScoreBoard();
            }
        },
        
        showWelcomeMessage: function()
        {
            $(this.el).html('<div id="welcome"><h1>Welcome to openfoos</h1><h2>Click on a player to log-in</h2></div>');
        },
        
        showScoreBoard: function()
        {
            console.log('board:showScore');
            var scoreBoard = new Foosball.ScoreBoardView();
            this.show(scoreBoard);
        },
        
        show: function(view)
        {
            if(this.current !== undefined)
                this.current.close();
            
            this.current = view;
            $(this.el).empty();
            $(this.el).html(view.render().el);
        }
        
    });
    
    Foosball.MessageView = Backbone.View.extend
    ({
        el: '#messages', 
        template: _.template($('#message-template').html()),
        events:
        {
        
        },
        
        showMessage: function(message)
        {
            var data = 
            {
                message: message,
                type: 'message'
            };
            this.$el.html(this.template(data));
            this.show();
        },
        
        showError: function(message)
        {
            var data = 
            {
                message: message,
                type: 'error'
            };
            $(this.el).html(this.template(data));
            this.show();
        },
        
        show: function()
        {
          var self = this;
          $(self.el).slideDown();   
            if(self.timer)
                clearTimeout(self.timer);
                self.timer = setTimeout(function() 
                {
                  self.hide();
                  self.timer = null;
                }, 2000);
        },
        
        hide: function()
        {
          $(this.el).slideUp(); 
        }
    }); 
    
    
    
    Foosball.Field = Backbone.View.extend
    ({
        el: '#field',
        initialize: function()
        {
          var stateView = new Foosball.GameStateView();
          window.game.on('change:state', this.handleGameState, this);
          this.renderTeams();
        },
        
        renderTeams: function()
        {
           var homeTeamView = new TeamView
           ({
                team: window.game.get('home_team'),
                el: '#home-team'
           });
           
            
           var visitorTeamView = new TeamView
           ({
                team: window.game.get('visitor_team'),
                el: '#visitor-team'
           });
        },
        
        handleGameState: function(game)
        {
            var state = game.get('state');
            if(state === 'new')
            {                
                this.renderTeams();
            }
            else if(state === 'end')
            {                
                var resultView = new Foosball.ResultView();
                $(this.el).append(resultView.render().el);
            }
        }
    });
    
    window.game = new Foosball.Game();
    window.referee = new Foosball.Referee();
    Foosball.message = new Foosball.MessageView();
    Foosball.board = new Foosball.BoardView();
    Foosball.application = new Foosball.Field();
    
    
    // Request indicator. 
    // Everytime we make a request to the server, 
    // we want show an indicator until the server responds to us
    $(document).ajaxStart(function()
    { 
      $('#request-indicator').slideDown(350); 
    }).ajaxStop(function()
    { 
        $('#request-indicator').slideUp(350);
    });

 
 /*
    window.ApplicationRouter = Backbone.Router.extend
    ({
        routes: 
        {
            '':   "enter"
        },
        
        initialize: function()
        {
          this.field = new Foosball.FieldView();  
        },
        
        enter: function()
        {
           
        }
        
    });
    
    window.router = new ApplicationRouter();
    Backbone.history.start();
    
    */
}); // End dokument.ready


