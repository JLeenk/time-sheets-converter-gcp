package jleenksystem.dev.timesheetsconverter.services;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.drive.Drive;

import jleenksystem.dev.timesheetsconverter.models.responses.GoogleAuthUrl;
import jleenksystem.dev.timesheetsconverter.models.secrets.Web;

@Service
public class OAuthService implements OAuthServiceI {

	private final GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow;

	private final Web webConfig;

	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

	public OAuthService(SecretManagerDevServiceI secretManagerDevService) throws IOException {
		MemoryDataStoreFactory dataStoreFactory = MemoryDataStoreFactory.getDefaultInstance();
		DataStore<StoredCredential> dataStore = dataStoreFactory.getDataStore("storedCredentials");
		HttpTransport httpTransport = new NetHttpTransport();
		webConfig = secretManagerDevService.getOAuthCredentials();
		this.googleAuthorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, 
				JSON_FACTORY,
				webConfig.getClientId(), 
				webConfig.getClientSecret(), 
				Arrays.asList(webConfig.getScopes()))
				.setCredentialDataStore(dataStore)
				.setAccessType("offline") // To receive a refresh token
				.build();
	}

	@Override
	public GoogleAuthUrl getGoogleAuthUrl() {
		return new GoogleAuthUrl(UriComponentsBuilder.fromUriString(webConfig.getAuthUri())
				.queryParam("response_type", "code").queryParam("client_id", webConfig.getClientId())
				.queryParam("redirect_uri", webConfig.getRedirectUris()[webConfig.getRedirectUrisPos()])
				.queryParam("scope", String.join(" ", webConfig.getScopes())).queryParam("access_type", "offline")
				.queryParam("prompt", "consent").toUriString());
	}

	@Override
	public String getJavaScriptOrigin() {
		return webConfig.getJavascriptOrigins()[webConfig.getRedirectUrisPos()];
	}

	@Override
	public String handleOAuthRedirect(String code) throws IOException, GeneralSecurityException {
		// Exchange the authorization code for a token
		GoogleTokenResponse tokenResponse = googleAuthorizationCodeFlow.newTokenRequest(code)
				.setRedirectUri(webConfig.getRedirectUris()[webConfig.getRedirectUrisPos()]).execute();

//		String accessTokenValue = tokenResponse.getAccessToken();

//		HttpRequestInitializer requestInitializer = request -> {
//			request.setHeaders(request.getHeaders().setAuthorization("Bearer " + accessTokenValue));
//		};

//		Oauth2 oauth2 = new Oauth2.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY,
//				requestInitializer).setApplicationName(webConfig.getAppName()).build();

//		Userinfo userInfo = oauth2.userinfo().get().execute();
		UUID uuid = UUID.randomUUID();

		googleAuthorizationCodeFlow.createAndStoreCredential(tokenResponse, uuid.toString());
		
		return uuid.toString();
	}

	@Override
	public Drive getDriverService(String userId) throws IOException, GeneralSecurityException {

		Credential credential = googleAuthorizationCodeFlow.loadCredential(userId);

		return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
				.setApplicationName(webConfig.getAppName()).build();

	}
}
