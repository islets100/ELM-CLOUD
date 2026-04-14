package team.tjusw.elm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JwtTokenDto {
	private String idToken;

	public JwtTokenDto(String idToken) {
		this.idToken = idToken;
	}

	@JsonProperty("id_token")
	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}
}

