package com.example.connector.dto;

public class MessageDto {

	private String receieverEmailId;
	private String senderEmailId;
	private Body body;
	private String accessToken;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	

	@Override
	public String toString() {
		return "MessageDto [receieverEmailId=" + receieverEmailId + ", senderEmailId=" + senderEmailId + ", body="
				+ body + ", accessToken=" + accessToken + "]";
	}

	public String getReceieverEmailId() {
		return receieverEmailId;
	}

	public void setReceieverEmailId(String receieverEmailId) {
		this.receieverEmailId = receieverEmailId;
	}

	public String getSenderEmailId() {
		return senderEmailId;
	}

	public void setSenderEmailId(String senderEmailId) {
		this.senderEmailId = senderEmailId;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public static class Body {
		private String content;

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}
}
