# Mobile certificate authentication (FiCom) plugin for Shibboleth IdP v3 - Frontend
[![MIT](https://img.shields.io/npm/l/express.svg?style=flat-square)](https://opensource.org/licenses/MIT)   

## Prerequisities and compilation

- Java 7+
- [Apache Maven 3](https://maven.apache.org/)

```
mvn clean package
```

After successful compilation, the _mobileauth-impl/target_ directory contains _mobileauth-impl-\<version>\-bin.zip_.   
File contains mobile authentication plugin jars + dependencies jars.

## Deployment

After compilation, the _mobileauth-impl-\<version>\-bin.zip_ must be extract to the hard drive and copy all the files to the Shibboleth IdP 3 root directory.

```
mkdir /tmp/mobAuth
cd /tmp/mobAuth
unzip /path/to/the/mobileauth-impl-0.5.1-SNAPSHOT-bin.zip
cd mobileauth-impl-<version>
cp -R * /path/to/the/shibboleth-idp/
cd /path/to/the/shibboleth-idp/bin
./build.sh
```
