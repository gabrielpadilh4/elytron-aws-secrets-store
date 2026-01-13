package org.gabrielpadilh4;

import org.wildfly.security.credential.Credential;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.credential.store.CredentialStore;
import org.wildfly.security.credential.store.CredentialStoreException;
import org.wildfly.security.credential.store.CredentialStoreSpi;
import org.wildfly.security.credential.store.UnsupportedCredentialTypeException;
import org.wildfly.security.password.interfaces.ClearPassword;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Map;

public class AwsSecretsCredentialStore extends CredentialStoreSpi {

    private static final String AWS_PROFILE_PARAMETER = "AWS_PROFILE";
    private static final String AWS_REGION_PARAMETER = "AWS_REGION";

    private SecretsManagerClient awsSecretManagerClient;

    @Override
    public void initialize(Map<String, String> attributes, CredentialStore.ProtectionParameter protectionParameter, Provider[] providers) throws CredentialStoreException {

        if (attributes.isEmpty() || !attributes.containsKey(AWS_PROFILE_PARAMETER)) {
            throw new CredentialStoreException("Missing configuration for the 'AWS_PROFILE'.");
        }

        if (attributes.isEmpty() || !attributes.containsKey(AWS_REGION_PARAMETER)) {
            throw new CredentialStoreException("Missing configuration for the 'AWS_REGION'.");
        }

        String awsProfile = attributes.get(AWS_PROFILE_PARAMETER);
        String awsRegion = attributes.get(AWS_REGION_PARAMETER);

        try {
            Region region = Region.of(awsRegion.toUpperCase());

            this.awsSecretManagerClient = SecretsManagerClient
                    .builder()
                    .region(region)
                    .credentialsProvider(ProfileCredentialsProvider.create(awsProfile))
                    .build();

        } catch (Exception e) {
            throw new CredentialStoreException("Failed to initialize AWS Secrets Manager Client. Error: " + e.getMessage());
        }
    }

    @Override
    public <C extends Credential> C retrieve(String credentialAlias, Class<C> credentialType, String credentialAlgorithm, AlgorithmParameterSpec parameterSpec, CredentialStore.ProtectionParameter protectionParameter) throws CredentialStoreException {

        if (credentialType.isAssignableFrom(PasswordCredential.class)) {
            GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(credentialAlias).build();
            try {
                String secretValue = awsSecretManagerClient.getSecretValue(request).secretString();

                ClearPassword password = ClearPassword.createRaw("clear", secretValue.toCharArray());

                return credentialType.cast(new PasswordCredential(password));
            } catch (Exception e) {
                throw new CredentialStoreException("Failed to obtain secret from AWS Secrets Manager. Error: " + e.getMessage());
            }
        }

        throw new CredentialStoreException("Credential not found or type mismatch");
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
