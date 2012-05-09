var Foosball = Foosball || {};
$(document).ready(function(){
    
    window.serverURL = "/api";
    
    
    Backbone.View.prototype.close = function()
    {
        this.trigger('close');
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
            attrs.rfid = response.rfid;
            return attrs;
        },

        
        defaults: 
        {
            'id': undefined,
            'username' : undefined,
            'image': undefined,
            'password': undefined,
            'rfid': undefined
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
            attrs.rating = response.rating;
            attrs.team_name = response.team_name;            
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
            'team_name': undefined,
            'rating': 0
        }
    });

   Foosball.Goal = Backbone.Model.extend
   ({
        defaults:
        {
            'player': undefined, 
            'position': undefined,
            'game_id': undefined,
            'backfire': false, 
            'timestamp': 0
        }
   }); 
    
   Foosball.Goals = Backbone.Collection.extend
   ({
        url: window.serverURL + '/game/goals',
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
        },
        
        save: function()
        {
            var collection = JSON.stringify(this.toJSON());
            $.ajax({
                type: 'POST',
                url: this.url,
                data: collection,
                error: function(){Foosball.message.showError("An error occured while we attempted to save the goals");},
                dataType: "json"
            });
        }
        
    });
     
   Foosball.Game = Backbone.Model.extend
   ({
        url: window.serverURL + '/game',
        
        initialize: function()
        {
            _.bindAll(this, 'isReady', 'start', 'finish', 'restart', 'reset');
            this.set('home_team', new Foosball.Team({side: 'home'}));
            this.set('visitor_team',new Foosball.Team({side: 'visitor'}));
        },
       
        isReady: function()
        {
            
            var home = this.get('home_team'),
                visitor = this.get('visitor_team');
            
            // Checks that both teams at least have one player
            if(home.count() === 0 || visitor.count() === 0)
            {
                Foosball.message.showError("Both teams need at least one player!");    
                return false;
            }
            
            // Both teams need to have an equal amount of players
            // if the game mode is ranked
            if(this.get('mode') === 'ranked')
            {    
                if(home.count() !== visitor.count())
                {
                    Foosball.message.showError("A ranked game need an equal amount of players on both teams!");    
                    return false;
                }
            }
            
           if(this.get('state') !== 'started')
           {                            
                   this.set('state', 'ready');
           }   
           return true;
        },
       
        start: function() 
        {
           if(!this.isReady()) return false; 
           
           
           // Authenticate teams
           var teamsAuthenticated = this.get('home_team').authenticate();
           teamsAuthenticated = this.get('visitor_team').authenticate();
            
           if(teamsAuthenticated === true)
           {   
             if(this.authenticate() === true)
             {
                this.set('state', 'started');
                return true;
             }
           }
           
           return false;
        },
       
        finish: function() 
        {
            var winner = this.get('home_score') > this.get('visitor_score') ? this.get('home_team') : this.get('visitor_team'); 
            this.set('state', 'finish'); 
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
            // Re-sets some of the game variables to default-values. 
            // We still keep information about authenticated teams and players
            this.set
            ({
                id: undefined,
                home_score: 0,
                visitor_score: 0,
                state: 'ready'
            });
            // Starts the game
            this.start();
        },
       
        reset: function()
        {
            // Remembers the current game-mode
            var mode = this.get('mode');
            // Re-sets all values to defaults
            this.set(this.defaults);
            // Sets the game mode to what was previously set
            this.set('mode', mode); 
            // Re-sets the teams and players
            this.get('home_team').reset(); 
            this.get('visitor_team').reset(); 
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
            'state': 'new',
            'mode' : 'ranked'
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
            
            else if(state === 'finish')
            {
                this.get('goals').save();
            }
        },
        
        addGoal: function(player, backfire)
        {
            var goal = new Foosball.Goal({
                                           player: player,
                                           position: player.get('position'),
                                           game_id: window.game.get('id'),
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
                
                window.game.set(team + '_score', window.game.get(team + '_score') - 1);}
        },
        
        startClock: function()
        {
            // tick tock; 
        },
        
        stopClock: function()
        {
           // tock tick 
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
               return game.get('home_team');
           }
           else
           {
               return game.get('visitor_team');
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
        className: 'player-container',
        template: _.template($('#player-template').html()),
        
        events: 
        {
          'focus .player' : 'focus',
          'blur .player' : 'unfocus',
          'click .player-login' : 'login',
          'click .player-logout' : 'logout',
          'click .player-score .player-image' : 'displayOptions', 
          'click .goal-options-cancel' : 'hideOptions',
          'click .goal-options-score' : 'score',
          'click .goal-options-backfire' : 'backfire',
          'click .goal-options-unscore' : 'unscore'
          
        },
      
        initialize: function()
        {
            _.bindAll(this, 'render', 'login', 'logout', 'score', 'unscore', 'handleGamestate', 'displayOptions', 'hideOptions', 'backfire', 'focus', 'unfocus');
            this.player = this.options.player;
            this.player.set('position', this.options.position);
            
            this.player.on('change:id', this.render);
            window.game.on('change:state', this.handleGamestate);
        },
        
        focus: function(){
            this.$el.find('.player-image').addClass('strong-shadow');
        },
        
        unfocus: function(){
            this.$el.find('.player-image').removeClass('strong-shadow');
        },
      
        render: function()
        {
            $(this.el).html(this.template(this.player.toJSON()));   
            return this;
        },
        
        
        login: function()
        { 
          Foosball.board.showLoginView(this.player);  
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
           window.referee.addGoal(this.player, false);
           this.hideOptions();
        },
        
        unscore: function()
        {
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
    
    window.TeamNameView = Backbone.View.extend
    ({
        className: 'team-name-container',
        initialize: function()
        {   
            this.team = this.options.team;
            this.defaultName = 'Home team';
            if(this.team === window.game.get('visitor_team'))
                this.defaultName = 'Visitor team';
            
            this.team.on("change:team_name", this.render, this);
        },
      
        render: function()
        {
            var name =  this.team.get('team_name'); 
            if(!name)
                name = this.defaultName;
            
            this.$el.html('<h2 class="team-name">' + name + "</h2>"); 
            return this;
        }
    });

    window.TeamView = Backbone.View.extend
    ({
        initialize: function()
        {   
            this.team = this.options.team;
            this.render();
        },
      
        render: function()
        {
            var player1View = new PlayerView({
                position: 1, 
                player: this.team.get('player1')
            });
            
            var player2View = new PlayerView({
                position: 2, 
                player: this.team.get('player2')
            });
            
            var teamNameView = new TeamNameView({team: this.team});
            
            this.$el.empty();
            this.$el.append(teamNameView.render().el);
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
            'keypress #rfid-input' : 'rfidInput',
            'click #rfid' : 'rfid',
            'click #login' : 'authenticate',
            'click #register' : 'register'
        },
      
        initialize: function()
        {
            _.bindAll(this, 'authenticate', 'register', 'isPlayerPresent', 'success', 'error', 'rfid', 'rfidInput');
            this.player = this.options.player; 
        },
        
        rfid: function()
        {
            Foosball.message.showMessage("Please scan your RFID-card", true); 
            var input = this.$el.find('#rfid-input');
            input.val(''); 
            input.focus(); 
        },
        
        rfidInput: function(e)
        {
            if (e.keyCode == 13)             
            {
                  Foosball.message.hide(); 
                  
                  var rfid = this.$el.find('#rfid-input').val();
                  if(this.isPlayerPresent(rfid) === false)
                  {
                      this.player.set('rfid', rfid, {silent: true});
                      var options = 
                      {
                        url: window.serverURL + '/player/login/rfid/' + rfid,
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
               e.preventDefault();
            }
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
  
        isPlayerPresent: function(id)
        {
          /*
             Used to check if a player with a given username or rfid is 
             present in one if the teams.
           */
          var comparator = function(player)
          {
              return player.isAuthenticated() && (player.get('username') === id || player.get('rfid') == id);
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
                    url: window.serverURL + '/player/login',
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
               this.player.set({username: username, password: password}, {silent:true});
               this.player.save({},options);
            }
        },
        
        success: function()
        {
            this.close();
        },
        
        error: function(model, response)
        {
           model.reset(); 
           Foosball.message.showError(response.responseText);    
        },
        
        register: function()
        {
          var username = $('#username').attr('value'); 
          var password = $('#password').attr('value'); 
          
          if(!username || !password)
          {
            Foosball.message.showError("Hey! Please provide us with an username and password!"); 
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
           $(this.el).html('<button id="start-game-button" class="button green">start</button>');
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
    
    Foosball.GameModeView = Backbone.View.extend
    ({
        el: '#game-mode', 
        template: _.template($('#mode-template').html()),
        events: 
        {
            'click #friendly-mode-button': 'setFriendlyMode',
            'click #ranked-mode-button': 'setRankedMode'
        },
        
        initialize: function()
        {
            window.game.on('change:state change:mode', this.render, this);
            this.render();
        },
        
        render: function()
        {
            var state = window.game.get('state');
            var mode = window.game.get('mode');
            
            if(state !== 'new')
            {
                this.$el.hide();
            }
            else
            {
                var attributes = 
                {
                   isFriendlyActive: mode === 'friendly' ? 'button-active yellow' : '',
                   isRankedActive: mode === 'ranked' ? 'button-active yellow' : ''
                }
                
                this.$el.html(this.template(attributes)).show(); 
            }
            return this;
        },
        
        setFriendlyMode: function()
        {
            window.game.set('mode', 'friendly');
        },
        
        setRankedMode: function()
        {
            window.game.set('mode', 'ranked');
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
                    homeTeam: window.game.get('home_team'),
                    visitorTeam: window.game.get('visitor_team'),
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
        
        handleGameState: function()
        {
            var state = window.game.get('state');
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
            var scoreBoard = new Foosball.ScoreBoardView();
            this.show(scoreBoard);
        },
        
        showLoginView: function(player)
        {
          var login = new Foosball.LoginView({player: player});
          this.show(login);
        },
        
        show: function(view)
        { 
            if(this.current !== undefined)
            {    
                this.current.off();
                this.current.close();
                
            }
            
            view.on('close', this.handleGameState, this);
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
            'click #close-message' : 'hide'
        },
       
        showMessage: function(message, hold)
        {
            if(!hold)
            hold = false;
        
            var data = 
            {
                message: message,
                type: 'message',
                hold: hold
            };
            this.$el.html(this.template(data));
            this.show(hold);
        },
        
        showError: function(message, hold)
        {
            var data = 
            {
                message: message,
                type: 'error',
                hold: hold
            };
            $(this.el).html(this.template(data));
            this.show(hold);
        },
        
        show: function(hold)
        {
          var self = this;
          $(self.el).slideDown(150);   
            if(!hold)
            {
                if(self.timer)
                clearTimeout(self.timer);
                
                self.timer = setTimeout(function() 
                {
                  self.hide();
                  self.timer = null;
                }, 4000);
            }
        },
        
        hide: function()
        {
          $(this.el).slideUp(150); 
        }
    }); 
    
    
    Foosball.Field = Backbone.View.extend
    ({
        el: '#field',
        initialize: function()
        {
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
            else if(state === 'finish')
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
    Foosball.gameMode = new Foosball.GameModeView();
    Foosball.gameState = new Foosball.GameStateView(); 
    
    
    // Request indicator. 
    // Everytime we make a request to the server, 
    // we want show an indicator until the server responds to us
    $(document).ajaxStart(function()
    { 
        $('#request-indicator').stop().slideDown(250); 
  
    }).ajaxStop(function()
    { 
        $('#request-indicator').stop().fadeOut(150);
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


