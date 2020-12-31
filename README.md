# Dalia

![Unit Tests](https://github.com/modulytic/dalia/workflows/Unit%20Tests/badge.svg)

Dalia is an SMSC written in Java that distributes messages to [endpoints](https://github.com/modulytic/termination-endpoint). It replaces the old Modulytic SMSC, based on [Jasmin](https://github.com/jookies/jasmin), archived [here](https://github.com/modulytic/termination-proxy).

It is named after Dalia from the live-action remake of Aladdin, who [pretended to be Jasmine](https://www.youtube.com/watch?v=PB7M_Tbjggg).

## Setup

You must define the credentials of at least one SMPP user (for ESMEs connecting). To do this, create a htpasswd file called `htpasswd` in the `conf/` directory.

## Usage

After configuring the project, you can start it with docker-compose:

```sh
docker-compose up
```
