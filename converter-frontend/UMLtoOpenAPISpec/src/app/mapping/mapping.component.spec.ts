import { TestBed, ComponentFixture } from '@angular/core/testing';
import { MappingComponent } from './mapping.component';
import { ReactiveFormsModule, FormsModule, FormArray, FormBuilder } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {ToastrService} from "ngx-toastr";

describe('MappingComponent', () => {
  let component: MappingComponent;
  let fixture: ComponentFixture<MappingComponent>;
  let dialogSpy: jasmine.Spy;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        FormsModule,
        MatDialogModule,
        BrowserAnimationsModule,
        MappingComponent
      ],
      providers: [
        {
          provide: MatDialog,
          useValue: {
            open: () => ({
              afterClosed: () => ({
                subscribe: (fn: (value: any) => void) => fn('testMethod')
              })
            })
          }
        },
        {
          provide: ToastrService,
          useValue: jasmine.createSpyObj('ToastrService', ['success', 'error', 'warning', 'info'])
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MappingComponent);
    component = fixture.componentInstance;
    dialogSpy = spyOn(TestBed.inject(MatDialog), 'open').and.callThrough();
    component.mappingsForm.setControl('mappings', new FormArray([]));
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should add a new mapping', () => {
    const initialLength = component.elements.length;
    component.addElement();
    expect(component.elements.length).toBeGreaterThan(initialLength);
    expect(component.showApplyAdditionsButton).toBeTrue();
  });

  it('should open add element dialog', () => {
    component.addElement();
    component.openAddElementDialog(true, 0);
    expect(dialogSpy).toHaveBeenCalled();
  });

  it('should reset the form after applying additions', () => {
    component.addElement();
    component.addNewClassToElements();
    expect(component.elements.length).toBe(0);
    expect(component.showApplyAdditionsButton).toBeFalse();
  });
});
