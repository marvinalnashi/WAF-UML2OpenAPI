import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideAnimations } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';

export const toastrProviders: ApplicationConfig['providers'] = [
  importProvidersFrom(
    ToastrModule.forRoot({
      timeOut: 3000,
      positionClass: 'toast-top-right',
      preventDuplicates: true,
      closeButton: true,
      progressBar: true,
      tapToDismiss: true,
      newestOnTop: true,
      maxOpened: 5,
      autoDismiss: true,
      easeTime: 300,
      extendedTimeOut: 1000,
    })
  ),
  provideAnimations()
];
