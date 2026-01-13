package org.gabrielpadilh4;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class AwsSecretsCredentialStoreTest {

    @Test
    public void testRetrieveSecret() throws Exception {
        SecretsManagerClient mockClient = Mockito.mock(SecretsManagerClient.class);
        GetSecretValueResponse response = GetSecretValueResponse.builder().secretString("test-secret-password").build();

        Mockito.when(mockClient.getSecretValue(Mockito.any(GetSecretValueRequest.class))).thenReturn(response);

        AwsSecretsCredentialStore store = new AwsSecretsCredentialStore();
        Assertions.assertNotNull(response.secretString());
        Assertions.assertEquals("test-secret-password", response.secretString());
    }

}
