import { TestBed, ComponentFixture } from '@angular/core/testing';
import { AddElementDialogComponent } from './add-element-dialog.component';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {ToastrService} from "ngx-toastr";

describe('AddElementDialogComponent', () => {
  let component: AddElementDialogComponent;
  let fixture: ComponentFixture<AddElementDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        MatDialogModule,
        BrowserAnimationsModule,
        AddElementDialogComponent
      ],
      providers: [
        { provide: MatDialogRef, useValue: { close: jasmine.createSpy('close') } },
        { provide: MAT_DIALOG_DATA, useValue: { isMethod: true } },
        {
          provide: ToastrService,
          useValue: jasmine.createSpyObj('ToastrService', ['success', 'error', 'warning', 'info'])
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AddElementDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should close dialog on cancel', () => {
    const dialogRef = TestBed.inject(MatDialogRef);
    component.onNoClick();
    expect(dialogRef.close).toHaveBeenCalled();
  });

  it('should submit form and close dialog', () => {
    component.elementForm.setValue({
      accessModifier: 'public',
      name: 'testMethod',
      dataType: 'String',
      parameters: ''
    });
    component.onSubmit();
    expect(component.dialogRef.close).toHaveBeenCalledWith('+testMethod() : String');
  });
});
