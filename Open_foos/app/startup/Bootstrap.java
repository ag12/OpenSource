package startup;

import models.Admin;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() {
        if (Admin.count() == 0) {
            Fixtures.loadModels("dependencies.yml");
        }
    }
}
