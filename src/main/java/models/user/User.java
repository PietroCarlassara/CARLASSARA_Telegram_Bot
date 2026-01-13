package models.user;

import models.enumerable.UserState;

public class User {
    private long chatId;
    private String firstName;
    private String lastName;
    private String username;
    private String language;

    private UserState state;

    public User(long chatId, String firstName, String lastName, String username, String language) {
        this.setChatId(chatId);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setUsername(username);
        this.setLanguage(language);
        this.setState(UserState.NONE);
    }

    // GET
    public long getChatId()         { return this.chatId; }
    public String getFirstName()    { return this.firstName; }
    public String getLastName()     { return this.lastName; }
    public String getUsername()     { return this.username; }
    public String getLanguage()     { return this.language; }
    public UserState getState()     { return state; }

    // SET
    public void setChatId(long chatId)          { this.chatId = chatId; }
    public void setFirstName(String firstName)  { this.firstName = firstName; }
    public void setLastName(String lastName)    { this.lastName = lastName; }
    public void setUsername(String username)    { this.username = username; }
    public void setLanguage(String language)    { this.language = language.toUpperCase(); }
    public void setState(UserState state)       { this.state = state; }
}