import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonaliseComponent } from './personalise.component';

describe('PersonaliseComponent', () => {
  let component: PersonaliseComponent;
  let fixture: ComponentFixture<PersonaliseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonaliseComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PersonaliseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
