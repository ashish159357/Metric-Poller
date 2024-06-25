package org.example.config;

public enum ApplicationConfig {
     SCHEDULER_PERIOD(7),

     //  metric poller should be less than scheduler period
     METRIC_POLLER_TIMEOUT(6);

     public final Integer value;

     private ApplicationConfig(Integer value){
         this.value = value;
     }
}
