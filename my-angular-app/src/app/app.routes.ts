import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { SignPageComponent } from './sign-page/sign-page.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'home', component: HomeComponent },
  { path: 'sign', component: SignPageComponent },
  { path: '**', redirectTo: 'home' } // redirect về home nếu không tìm thấy route
];
