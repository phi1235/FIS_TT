package com.example.keycloak.provider;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

/**
 * Adapter để map CustomUser với Keycloak UserModel
 */
public class CustomUserAdapter extends AbstractUserAdapterFederatedStorage {

    private final CustomUser user;
    private final ComponentModel componentModel;

    public CustomUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, CustomUser user) {
        super(session, realm, model);
        this.user = user;
        this.componentModel = model;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getFirstName() {
        return user.getFirstName();
    }

    @Override
    public String getLastName() {
        return user.getLastName();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    @Override
    public String getId() {
        return StorageId.keycloakId(componentModel, user.getId());
    }

    @Override
    public void setEmail(String email) {
        user.setEmail(email);
    }

    @Override
    public void setFirstName(String firstName) {
        user.setFirstName(firstName);
    }

    @Override
    public void setLastName(String lastName) {
        user.setLastName(lastName);
    }

    @Override
    public void setEnabled(boolean enabled) {
        user.setEnabled(enabled);
    }

    @Override
    public void setUsername(String username) {
        user.setUsername(username);
    }
}

