package org.gabrielpadilh4;

import org.wildfly.security.WildFlyElytronBaseProvider;

public class AwsSecretsCredentialStoreProvider extends WildFlyElytronBaseProvider {

    private static AwsSecretsCredentialStoreProvider INSTANCE = new AwsSecretsCredentialStoreProvider();

    public AwsSecretsCredentialStoreProvider() {
        super("AWS Secrets Credential Store Provider", "1.0", "AWS Secrets Manager Elytron Provider");
        putService(new Service(this, "CredentialStore", "AwsSecretsCredentialStore","org.gabrielpadilh4.AwsSecretsCredentialStore", null, null));
    }

    public static AwsSecretsCredentialStoreProvider getInstance() {
        return INSTANCE;
    }
}