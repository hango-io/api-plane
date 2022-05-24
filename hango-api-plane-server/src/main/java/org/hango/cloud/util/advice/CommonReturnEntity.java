package org.hango.cloud.util.advice;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class CommonReturnEntity {

	@JsonProperty("RequestId")
	private String requestId;
	
	@JsonProperty("Message")
	private String message;
	
	@JsonProperty("Code")
	private String code;
	
	public CommonReturnEntity() {}
	
	public CommonReturnEntity(Builder builder) {
		this.requestId = builder.requestId;
		this.message = builder.message;
		this.code = builder.code;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}


	static class Builder {
		
		private String requestId;
		
		private String message;
		
		private String code;

		public Builder requestId(String requestId) {
			this.requestId = requestId;
			return this;
		}
		public Builder message(String message) {
			this.message = message;
			return this;
		}
		public Builder code(String code) {
			this.code = code;
			return this;
		}
		public CommonReturnEntity build() {
			return new CommonReturnEntity(this);
		}
	}
}
