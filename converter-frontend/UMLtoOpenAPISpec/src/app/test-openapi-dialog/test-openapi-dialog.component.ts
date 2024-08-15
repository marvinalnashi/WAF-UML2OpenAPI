import { Component, Inject } from '@angular/core';
import { MockServerService } from "../mock-server.service";
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from "@angular/material/dialog";
import { JsonPipe, NgForOf, NgIf } from "@angular/common";
import { MatButton } from "@angular/material/button";
import { MatCheckbox } from "@angular/material/checkbox";
import { FormsModule } from "@angular/forms";

@Component({
  selector: 'app-test-openapi-dialog',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    NgIf,
    MatButton,
    MatDialogActions,
    JsonPipe,
    NgForOf,
    MatCheckbox,
    FormsModule
  ],
  templateUrl: './test-openapi-dialog.component.html',
  styleUrls: ['./test-openapi-dialog.component.scss']
})
export class TestOpenApiDialogComponent {
  step: number = 1;
  testResults: any[] = [];
  availableTests: any[] = [];

  constructor(
    public dialogRef: MatDialogRef<TestOpenApiDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private mockServerService: MockServerService
  ) {}

  ngOnInit(): void {
    this.mockServerService.testOpenApiSpecification().subscribe(tests => {
      this.availableTests = tests;
    }, error => {
      console.error('Error loading tests', error);
    });
  }

  onCloseClick(): void {
    this.dialogRef.close();
  }

  onContinue(): void {
    this.step = 2;
  }

  onTestClick(): void {
    const selectedTests = this.availableTests.filter(test => test.selected);
    if (selectedTests.length > 0) {
      this.testResults = [];
      selectedTests.forEach(test => {
        this.mockServerService.testApi(test.method, test.path, test.body).subscribe(result => {
          this.testResults.push({ method: test.method, path: test.path, success: true, result });
        }, error => {
          this.testResults.push({
            method: test.method,
            path: test.path,
            error: `Error Code: ${error.status}\nMessage: ${error.message}\nResponse Body: ${error.error}`,
            success: false
          });
        });
      });
    }
  }
}
