import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { BehaviorSubject, Observable } from 'rxjs';

export interface KeycloakConfig {
  url: string;
  realm: string;
  clientId: string;
}

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {
  private keycloak: Keycloak | undefined;
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$: Observable<boolean> = this.isAuthenticatedSubject.asObservable();

  // Keycloak configuration - Update these values according to your Keycloak setup
  private keycloakConfig: KeycloakConfig = {
    url: 'http://localhost:8080', // Keycloak server URL
    realm: 'phi-realm', // Your realm name
    clientId: 'angular-app' // Your client ID
  };

  constructor() {
    this.initKeycloak();
  }

  /**
   * Initialize Keycloak
   */
  async initKeycloak(): Promise<boolean> {
    try {
      this.keycloak = new Keycloak({
        url: this.keycloakConfig.url,
        realm: this.keycloakConfig.realm,
        clientId: this.keycloakConfig.clientId
      });

      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso', // 'login-required' or 'check-sso'
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
        pkceMethod: 'S256',
        checkLoginIframe: false
      });

      this.isAuthenticatedSubject.next(authenticated);

      if (authenticated) {
        console.log('User is authenticated');
        this.setupTokenRefresh();
      }

      return authenticated;
    } catch (error) {
      console.error('Failed to initialize Keycloak', error);
      return false;
    }
  }

  /**
   * Login user
   */
  login(): void {
    if (this.keycloak) {
      this.keycloak.login();
    }
  }

  /**
   * Logout user
   */
  logout(): void {
    if (this.keycloak) {
      this.keycloak.logout();
    }
  }

  /**
   * Get access token
   */
  getToken(): string | undefined {
    return this.keycloak?.token;
  }

  /**
   * Get refresh token
   */
  getRefreshToken(): string | undefined {
    return this.keycloak?.refreshToken;
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.keycloak?.authenticated ?? false;
  }

  /**
   * Get user profile
   */
  async getUserProfile(): Promise<any> {
    if (this.keycloak?.authenticated) {
      try {
        return await this.keycloak.loadUserProfile();
      } catch (error) {
        console.error('Failed to load user profile', error);
        return null;
      }
    }
    return null;
  }

  /**
   * Get user roles
   */
  getUserRoles(): string[] {
    if (this.keycloak?.tokenParsed) {
      const token = this.keycloak.tokenParsed as any;
      return token.realm_access?.roles || [];
    }
    return [];
  }

  /**
   * Check if user has role
   */
  hasRole(role: string): boolean {
    return this.getUserRoles().includes(role);
  }

  /**
   * Update token
   */
  async updateToken(minValidity: number = 5): Promise<boolean> {
    if (this.keycloak) {
      try {
        return await this.keycloak.updateToken(minValidity);
      } catch (error) {
        console.error('Failed to update token', error);
        return false;
      }
    }
    return false;
  }

  /**
   * Setup automatic token refresh
   */
  private setupTokenRefresh(): void {
    if (this.keycloak) {
      setInterval(() => {
        this.updateToken(30).then((refreshed) => {
          if (refreshed) {
            console.log('Token refreshed');
          }
        });
      }, 60000); // Check every minute
    }
  }

  /**
   * Register user
   */
  register(): void {
    if (this.keycloak) {
      this.keycloak.register();
    }
  }

  /**
   * Get account management URL
   */
  accountManagement(): void {
    if (this.keycloak) {
      this.keycloak.accountManagement();
    }
  }
}
