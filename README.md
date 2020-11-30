# dalia

Dalia is an SMSC written in Java that will distribute messages to [endpoints](https://github.com/modulytic/termination-endpoint). It is named after Dalia from the live-action remake of Aladdin, because it takes the place of [Jasmin](https://github.com/jookies/jasmin) like she [took the place of Jasmine](https://www.youtube.com/watch?v=PB7M_Tbjggg).

The old, Jasmin-based SMSC is archived [here](https://github.com/modulytic/termination-proxy).

## Setup

You must define the credentials of at least one SMPP user (for ESMEs connecting). To do this, create a file `smpp_users.json` in the `conf/` directory. The file should follow this template:

```json
{
    "user1": "pass1",
    "user2": "pass2"
}
```

Note that this is **NOT** the same as the old SMSC! If you copy the old file directly, it **WILL NOT** work.

## Usage

After configuring the project, you can start it with docker-compose:

```sh
docker-compose up
```
