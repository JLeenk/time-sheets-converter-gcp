package jleenksystem.dev.timesheetsconverter.services;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;

import jleenksystem.dev.timesheetsconverter.models.secrets.Web;

@Service
public class SecretManagerDevService implements SecretManagerDevServiceI {

	@Value("${secretId}")
	private String secretId;
	
	@Value("${projectId}")
	private String projectId;
	
	@Value("${secretVersion}")
	private String version;
	
	@Override
	public Web getOAuthCredentials() {
		Web webConfig = null;
		try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            String secretName = String.format("projects/%s/secrets/%s/versions/%s", projectId, secretId, version);

            AccessSecretVersionResponse response = client.accessSecretVersion(secretName);
            
            String secretPayload = response.getPayload().getData().toStringUtf8();
            
            ObjectMapper objectMapper = new ObjectMapper();
            
            JsonNode rootNode = objectMapper.readTree(secretPayload);
            JsonNode webNode = rootNode.path("web");

            webConfig = objectMapper.treeToValue(webNode, Web.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return webConfig;
	}

	
}
