package org.hango.cloud.core.plugin.processor.auth;

@SuppressWarnings("java:S115")
public enum AuthTypeEnum {
	JwtAuth("jwt-auth", "jwt_authn_type", "authority"),
	Oauth2Auth("oauth2-auth", "oauth2_authn_type", "authority"),
	SimpleAuth("simple-auth", "proxy.super_authz.authn_policy.token_authn_policy", ""),
	;

	private String kind;
	private String auth_type;
	private String cache_key;

	AuthTypeEnum(final String kind, final String auth_type, final String cache_key) {
		this.kind = kind;
		this.auth_type = auth_type;
		this.cache_key = cache_key;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(final String kind) {
		this.kind = kind;
	}

	public String getAuth_type() {
		return auth_type;
	}

	public void setAuth_type(final String auth_type) {
		this.auth_type = auth_type;
	}

	public String getCache_key() {
		return cache_key;
	}

	public void setCache_key(final String cache_key) {
		this.cache_key = cache_key;
	}

	public static AuthTypeEnum getAuthTypeEnum(String kind) {
		for (AuthTypeEnum authTypeEnum : AuthTypeEnum.values()) {
			if (authTypeEnum.getKind().equals(kind)) {
				return authTypeEnum;
			}
		}
		return null;
	}
}
