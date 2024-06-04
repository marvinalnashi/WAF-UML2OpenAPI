import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NgDocComponent } from './ng-doc.component';

describe('NgDocComponent', () => {
  let component: NgDocComponent;
  let fixture: ComponentFixture<NgDocComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgDocComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NgDocComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
