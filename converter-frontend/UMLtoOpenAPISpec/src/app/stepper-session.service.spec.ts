import { TestBed } from '@angular/core/testing';

import { StepperSessionService } from './stepper-session.service';

describe('StepperSessionService', () => {
  let service: StepperSessionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StepperSessionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
