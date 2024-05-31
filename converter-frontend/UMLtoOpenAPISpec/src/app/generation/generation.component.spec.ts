import { TestBed } from '@angular/core/testing';
import { GenerationComponent } from './generation.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';

describe('GenerationComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, ReactiveFormsModule, GenerationComponent]
    }).compileComponents();
  });

  it('should create the component', () => {
    const fixture = TestBed.createComponent(GenerationComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
