# Deployment

## Prerequisites

* Access to an user account on the target machine
* Public key configured on the target machine
* User needs to be part of the group `deployment`

### Database setup

In case of incompatible database changes, you need to remove all database tables **before** the deployment.
Connecting the database to IntelliJ is recommended.
Database credentials can be obtained from `Josef Fuchshuber`, `Florian Engel` or `Franz Wimmer`.

## Deployment script

Usage:

```
$ ./deploy.sh [-p] <username>
```

This will deploy to `TEST` [https://test.lokaler.kaufen](https://test.lokaler.kaufen).

The `-p` parameter changes the deployment target to `PROD` [https://demo.lokaler.kaufen](https://demo.lokaler.kaufen).
.
