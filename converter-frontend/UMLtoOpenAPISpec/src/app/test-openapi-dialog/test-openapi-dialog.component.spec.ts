import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestOpenapiDialogComponent } from './test-openapi-dialog.component';

describe('TestOpenapiDialogComponent', () => {
  let component: TestOpenapiDialogComponent;
  let fixture: ComponentFixture<TestOpenapiDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestOpenapiDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TestOpenapiDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
