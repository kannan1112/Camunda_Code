package com.surge.digital.connector.model;

public class MessageDto {

  private String receieverEmailId;
  private String senderEmailId;
  private Body body;
  private String AccessToken;

  public String getAccessToken() {
    return AccessToken;
  }

  public void setAccessToken(String accessToken) {
    AccessToken = accessToken;
  }

  //  @Override
  //  public String toString() {
  //    return "MessageDto [receieverEmailId="
  //        + receieverEmailId
  //        + ", senderEmailId="
  //        + senderEmailId
  //        + ", body="
  //        + body
  //        + "]";
  //  }

  public String getReceieverEmailId() {
    return receieverEmailId;
  }

  @Override
  public String toString() {
    return "MessageDto [receieverEmailId="
        + receieverEmailId
        + ", senderEmailId="
        + senderEmailId
        + ", body="
        + body
        + ", AccessToken="
        + AccessToken
        + "]";
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
