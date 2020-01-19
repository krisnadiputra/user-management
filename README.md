# User Management #

An API for user management service

## Build & Run ##

```sh
$ cd user-management
$ sbt
> jetty:start
```

The API runs on [http://localhost:8080/](http://localhost:8080/). You can try it using `curl` or using REST Client app. After it runs, it will create `user-management.db` file inside the folder.

## Tech stacks ##
* Scalatra 2.12.10
* SQLite 3
* Slick 3.2.3

## Assumptions ##
* `users` table schema consists of `id`, `userName`, `emailAddress`, `password`, `createdAt`, `updatedAt`, `blockedAt`, and `version`.
* `userName` and `emailAddress` are unique properties.
* `id` is an integer starts from 1.
* `version` starts from 1 once a new user is created and increments as it gets updated.

## Simplifications ##
* Deletion is a permanent operation.

## Endpoints ##

The prefix for below endpoints is `/api/*`. Some of the endpoints require JSON format body for their parameters.
Method | URI | Description | Body
--- | --- | --- | ---
GET | users/ | To get a list of all users
GET | users/*:id* | To get a specific user data by its *id*
POST | users/signup | To register a new user data into the DB | `userName: STRING`,  `emailAddress: STRING`, and `password: STRING`
POST | users/*:id*/block | To block a specific user by its *id*
POST | users/*:id*/unblock | To unblock a specific user by its *id*
POST | users/*:id*/reset-password | To reset the password of a user by its *id*
PUT | users/*:id* | To update a specific user data by its *id* | At least one of `emailAddress: STRING` and `password: STRING`
DELETE | users/*:id* | To delete a specific user by its *id*
