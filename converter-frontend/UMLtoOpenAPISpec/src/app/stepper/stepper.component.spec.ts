import { TestBed, ComponentFixture } from '@angular/core/testing';
import { StepperComponent } from './stepper.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { GenerationService } from '../generation.service';
import { MockServerService } from '../mock-server.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('StepperComponent', () => {
  let component: StepperComponent;
  let fixture: ComponentFixture<StepperComponent>;
  let generationService: jasmine.SpyObj<GenerationService>;
  let mockServerService: jasmine.SpyObj<MockServerService>;

  beforeEach(async () => {
    const generationServiceSpy = jasmine.createSpyObj('GenerationService', ['parseDiagramElements', 'generateSpec']);
    const mockServerServiceSpy = jasmine.createSpyObj('MockServerService', ['toggleMockServer']);

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        FormsModule,
        BrowserAnimationsModule,
        StepperComponent
      ],
      providers: [
        { provide: GenerationService, useValue: generationServiceSpy },
        { provide: MockServerService, useValue: mockServerServiceSpy }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();

    generationService = TestBed.inject(GenerationService) as jasmine.SpyObj<GenerationService>;
    mockServerService = TestBed.inject(MockServerService) as jasmine.SpyObj<MockServerService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StepperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  // it('should read file on file selected', () => {
  //   const file = new File(['test content'], 'test.xml', { type: 'text/xml' });
  //   const event = { target: { files: [file] } } as unknown as Event;
  //
  //   generationService.parseDiagramElements.and.returnValue(of({}));
  //   spyOn(component, 'readFile').and.callThrough();
  //
  //   component.onFileSelected(event);
  //   expect(component.readFile).toHaveBeenCalledWith(file);
  //   expect(component.uploadedFile).toBe(file);
  // });
});
