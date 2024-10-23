package jleenksystem.dev.timesheetsconverter.models.secrets;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Web {
	
	@JsonProperty("app_name")
	private String appName;
	
	@JsonProperty("client_id")
	private String clientId;
	
	@JsonProperty("project_id")
	private String projectId;
	
	@JsonProperty("auth_uri")
	private String authUri;

	@JsonProperty("token_uri")
	private String tokenUri;

	@JsonProperty("auth_provider_x509_cert_url")
	private String authProvider;

	@JsonProperty("client_secret")
	private String clientSecret;
	
	@JsonProperty("redirect_uris_pos")
	private Integer redirectUrisPos;
	
	@JsonProperty("redirect_uris")
	private String[] redirectUris;
	
	@JsonProperty("javascript_origins")
	private String[] javascriptOrigins;
	
	@JsonProperty("scopes")
	private String[] scopes;
}
