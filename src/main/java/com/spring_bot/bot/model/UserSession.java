package com.spring_bot.bot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_session")
public class UserSession {

    @Id
    @Column("id")
    private Long id;

    @Column("chat_id")
    private Long chatId;

    @Column("locale")
    private String locale;

    @Column("first_name")
    private String firstName;

    @Column("user_state")
    private UserState userState;

    @Column("selected_expert_id")
    private String selectedExpertId;

    @Column("is_promo_activated")
    private Boolean isPromoActivated;

    public Boolean getIsPromoActivated() {
        return isPromoActivated;
    }

    public void setIsPromoActivated(Boolean isPromoActivated) {
        this.isPromoActivated = isPromoActivated;
    }

    public UserState getUserState() {
        return userState;
    }

    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    public String getSelectedExpertId() {
        return selectedExpertId;
    }

    public void setSelectedExpertId(String selectedExperId) {
        this.selectedExpertId = selectedExperId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}