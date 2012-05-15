import play.*;
import play.jobs.*;
import play.test.*;

import models.*;

@OnApplicationStart
public class Bootstrap extends Job {
    
    @Override
    public void doJob(){
       
        if(Admin.count() == 0)
            Fixtures.loadModels("start-data.yml");

    }
    
}
