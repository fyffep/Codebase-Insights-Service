package com.insightservice.springboot.model;

import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;

/**
 * TEMPORARY! We may not need this if we use GitHub auth.
 */
public class User //implements UserDetails
{
    @Id
    private String id;
    @NotBlank(message = "Username cannot be blank")
    //@Column(unique = true)
    private String username;
    private String password;

    public User() {
        //Required constructor to be a bean
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    //
    //  REQUIRED METHODS FOR UserDetails
    //
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /*@Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }*/
    //
    //  END REQUIRED METHODS FOR UserDetails
    //
}
