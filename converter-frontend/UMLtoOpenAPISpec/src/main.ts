import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app.component';
import { HttpClientModule, provideHttpClient, withInterceptorsFromDi, withFetch } from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { BrowserAnimationsModule, provideAnimations } from "@angular/platform-browser/animations";
import config from './ng-doc';

bootstrapApplication(AppComponent, {
  providers: [importProvidersFrom(HttpClientModule, BrowserAnimationsModule), provideAnimationsAsync()],
});
