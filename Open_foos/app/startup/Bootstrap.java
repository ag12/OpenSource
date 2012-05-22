package startup;

import models.Admin;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job {

    @Override
    public void doJob() {
        if (Admin.count() == 0) {
           
            Admin admin = new Admin();
            admin.username = "Admin";
            admin.password = "c5abc7086cea5238aa8b53f36688ad05";
            admin.email = "hovedprosjekt.hioa@gmail.com";
            admin.active = true;
            admin.save();
        }
    }
}
