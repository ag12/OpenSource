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
            attrs.won = response.won;            
            attrs.lost = response.lost;            
            
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
            'rating': 0,
            'won': 0,
            'lost': 0
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
   
   Foosball.Clock = Backbone.Model.extend
   ({ 
       tick: function()
       {
           this.set('seconds', (this.get('seconds') + 1));
       },
       
       reset: function()
       {
           this.set('seconds', 0);
       },
       
       defaults:
       {
            'seconds': 0
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
             var self = this;
             var successCallback = function(){
                 self.set('state', 'started');
             };
             self.authenticate(successCallback);
             
           }
           
        },
        
        end: function()
        {
            this.set('state', 'end');
        },
        
        finish: function() 
        {
            var self = this;
            
            var successCallback = function(){
                self.set('state', 'finish'); 
                self.reset();
            };
            
            self.authenticate(successCallback);
            
        },
       
        restart: function()
        {
            // Re-sets some of the game variables to default-values. 
            // We still keep information about authenticated teams and players
            var self = this;
            var successCallback = function(){
                self.set('state', 'restart');
                self.set
                ({
                    id: undefined,
                    home_score: 0,
                    visitor_score: 0,
                    state: 'ready'
                });
                // Starts the game
                self.start();
            };
            self.authenticate(successCallback);
            
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
        
        onOvertime: function()
        {
            this.set('state', 'overtime');
        },
        
        authenticate: function(successCallback)
        {
           this.save({}, {
               async: true,
               success: function()
               {
                 successCallback();  
                 console.log("the game has been authenticated by the server");   
               },
               error: function()
               {
                 Foosball.message.showError("An error occured when trying to authenticate the game", false);  
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
            _.bindAll(this, 'handleKeypress');
            this.set('goals', new Foosball.Goals());
            window.game.on('change:state', this.handleGameState, this);
            $(document).bind('keypress', this.handleKeypress);
        },
        
        handleKeypress: function(e)
        {    
          var state = window.game.get('state');  
          if(state === 'started' || state === 'overtime')
          {
              var key = e.keyCode;
              var player = undefined;
              
              if(key === 49)
              {
                 player = game.get('home_team').get('player1'); 
              }
              else if(key === 50)
              {
                 player = game.get('home_team').get('player2'); 
              }
              else if(key === 51)
              {
                 player = game.get('visitor_team').get('player1'); 
              }
              else if(key === 52)
              {
                 player = game.get('visitor_team').get('player2'); 
              }
              
              if(player !== undefined && player.isAuthenticated())
              {
                  this.addGoal(player, false);
              }
          }
        },
        
        handleGameState: function(game)
        {
            var state = game.get('state');
            
            if(state === 'started')
            {
                this.reset();
                this.startClock();
            }
            else if(state === 'end' || state === 'new')
            {
                this.stopClock();
            }
            else if(state === 'finish')
            {
                this.get('goals').save();
            }
            else if(state === 'overtime')
            {
                this.startClock();
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
            Foosball.message.showMessage('GOOOOOOAL!',false)
            this.isGameWon();
        },
       
        removeGoal: function(player)
        {
            // Remove the last goal
            var goal = this.get('goals').pop(player);

            if(goal !== undefined)
            {
                var player = goal.get('player');
                var team = window.game.get('home_team').contains(player) ? 'home' : 'visitor';
           
                if(goal.get('backfire'))
                {
                    team = team === 'home' ? 'visitor' : 'home';
                }
                
                window.game.set(team + '_score', window.game.get(team + '_score') - 1);
            }
            
            if(window.game.get('state') === 'end')
            {
                window.game.onOvertime();
            }
        },
        
        startClock: function()
        {
            if(!this.clockInterval)
            {
                this.clockInterval = setInterval(function()
                {
                    Foosball.clock.tick();
                }, 1000); 
            }
        },
        
        stopClock: function()
        {
           clearInterval(this.clockInterval);
           this.clockInterval = undefined;
        },
        
       
        isGameWon: function ()
        {
            if(window.game.get('home_score') === 10 || window.game.get('visitor_score') === 10 )
            {
                window.game.end();
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
           Foosball.clock.reset();
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
          'click .player-login' : 'login',
          'click .player-logout' : 'logout',
          'click .player-score .player-image' : 'displayOptions', 
          'click .goal-options-cancel' : 'hideOptions',
          'click .goal-options-score' : 'score',
          'click .goal-options-backfire' : 'backfire',
          'click .goal-options-unscore' : 'unscore',
          'click .player-image' : 'focus' 
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
            if(!this.isFocused)
            {
                this.$el.find('.player').css('background-color', '#cfff84');
                this.isFocused = true;
            }
        },
        
        unfocus: function(){
            this.$el.find('.player').css('background-color', 'whiteSmoke');
            this.isFocused = false;
        },
      
        render: function()
        {
            this.$el.html(this.template(this.player.toJSON()));   
            this.isFocused = false;
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
            this.unfocus();
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
        className: 'team-name-container cursive dark-green',
        initialize: function()
        {   
            this.team = this.options.team;
            this.defaultName = 'Home team';
            if(this.team === window.game.get('visitor_team'))
                this.defaultName = 'Away team';
            
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
            'click #rfid' : 'rfidOption',
            'click #login' : 'authenticateOptions',
            'click #register' : 'register'
        },
      
        initialize: function()
        {
            _.bindAll(this, 'authenticate', 'register', 'isPlayerPresent', 'success', 'error', 'rfidOption', 'rfidInput', 'authenticateOptions');
            this.player = this.options.player; 
        },
        
        rfidOption: function()
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
                      this.lastRFID = rfid;
                      //this.player.set('rfid', rfid, {silent: true});
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
            this.$el.html(this.template);
            var username = this.$el.find('#username');
            username.typeahead
            ({
                items: 3, 
                source: function (typeahead, query) {
                    return $.get('/api/player/autocomplete/' + query, 
                    {
                        query: ''
                    }, 
                    function (data) 
                    {
                        return typeahead.process(data);
                    });
                }
            });
            
            // The field is not visible at the instant that this render method is called. 
            // This is because 
            setTimeout(function()
            {
                username.focus();
            },500);

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
        
        authenticateOptions: function(){
            
            if(this.lastRFID)
            {
                var self = this;
                Foosball.message.showOptionDialog('Do you want to set this RFID?', function(){self.authenticate(true)}, function(){self.authenticate(false)}); 
            }
            else
            {
                this.authenticate(false);
            }
        },
      
        authenticate: function(setRFID)
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
               if(setRFID === true)
               {    
                    this.player.set({rfid: this.lastRFID}, {silent:true});
               }
               
               this.lastRFID = undefined;
               this.player.set({username: username, password: password}, {silent:true});
               this.player.save({},options);
            }
        },
        
        success: function()
        {
            this.lastRFID = undefined;
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
        },
        
        onClose: function()
        {
            // Sometimes, if the server is slow to respond, then the 
            // typeahead container might still be displayed. If it is present, we simply wish to remove it
            var typeahead = $('.typeahead'); 
            if(typeahead) typeahead.remove();
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
           window.game.on('change:state', this.render, this);
           this.render();
        },
        
        render: function()
        {
           var state = window.game.get('state');
           if(state === 'started' || state === 'overtime')
           $(this.el).html('<button id="cancel-game-button" class="button yellow">Abort</button>');    
           
           else
           $(this.el).html('<button id="start-game-button" class="button green">Start!</button>');
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
    
    Foosball.ClockView = Backbone.View.extend
    ({
        id: 'clock',
        template: _.template($('#clock-template').html()),
       
        initialize: function()
        {
            Foosball.clock.on('change:seconds', this.update, this);
        }, 
        
        render: function()
        {
            this.$el.html(this.template()); 
            this.seconds = this.$el.find('#seconds');
            this.minutes = this.$el.find('#minutes');
            return this;
        },
        
        update: function(clock)
        {
            var s = clock.get('seconds'),
            fseconds = this.format(s%60), 
            fminutes = this.format(parseInt(s/60)); 
            
            this.seconds.html(fseconds);
            this.minutes.html(fminutes);
        },
        
       format: function(value)
       {
            value = value + "";
            if(value.length < 2)
            {
                return "0" + value;
            }
            else
            {
                return value;
            }
       },
       
       onClose:function()
       {
           Foosball.clock.off('change:seconds', this.update, this);
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
            _.bindAll(this, 'render', 'regretGoal', 'update');
            window.game.on('change:home_score change:visitor_score', this.update, this);
        },
        
        render: function()
        {
            // Renders the template for the scoreboard
            this.$el.html(this.template());
            
            // Renders the clock view inside the scoreboard
            this.clock = new Foosball.ClockView();
            this.$el.find('#clock-container').html(this.clock.render().el);
            
            // Fetches refrences to the home-score and visitor-score containers
            // so that we won't have to look for them every time we wish to update them
            this.homeScore = this.$el.find('#home-score');
            this.visitorScore = this.$el.find('#visitor-score');
            
            return this;
        },
        
        update: function(game)
        {
            // If a player scores in-game. We only re-render the score label of the team. 
            // Remember that homeScore is a local refrence to the label that is created 
            // when we first render the scoreboard.
            
            this.homeScore.html(game.get('home_score'));
            this.visitorScore.html(game.get('visitor_score'));
        },
        
        regretGoal: function()
        {
            window.referee.removeGoal();
        },
        
        onClose: function()
        {
           this.clock.close();
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
        el: '#result-container',
        template: _.template($('#result-template').html()),
        events:
        {
            'click #finish-button': 'finishGame',
            'click #restart-button': 'restartGame'
        },
        
        initialize: function()
        {
            _.bindAll(this, 'render', 'finishGame', 'restartGame', 'handleGameState');
            window.game.on('change:state', this.handleGameState);
        },
        
        handleGameState: function(game)
        {
          var state = game.get('state');  
          if(state === 'end')
          {
              this.render();
          }
          else
          {
              this.$el.hide();
          }
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
            
            this.$el.css
            ({
                left: $(window).width() / 2 - this.$el.width() / 2
            });
            
            this.$el.fadeIn();
            return this;
        },
        
        finishGame: function()
        {
            window.game.finish(); 
        },
        
        restartGame: function()
        {
            window.game.restart(); 
        }
    }); 
    
    Foosball.WelcomeView = Backbone.View.extend
    ({
        id: 'welcome',
        template: _.template($('#welcome-template').html()),
        render: function()
        {
           this.$el.html(this.template()); 
           return this;
        }
        
    });
    
    Foosball.BoardView = Backbone.View.extend
    ({
        //el: '#board',    
        id: 'board-content',
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
            var welcomeView = new Foosball.WelcomeView();
            this.show(welcomeView);
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
                this.current.off('close', this.handleGameState, this);   
                this.current.close();
            }
            
            view.on('close', this.handleGameState, this);
            
            this.current = view;
            var self = this.$el;
            self.fadeOut('fast', function(){
                self.html(view.render().el);
                self.fadeIn();
            })
        }
    });
    
    Foosball.MessageView = Backbone.View.extend
    ({
        el: '#messages', 
        template: _.template($('#message-template').html()),
        
        events:
        {   
            'click #close-message' : 'hide',
            'click #message-confirm-option' : 'confirm',
            'click #message-decline-option' : 'decline'
        },
        
        confirm: function()
        {
          this.confirmCallback(); 
          this.hide();
        },
        
        decline: function()
        {
            this.declineCallback(); 
            this.hide();
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
            this.show(data, hold);
        },
        
        showError: function(message, hold)
        {
            var data = 
            {
                message: message,
                type: 'error',
                hold: hold
            };
            
            this.show(data, hold);
        },
        
        showOptionDialog: function(message, confirmCallback, declineCallback)
        {
            var hold = true;
            var data = 
            {
                message: message,
                type: 'option',
                hold: hold
            };
            this.confirmCallback = confirmCallback;
            this.declineCallback = declineCallback;
            this.show(data, hold);
        },
        
        show: function(data, hold)
        {
          var self = this;
          self.hide();
          self.$el.html(self.template(data));
          self.$el.slideDown(150);   
            
            if(!hold)
            {
                self.timer = setTimeout(function() 
                {
                  self.hide();
                }, 4000);
            }
        },
        
        hide: function()
        {
          var self = this;
          self.$el.slideUp(150);
          if(self.timer)
          {
              clearTimeout(this.timer);
              self.timer = undefined;
          }
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
        }
    });
    
    window.game = new Foosball.Game();
    window.referee = new Foosball.Referee();
    Foosball.message = new Foosball.MessageView();
    Foosball.board = new Foosball.BoardView();
    $('#board').html(Foosball.board.render().el);
    Foosball.application = new Foosball.Field(); // TODO: Fiks!
    Foosball.gameMode = new Foosball.GameModeView();
    Foosball.gameState = new Foosball.GameStateView();
    Foosball.results = new Foosball.ResultView();
    Foosball.clock = new Foosball.Clock();
    
    
    
    // Request indicator. 
    // Everytime we make a request to the server, 
    // we want show an indicator until the server responds to us
    $(document).ajaxStart(function()
    { 
        $('#request-indicator').slideDown(250); 
  
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


