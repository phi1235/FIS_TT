import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { KeycloakService } from './services/keycloak.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'Angular Keycloak App';
  isAuthenticated = false;
  userProfile: any = null;

  constructor(private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    this.keycloakService.isAuthenticated$.subscribe(isAuth => {
      this.isAuthenticated = isAuth;
      if (isAuth) {
        this.loadUserProfile();
      }
    });
  }

  async loadUserProfile(): Promise<void> {
    this.userProfile = await this.keycloakService.getUserProfile();
  }

  login(): void {
    this.keycloakService.login();
  }

  logout(): void {
    this.keycloakService.logout();
  }
}
