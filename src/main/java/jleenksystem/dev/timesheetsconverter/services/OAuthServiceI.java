package jleenksystem.dev.timesheetsconverter.services;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.services.drive.Drive;

import jleenksystem.dev.timesheetsconverter.models.responses.GoogleAuthUrl;

public interface OAuthServiceI {
	GoogleAuthUrl getGoogleAuthUrl();
	String getJavaScriptOrigin();
	String handleOAuthRedirect(String code) throws IOException, GeneralSecurityException;
	Drive getDriverService(String userId) throws IOException, GeneralSecurityException;
}
