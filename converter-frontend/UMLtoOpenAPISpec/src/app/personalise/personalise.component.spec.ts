import { TestBed, ComponentFixture } from '@angular/core/testing';
import { PersonaliseComponent } from './personalise.component';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

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
        PersonaliseComponent
      ],
      providers: [
        {
          provide: MatDialog,
          useValue: {
            open: () => ({
              afterClosed: () => ({
                subscribe: (fn: (value: any) => void) => fn({ newValue: 'testValue' })
              })
            })
          }
        }
      ]
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
});
