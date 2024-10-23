package jleenksystem.dev.timesheetsconverter.controllers;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jleenksystem.dev.timesheetsconverter.models.responses.GoogleAuthUrl;
import jleenksystem.dev.timesheetsconverter.services.OAuthServiceI;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OAuthController {

	private final OAuthServiceI authService;
	
	@GetMapping("/googleAuthUrl")
	public GoogleAuthUrl googleAuthUrl() {
		return authService.getGoogleAuthUrl();
	}
	
	@GetMapping("/oauth/callback")
	public void handleOAuthRedirect(String code, HttpServletResponse response) {
		String origin = authService.getJavaScriptOrigin();
		try {
			String token = authService.handleOAuthRedirect(code);
			response.sendRedirect(String.format("%s/manager?token=%s", origin, token));
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			try {
				response.sendRedirect(String.format("%s/failure", origin) );
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
