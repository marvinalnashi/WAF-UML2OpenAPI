import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddElementDialogComponent } from './add-element-dialog.component';

describe('AddElementDialogComponent', () => {
  let component: AddElementDialogComponent;
  let fixture: ComponentFixture<AddElementDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddElementDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddElementDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
