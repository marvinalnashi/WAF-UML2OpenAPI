import { TestBed, ComponentFixture } from '@angular/core/testing';
import { PersonaliseComponent } from './personalise.component';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {ToastrModule, ToastrService} from "ngx-toastr";

describe('PersonaliseComponent', () => {
  let component: PersonaliseComponent;
  let fixture: ComponentFixture<PersonaliseComponent>;
  let dialogSpy: jasmine.Spy;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        MatDialogModule,
        BrowserAnimationsModule,
        PersonaliseComponent,
        ToastrModule.forRoot()
      ],
      providers: [
        {
          provide: MatDialog,
          useValue: {
            open: () => ({
              afterClosed: () => ({
                subscribe: (fn: (value: any) => void) => fn({ newValue: 'RenamedExample' })
              })
            })
          }
        },
        {
          provide: ToastrService,
          useValue: jasmine.createSpyObj('ToastrService', ['success', 'error', 'warning', 'info'])
        }
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PersonaliseComponent);
    component = fixture.componentInstance;
    dialogSpy = spyOn(TestBed.inject(MatDialog), 'open').and.callThrough();
    component.openApiData = {
      components: {
        schemas: {
          TestClass: {
            properties: {
              attr1: {},
              attr2: {}
            }
          }
        }
      }
    };
    component.selectedClassAttributes = ['attr1', 'attr2'];
    component.selectedAttributeExamples = ['example1', 'example2'];
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should select a class and load attributes', () => {
    component.ngOnChanges({
      openApiData: {
        currentValue: component.openApiData,
        previousValue: {},
        firstChange: true,
        isFirstChange: () => true
      }
    });
    component.selectClass('TestClass');
    fixture.detectChanges();
    expect(component.selectedClass).toBe('TestClass');
    expect(component.selectedClassAttributes.length).toBe(2);
  });

  it('should handle example value updates', () => {
    component.selectClass('TestClass');
    fixture.detectChanges();

    component.selectAttribute('attr1');
    fixture.detectChanges();

    component.selectedAttributeExamples = ['example1', 'example2'];
    fixture.detectChanges();

    component.selectedAttributeExamples[0] = 'UpdatedExample';
    fixture.detectChanges();

    expect(component.selectedAttributeExamples[0]).toBe('UpdatedExample');
  });
});
