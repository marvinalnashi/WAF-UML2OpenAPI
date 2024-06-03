import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SessionListDialogComponent } from './session-list-dialog.component';

describe('SessionListDialogComponent', () => {
  let component: SessionListDialogComponent;
  let fixture: ComponentFixture<SessionListDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SessionListDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SessionListDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
