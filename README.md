
# Project Setup

## Prerequisites
- PostgreSQL database setup.

## Database Setup

1. **Create PostgreSQL Database**

   Create a new database named `mydb`:
   ```sql
   CREATE DATABASE mydb;

2. **Execute the following SQL commands to create the necessary tables**

       CREATE TABLE public.discovery (
       discovery_id serial4 NOT NULL,
       discovery_name varchar NULL,
       credential_id int8 NULL,
       "type" varchar NULL,
       hostname varchar NULL,
       protocol varchar NULL,
       CONSTRAINT discovery_discovery_name_key UNIQUE (discovery_name),
       CONSTRAINT discovery_pk PRIMARY KEY (discovery_id)
       );

    CREATE TABLE public.credential_profile (
    credentialprofilename VARCHAR NULL,
    username VARCHAR NULL,
    "password" VARCHAR NULL,
    credentialprofileid SERIAL4 NOT NULL,
    CONSTRAINT credential_profile_pkey PRIMARY KEY (credentialprofileid)
    );

    CREATE TABLE public.monitor (
    monitor_id serial4 NOT NULL,
    discovery_id int8 NULL,
    hostname varchar NULL,
    protocol varchar NULL,
    username varchar NULL,
    "password" varchar NULL,
    status varchar NULL,
    CONSTRAINT monitor_pk PRIMARY KEY (monitor_id)
    );


Apis :

1. http://localhost:8080/api/v1/discovery/create

request Json :
{
"discoveryId":1,
"discoveryName":"sh",
"ip":"",
"ipRange":"10.20.40.35-37",
"credentialId":"5",
"type": "linux",
"hostname": "ashish",
"protocol": "SSH"
}

2. http://localhost:8080/api/v1/credential-profile/create

request Json :
{
"credentialProfileId": 1,
"credentialProfileName": "akash-linux-profile",
"username": "ashish",
"password": "Mind@123"
}