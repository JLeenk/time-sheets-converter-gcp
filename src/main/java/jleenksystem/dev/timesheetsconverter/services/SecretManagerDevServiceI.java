package jleenksystem.dev.timesheetsconverter.services;

import jleenksystem.dev.timesheetsconverter.models.secrets.Web;

public interface SecretManagerDevServiceI {
	Web getOAuthCredentials();
}
