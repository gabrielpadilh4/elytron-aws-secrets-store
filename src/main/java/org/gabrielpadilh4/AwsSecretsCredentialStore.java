package org.gabrielpadilh4;

import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.credential.store.CredentialStore;
import org.wildfly.security.credential.store.CredentialStoreException;
import org.wildfly.security.credential.store.CredentialStoreSpi;
import org.wildfly.security.credential.store.UnsupportedCredentialTypeException;
import org.wildfly.security.password.interfaces.ClearPassword;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Map;

public class AwsSecretsCredentialStore extends CredentialStoreSpi {

    private SecretsManagerClient awsSecretManagerClient;

    @Override
    public void initialize(Map<String, String> attributes, CredentialStore.ProtectionParameter protectionParameter, Provider[] providers) throws CredentialStoreException {

        try {
            this.awsSecretManagerClient = SecretsManagerClient
                    .builder()
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

        } catch (Exception e) {
            throw new CredentialStoreException("Failed to initialize AWS Secrets Manager Client. Error: " + e.getMessage());
        }
    }

    @Override
    public <C extends Credential> C retrieve(String credentialAlias, Class<C> credentialType, String credentialAlgorithm, AlgorithmParameterSpec parameterSpec, CredentialStore.ProtectionParameter protectionParameter) throws CredentialStoreException {

        if (!credentialType.isAssignableFrom(PasswordCredential.class)) {
            throw new UnsupportedCredentialTypeException("Only PasswordCredential is supported!");
        }

        GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(credentialAlias).build();

        try {
            String secretValue = awsSecretManagerClient.getSecretValue(request).secretString();

            ClearPassword password = ClearPassword.createRaw(ClearPassword.ALGORITHM_CLEAR, secretValue.toCharArray());

            return credentialType.cast(new PasswordCredential(password));
        } catch (Exception e) {
            throw new CredentialStoreException("Failed to obtain secret from AWS Secrets Manager for alias: '" + credentialAlias + "' Error: " + e.getMessage());
        }
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public void store(String s, Credential credential, CredentialStore.ProtectionParameter protectionParameter) throws CredentialStoreException, UnsupportedCredentialTypeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(String s, Class<? extends Credential> aClass, String s1, AlgorithmParameterSpec algorithmParameterSpec) throws CredentialStoreException {
        throw new UnsupportedOperationException();
    }
}
