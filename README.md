# elytron-aws-secrets-store

A custom **WildFly Elytron** `CredentialStore` implementation that integrates with **AWS Secrets Manager** via AWS Java SDK.

This module allows WildFly to dynamically fetch sensitive credentials (like database passwords, keystores and more) directly from AWS, eliminating the need to store clear-text passwords in the configuration `XML` or local files.

---

## ✨ Features

* **Native Elytron Integration**: Works seamlessly with any WildFly subsystem that integrates with Elytron credential store(Datasources, Mail, Messaging, Elytron, etc.).
* **Secure by Design**: Credentials remain in AWS; only references (aliases) are stored locally.
* **IAM Ready**: Leverages the [Default AWS Credentials Provider](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started-auth.html) (supports EC2 Instance Profiles, EKS Pod Identities, and local profiles).

## 🛠 Prerequisites

- AWS credentials configured. See the official [sdk-for-java](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started-auth.html) for detailed instructions.
- WildFly / JBoss EAP installed.
- The [elytron-aws-secrets-store-1.0-SNAPSHOT.jar](https://github.com/gabrielpadilh4/elytron-aws-secrets-store/releases/download/1.0.0/elytron-aws-secrets-store-1.0-SNAPSHOT.jar)
---

## ⚙️ Usage Example

Configure an existing data source (replace PostgresDS with your actual data source name) to fetch its password from AWS Secrets Manager store using the "alias" with your AWS secret name.
~~~
/subsystem=datasources/data-source=PostgresDS:write-attribute(name=credential-reference, value={store=AwsSecretsCredentialStore, alias="prod/db/password"})
reload
~~~

In the above example "prod/db/password" is the name of my secret that holds the password for the database in AWS Secrets Manager.

It can be seen as follows using AWS cli:
```bash
$ aws secretsmanager create-secret --name "prod/db/password" --secret-string 'thisismysecuredbpassword' --profile localstack
{
    "ARN": "arn:aws:secretsmanager:us-east-1:000000000000:secret:prod/db/password-DrjuOs",
    "Name": "prod/db/password",
    "VersionId": "b0c6d7bf-8540-4d5a-8e17-9aed408e898e"
}
$
$ aws secretsmanager list-secrets --profile localstack
{
    "SecretList": [
        {
            "ARN": "arn:aws:secretsmanager:us-east-1:000000000000:secret:prod/db/password-DrjuOs",
            "Name": "prod/db/password",
            "LastChangedDate": "2026-01-13T10:43:13.039013-03:00",
            "SecretVersionsToStages": {
                "b0c6d7bf-8540-4d5a-8e17-9aed408e898e": [
                    "AWSCURRENT"
                ]
            },
            "CreatedDate": "2026-01-13T10:43:13.039013-03:00"
        }
    ]
}
```


## 🚀 Installation and Deployment - via jboss-cli.sh

1.Connect to Wildfly/JBoss via jboss-cli.sh
~~~
cd $WILDFLY_HOME
./bin/jboss-cli.sh --connect
~~~

2.Add the Elytron AWS Secrets Jar module to Wildfly / JBoss EAP
~~~
module add --name=elytron-aws-secrets-store --resources=/path/to/jar/elytron-aws-secrets-store-1.0-SNAPSHOT.jar --dependencies=org.wildfly.security.elytron,org.slf4j,org.jboss.logging
~~~
Restart the Wildfly/JBoss instance after adding the module.

3. Configure and Register the Provider
Register the custom provider so Elytron can recognize the AWS implementation:
~~~
/subsystem=elytron/provider-loader=AwsSecretsCredentialStoreProvider:add(class-names=[org.gabrielpadilh4.AwsSecretsCredentialStoreProvider], module=elytron-aws-secrets-store)
/subsystem=elytron:write-attribute(name=initial-providers, value=AwsSecretsCredentialStoreProvider)
reload
~~~

4. Create the credential store instance(make sure to set the AWS_REGION property to your desired value)
~~~
/subsystem=elytron/credential-store=AwsSecretsCredentialStore:add(providers=AwsSecretsCredentialStoreProvider,credential-reference={clear-text=''}, type=AwsSecretsCredentialStore)
~~~
Note that `clear-text` value will not be used. I have left it empty because is mandatory. In my example `AWS_PROFILE` is the one configured under `~/.aws/config`

5. Pass the Java properties for the Java SDK. E.g for referencing a `profile`
~~~
cd $WILDFLY_HOME
./bin/standalone.sh -Daws.profile=localstack
~~~
Where `localstack` is a profile under `~/.aws/config`. Check the [JVM settings reference for all system properties options](https://docs.aws.amazon.com/sdkref/latest/guide/settings-reference.html#JVMSettings)
