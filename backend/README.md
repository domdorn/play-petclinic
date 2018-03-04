# play-petclinic

This application is a implementation of the "PetClinic" Application known from other application frameworks. 

It consists of multible bounded-contexts (auth, billing, petclinic). Each bounded-context creates a docker image and can thus be deployed as a microservice. 
To have a great local development experience, all those microservices are started when running `sbt run`. They can be accessed using different hostnames.

## /etc/hosts
It is recommended to add these hostnames to your /etc/hosts

127.0.0.1	auth.test	
127.0.0.1	billing.test	
127.0.0.1	petclinic.test	


## Best Practices for Blocking API

