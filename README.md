![fam logo](http://facility-access-manager.com/images/fam_logo_free_blue.png)

This is an online access manager for all kind of facilities

* [Official homepage](http://facility-access-manager.com)
* [Demo](http://facility-access-manager.com/fam-core)
* [JavaDoc](http://facility-access-manager.com/docs)


README Contents
---------------

1. [License](#a)
2. [System requirements](#b)
  * [System requirements - server](#b-a)
  * [System requirements - client](#b-b)
  * [System requirements - developer](#b-c)
3. [Install](#c)
  * [Main](#c-a)
  * [Plugins](#c-b)
4. [Configuration](#d)
  * [In General](#d-a)
  * [Facilities](#d-b)
  * [Job Data](#d-c)
  * [Cronjobs](#d-d)
  * [Templates](#d-e)
  * [Mails](#d-g)
  * [Roles and Rights](#d-h)

<!-- TODO #26 uncomment

5. [Update Instructions](#z)
  * [1.8.1 → 1.8.2](#z-a)

-->


<a name="a"/>
License
-------

Copyright 2009-2012 by KNURT Systeme ([http://www.knurt.de](http://www.knurt.de))

Licensed under the Creative Commons License Attribution-NonCommercial-ShareAlike 3.0 Unported;

You may not use the Facility Access Manager except in compliance with the License.

You may obtain a copy of the License at

[http://creativecommons.org/licenses/by-nc-sa/3.0](http://creativecommons.org/licenses/by-nc-sa/3.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.

[Get a commercial license](http://facility-access-manager.com/download-the-facility-access-manager.html)


<a name="b"/>
System requirements
-------------------
Server and client (computer of user) have to meet certain requirements in order to ensure an ideal and accurate performance of the software. 

<a name="b-a"/>
### System requirements - server

* [Linux](http://www.linux.org)
* [Java 6](http://java.sun.com/javase/6) (5 won't work, 7 not tested yet)
* [MySQL 5.0.3 or higher](http://www.mysql.com) (other databases with java support should work as well but are not tested)
* [Apache Tomcat 5.5 or higher] (http://tomcat.apache.org) (other servers should work as well but are not tested)
* [CouchDB 0.10.0 or higher] (http://couchdb.apache.org) 

<a name="b-b"/>
### System requirements - client

An up-to-date browser with activated cookies and JavaScript is required. 

Internet Explorer is not recommended but supported starting at Version 8.

<a name="b-c"/>
### System requirements - developer

You need the [server requirements as described above](#b-a). 

Furthermore:
* [Maven] (http://maven.apache.org)
* [Java-Heinzelmann] (https://github.com/knurtsysteme/java-heinzelmann) See ```pom.xml``` to find out which version.
* on couch works [CouchApp] (http://couchapp.org/)
* on css works [less] (http://lesscss.org) running out of the box (without npm).

... and git.

For testing:
* [Firefox] (http://www.mozilla.org/firefox)

[JMeter](http://jmeter.apache.org) and [Molybdenum](https://www.molyb.org) are shipped with the ```pom.xml```

<a name="c"/>
Install
-------

Unfortunatly the installation is difficult and we do not have an installation script by now. Here is what to do.

<a name="c-a"/>
### Main

1. [Download the latest version](http://facility-access-manager.com/download-the-facility-access-manager.html)
2. Extract the ZIP-File
3. Copy folder ```opt/knurt``` into system folder ```/opt```
4. Ensure tomcat can read all files in ```/opt/knurt/fam``` and can write into folder ```/opt/knurt/fam/files```
5. Edit file ```/opt/knurt/fam/config/fam_global.conf``` as described there. This is the place where to put in all the database connections.
6. Make sure database- and email-driver jars are in your tomcat lib folder.
7. Create CouchDB-Database (as configured in 5.) and push in the design-document ```migration/as.json```
8. Create SQL-Database (as configured in 5.) and push in the sql-file ```migration/init.sql```
9. Install ```webapp/fam-core.war``` to your webapps folder of your server (e.g. ```/usr/share/tomcat6/webapps```)
10. Start up server and application
11. Visit your server-uri. You should see this [http://facility-access-manager.com/fam-core](Demo System) now.
12. Click the Register-Tab and register yourself. You get an email with your username then.
13. Replace the username ```daoltman``` with your username in ```/opt/knurt/fam/config/rolesAndRights.xml``` to be an admin. Add more admins there by adding the usernames comma seperated.
14. Log in as admin

Hopefully that's it! You can check all configuration parameters under Admin → System Configuration.

What you have done is installing the demo system. What you may want to do is configuring the system with your facilities, booking rules, roles and rights. Go to [base Configuration to know how](#d-a)

#### split application

You can install the system on three different servers: One for the public page, one for protected pages and the third one for admin pages. You can also install it on two servers. The only thing to do is to configure the same public, protected and admin url in ```/opt/knurt/fam/config/fam_global.conf```. The protected and admin area do need contact the same databases.


<a name="c-b"/>
### Plugins

Copy your plugins into ```/opt/knurt/fam/plugins``` and restart application.

***WARNING: Plugins can do everything including destroying everything!***

<a name="d"/>
Configuration
-------------

After installing the software there are different places where to configure the system. 

**How to configure the system is described just in place (in the files where to configure it).** Find a short description of where to find what below.

#### XML

The core configuration (roles, facilities, booking rules) is made with XML files. XML stands for "Extensible Markup Language" and simply describe data structures.

In practice, XML bean configurations which are part of the spring framework are particularly used.
XML bean configurations can be very complex and very powerful. Basically all options of XML bean configurations can be used but basic knowledge of XML is sufficient to configure the application.

For a complete description of options of an XML bean configuration visit [the IoC container](http://static.springsource.org/spring/docs/3.0.x/reference/beans.html)

[A good introduction to XML is offered by Wikipedia](http://en.wikipedia.org/wiki/XML)


#### Velocity

The system supports creating own templates. The one and only template by now uses [Velocity](http://velocity.apache.org/). See [Templates](#d-d)-Section for more information.


#### Properties

Properties files are an important mean of internationalization when programming. Although the System so far only supports the English language, several texts are stored in properties files.

The use of properties files is basically the key-value coding (key=this is the value). Concerning emails placeholders are used ({0}, {1}, {2} etc.). Furthermore almost all properties files contain comments starting with a pound sign "#".

During the installation you already worked with it!

The preceding description is sufficient to configure the application. Furthermore [Wikipedia offers a good introduction to properties files](en.wikipedia.org/wiki/.properties)

#### Online-Configuration

Some configurations like the terms, the job data or user's responsibilities are made directly in the system and are described there.

<a name="d-a"/>
### In General

#### The very core configuration ####

Follow the instructions in the file ```/opt/knurt/fam/config/fam_global.conf```. Main settings are for database connections, a hard coded direct access to a full screen booking calendar, the uri to own content (if you like to use own pictures etc), a possibility to use a proxy, the email-server-settings, file-upload settings and so on.


#### Texts concerning the entire application ####

Texts for emails, html header, facilities and logbooks are set in the file ```/opt/knurt/fam/config/lang.properties```.


#### Usually you do not have to care for ####

* ```/opt/knurt/fam/config/loader.xml``` Load the configuration files described here. Configure other files if you want to.
* ```/opt/knurt/fam/config/calendarDefaultViews.xml``` Is deprecated and will be removed in future releases.
* ```/opt/knurt/fam/config/mail.xml``` If you want to have a delay on sending emails.


<a name="d-b"/>
### Facilities

All facilities and booking rules are configured in the files ```/opt/knurt/fam/config/facilities*.xml```.

Every facility can have its own booking rules even seperated for different roles. 

In most cases the booking rule is a simple standard booking rule. In some cases the definitions can get quiet complex. 

Two basically different booking rules can be configured: Either a facility is booked over a calendar ("time based") or you have to join a queue to book a facility ("queue based").

A booking rule might describe things like a label for the smallest single time unit (like "school hour"), specific rights for specific users and most important: The minimal and maximal time to book as well as the minimal and maximal units to book and the total units available.

The demo system provides you with easy and complex definitions described here:

* ```/opt/knurt/fam/config/facilitiesConfigured.xml``` Configured facilities used by the system.
* ```/opt/knurt/fam/config/facilitiesPoolAbstract.xml``` Facility prototypes. Usually you do not have to care for. (change only with extreme wisdom).
* ```/opt/knurt/fam/config/facilitiesPoolBookable.xml``` Concrete bookable facilities.
* ```/opt/knurt/fam/config/facilitiesPoolBookingRulesAbstract.xml``` Booking rules for facilities.
* ```/opt/knurt/fam/config/facilitiesPoolNoneBookable.xml``` Concrete none-bookable facilities.

You have the rules and the attributes for the facilities configured? Now you have to give it a name.

Names are configured in the file ```/opt/knurt/fam/config/lang.properties```. 
Say you have three people to rent and you named this "facility" ```myfoo``` in ```/opt/knurt/fam/config/facilitiesConfigured.xml```.
Three labels must be added in ```lang.properties``` then:

```
# the label shown when user choose the facility to book ("I'd like to book ...")
myfoo.label=Nice People to rent
# the label shown when user book one of it ("I'd like to book 1 ...")
label.capacity.myfoo.singular=Person
# the label shown when user book more then one of it ("I'd like to book 2 ...")
label.capacity.myfoo.plural=People
```

<a name="d-c"/>
### Job Data

What do you want to know from the booker? How do you want to receive the request? What do you want to answer? How do you want to present your answer to the booker? 

These four questions can be answered if you sign into the system, click Admin → Job Data and fill out the forms as described there.

**!!! You must have configured a Job Data for your root facility !!!**  Otherwise booking won't work.


<a name="d-d"/>
### Cronjobs

There are several tasks the software does only if a specific url is visited:

* Send out e-mails with a delay
* Send out an e-mail to the operator if someone apply for a facility the operator is responsible for.
* Send out an e-mail if an user account expires

To get these features run there must be a crontab contacting the uri every 5 minutes.

The easiest solution is ```curl```:
```
[root@localhost ~]# crontab -l
*\/5 * * * * curl http://www.yourdomain.com/fam-core/exec-cronjob.html
```

Alternative ```wget```:
```
[root@localhost ~]# crontab -l
*\/5 * * * * wget -S http://www.yourdomain.com/fam-core/exec-cronjob.html --no-cookies --no-cache -O -
```

In both cases replace ```http://www.yourdomain.com/fam-core``` with your address!


<a name="d-e"/>
### Templates

All template-files can be found in the folder ```/opt/knurt/fam/template```. You can change the behaviour, the style and the structure of the template by adding and changing files in the folder ```/opt/knurt/fam/template/custom```. 
*DO NOT CHANGE FILES IN OTHER DIRECTORIES* because those changes might get lost on updates.

#### overriding a template of the main page
1. copy file from ```content_main/[visibility]/[resource].html``` to ```custom/[resource]_[visibility]_main.html```
2. edit file in ```custom``` directory. 

example: ```cp content_main/public/home.html custom/home_public_main.html```

#### overriding sub content
1. copy file from ```content_sub/[resource].html``` to ```custom/[resource]_sub.html```
2. edit file in ```custom``` directory. 

example: ```cp content_sub/register.html custom/register_sub.html```

#### configure web-analytics
You can configure your web-analytics in the file ```custom/web-analytics.html```.

If you do not want web-analytics, simply kill that file.


#### configure maintenance and page texts

In ```custom/lanuage.xml``` you can configure other things like your company name or a maintenance message. The main part of this file described page specific texts used by the template.

#### define a new page
1. add the page definitions in ```custom/config.xml``` and ```custom/language.xml``` file 
2. copy an existing page ```<page>...</page>``` in both files - think, that is self-explaining.
3. create the content file in ```custom``` directory like described above.
4. create page specific styles in ```styles/[name].css```. 
4. create page specific scripts in ```scripts/[name].js```.

Attention: Styles and scripts are parsed by velocity. You might get difficulties if you are not including your concrete styles. See existing files for more information.

#### change styles and scripts
1. copy a file from directory ```styles``` or ```scripts``` into this directory (without renaming)
2. edit file here
3. files not working are files named "all-....css"

#### override global styles
Override style definition concerning all pages in "all-pages-update.css"

#### defining your letter
1. define letter style ```letter_style.json```
2. define letter structure in ```letter_booking.json```

As you find in the demo json-files, you can use some velocity templates.

#### define require field inputs of registration
1. set configuration in ```custom/ValidationConfiguration.js```
2. do not forget to change the server side definitions in ```rolesAndRights.xml``` (bean with id ```bean2010310903```). there is no need to be equal, but all things missed defined in ```rolesAndRights.xml``` must be filled in by the user later.

You may want define simple things like "must have a value" up to complex custom validation functions here. See the file for more information.

<a name="d-f"/>
### Logbooks

Logbooks is a mix of a lightweight forum, a bug tracker and a social status message. Write something in a logbook and everybody gets a news-message.

You can define logbooks in these files:

* ```/opt/knurt/fam/config/logbooksConfigured.xml``` Configured logbooks used by the system.
* ```/opt/knurt/fam/config/logbooksPool.xml``` Logbooks.

Like defining facilities, you set the label in ```lang.properties```. Say you named it ```mylog``` in the file ```/opt/knurt/fam/config/logbooksPool.xml```:
```
mylog.label=My Logbook
mylog.description=This is a description of the logbook
mylog.tags=notice,problem,request,bug
```

```mylog.tags``` are predefined tags the user can simply click on.

<a name="d-g"/>
### Mails

All mails are defined in ```lang.properties```. Search for ```mail.``` to edit it.


<a name="d-h"/>
### Roles and Rights

Every user has exactly one role. Every role has different rights. By default there are four roles:

* extern: All users not being a member of the institution or company
* intern: Members of the institution or company
* admin: All rights without any restriction
* operator: Members responsible for at least one facility

*Warning: The system has never been tested without these preconfigured roles*

If you want to edit the rights or add roles you have to edit the file ```/opt/knurt/fam/config/rolesAndRights.xml``` as described there.

This file furthermore defines known departments and roles being linked with that departments. And you can define required fields of a user. If one field is missed for a user, the software ask him on the next visit for it.

To give new roles a name you have to edit ```lang.properties```:

```
role.desc.myrole=This role is my role
role.label.myrole= My Role
```

<!-- TODO #26 uncomment
<a name="z"/>
Update Instructions
-------------------

<a name="z-a"/>
### 1.8.1 → 1.8.2
- backup couchdb and template files first
- download full version
- replace template-folders (but custom)
- diff your custom files with new template files. 
  - there are changes caused by #24
  - package ```de.knurt.fam.core.control.persistence``` moved to ```de.knurt.fam.core.persistence```
- push new design document by executing ```migrate_couchdb.sh```
- reinstall fam-core.war
-->
